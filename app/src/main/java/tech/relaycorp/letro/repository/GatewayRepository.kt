package tech.relaycorp.letro.repository

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.data.AccountCreatedDataModel
import tech.relaycorp.letro.data.ContentType
import tech.relaycorp.letro.data.EndpointPairDataModel
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestVeraIdsDataModel
import tech.relaycorp.letro.utility.loadNonNullFirstPartyEndpoint
import tech.relaycorp.letro.utility.loadNonNullThirdPartyEndpoint
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayRepository @Inject constructor(
    @ApplicationContext var context: Context,
    private val preferencesDataStoreRepository: PreferencesDataStoreRepository,
) {

    private val gatewayScope = CoroutineScope(Dispatchers.IO)

    private val _isGatewayAvailable: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val isGatewayAvailable: StateFlow<Boolean?> get() = _isGatewayAvailable

    private val _isGatewayFullySetup: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isGatewayFullySetup: StateFlow<Boolean> get() = _isGatewayFullySetup

    private val _accountCreatedConfirmationReceivedFromServer: MutableSharedFlow<AccountCreatedDataModel> =
        MutableSharedFlow()
    val accountCreationConfirmationReceivedFromServer: SharedFlow<AccountCreatedDataModel> get() = _accountCreatedConfirmationReceivedFromServer

    private val _pairingRequestSent: MutableSharedFlow<PairingRequestVeraIdsDataModel> =
        MutableSharedFlow()
    val pairingRequestSent: SharedFlow<PairingRequestVeraIdsDataModel> get() = _pairingRequestSent

    private val _pairingMatchReceived: MutableSharedFlow<PairingMatchDataModel> =
        MutableSharedFlow()
    val pairingMatchReceived: SharedFlow<PairingMatchDataModel> get() = _pairingMatchReceived

    private val _pairingAuthorizationSent: MutableSharedFlow<PairingMatchDataModel> =
        MutableSharedFlow()
    val pairingAuthorizationSent: SharedFlow<PairingMatchDataModel> get() = _pairingAuthorizationSent

    private val _pairingAuthorizationReceived: MutableSharedFlow<String> =
        MutableSharedFlow()
    val pairingAuthorizationReceived: SharedFlow<String> get() = _pairingAuthorizationReceived

    private val serverFirstPartyEndpointNodeId = preferencesDataStoreRepository.serverFirstPartyEndpointNodeId
    private val serverThirdPartyEndpointNodeId = preferencesDataStoreRepository.serverThirdPartyEndpointNodeId

    init {
        checkIfGatewayIsAvailable()

        gatewayScope.launch {
            _isGatewayAvailable.collect { isAvailable: Boolean? ->
                if (isAvailable == true) {
                    registerServerFirstPartyEndpointIfNeeded()
                    importServerThirdPartyEndpointIfNeeded()
                    collectToUpdateIsGatewayAuthorizedReceivingMessagesFromServer()
                    updateIsGatewayFullySetup()
                    startReceivingMessages()
                }
            }
        }
    }

    // TODO Clean the implementation of this when possible
    private var awalaIsSetup = false

    fun checkIfGatewayIsAvailable() {
        gatewayScope.launch {
            if (!awalaIsSetup) {
                Awala.setUp(context)
                awalaIsSetup = true
            }
            try {
                GatewayClient.bind()
                _isGatewayAvailable.emit(true)
            } catch (exp: GatewayBindingException) {
                _isGatewayAvailable.emit(false)
            }
        }
    }

    fun sendCreateAccountRequest(veraId: String) {
        gatewayScope.launch {
            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(serverFirstPartyEndpointNodeId.value)
            val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(serverThirdPartyEndpointNodeId.value)

            val message = OutgoingMessage.build(
                type = ContentType.AccountCreationRequest.value,
                content = veraId.toByteArray(),
                senderEndpoint = firstPartyEndpoint,
                recipientEndpoint = thirdPartyEndpoint,
            )

            GatewayClient.sendMessage(message)
        }
    }

    fun startPairingWithContact(pairingRequestVeraIds: PairingRequestVeraIdsDataModel) {
        gatewayScope.launch {
            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(serverFirstPartyEndpointNodeId.value)
            val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(serverThirdPartyEndpointNodeId.value)

            val pairingRequestContent: ByteArray = generatePairingRequest(
                pairingRequestVeraIds.requesterVeraId,
                pairingRequestVeraIds.contactVeraId,
                firstPartyEndpoint,
            )

            val pairingRequestMessage = OutgoingMessage.build(
                ContentType.ContactPairingRequest.value,
                pairingRequestContent,
                firstPartyEndpoint,
                thirdPartyEndpoint,
            )

            GatewayClient.sendMessage(pairingRequestMessage)
            _pairingRequestSent.emit(pairingRequestVeraIds)
        }
    }

    fun sendPairingAuthorizationRequest(match: PairingMatchDataModel) {
        gatewayScope.launch {
            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(serverFirstPartyEndpointNodeId.value)
            val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(serverThirdPartyEndpointNodeId.value)

            val pairingAuth = generatePairingAuth(match, firstPartyEndpoint)

            val pairingAuthMessage = OutgoingMessage.build(
                ContentType.ContactPairingAuthorization.value,
                pairingAuth,
                firstPartyEndpoint,
                thirdPartyEndpoint,
            )

            GatewayClient.sendMessage(pairingAuthMessage)
            _pairingAuthorizationSent.emit(match)
        }
    }

    private fun startReceivingMessages() {
        gatewayScope.launch {
            GatewayClient.receiveMessages().collect { message ->
                // TODO Remove first message.ack() before publishing the app.
                // It's here to avoid the server getting stuck with messages that can't be processed.
                message.ack()
                when (message.type) {
                    ContentType.AccountCreationCompleted.value -> {
                        val veraIds =
                            message.content.toString(Charset.defaultCharset()).split(",")
                        _accountCreatedConfirmationReceivedFromServer.emit(
                            AccountCreatedDataModel(
                                veraIds[0],
                                veraIds[1],
                            ),
                        )
                        message.ack()
                    }
                    ContentType.ContactPairingMatch.value -> {
                        // TODO Simplify this by moving to the ContactRepository
                        val pairingMatch = parsePairingMatch(message.content)
                        _pairingMatchReceived.emit(pairingMatch)
                        message.ack()
                    }
                    ContentType.ContactPairingAuthorization.value -> {
                        // TODO Simplify this by moving to the ContactRepository
                        val privateThirdPartyEndpoint = importPairingAuth(message.content)
                        _pairingAuthorizationReceived.emit(privateThirdPartyEndpoint.nodeId)
                        message.ack()
                    }
                    else -> {
                        // TODO Log rather than throw
                    }
                }
            }
        }
    }

    private fun shouldAuthorizeReceivingMessages(
        firstPartyEndpointNodeId: String?,
        thirdPartyEndpointNodeId: String?,
        authorizedReceivingMessagesFromServer: Boolean?,
    ) = authorizedReceivingMessagesFromServer != true &&
        firstPartyEndpointNodeId != null &&
        thirdPartyEndpointNodeId != null

    private fun generatePairingRequest(
        requesterVeraId: String,
        contactVeraId: String,
        requesterEndpoint: FirstPartyEndpoint,
    ): ByteArray {
        val publicKey = requesterEndpoint.publicKey
        val publicKeyBase64 = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        val content = "$requesterVeraId,$contactVeraId,$publicKeyBase64"
        return content.toByteArray()
    }

    private fun parsePairingMatch(content: ByteArray): PairingMatchDataModel {
        val contentString = content.toString(Charset.defaultCharset())
        val parts = contentString.split(",")
        return PairingMatchDataModel(
            requesterVeraId = parts[0],
            contactVeraId = parts[1],
            contactEndpointId = parts[2],
            contactEndpointPublicKey = Base64.decode(parts[3], Base64.NO_WRAP),
        )
    }

    private suspend fun generatePairingAuth(
        match: PairingMatchDataModel,
        firstPartyEndpoint: FirstPartyEndpoint,
    ): ByteArray {
        return firstPartyEndpoint.authorizeIndefinitely(
            match.contactEndpointPublicKey,
        )
    }

    private suspend fun importPairingAuth(auth: ByteArray): PrivateThirdPartyEndpoint {
        return PrivateThirdPartyEndpoint.import(auth)
    }

    // TODO Maybe use something else than combine
    private fun collectToUpdateIsGatewayAuthorizedReceivingMessagesFromServer() {
        gatewayScope.launch {
            combine(
                serverFirstPartyEndpointNodeId,
                serverThirdPartyEndpointNodeId,
                preferencesDataStoreRepository.isGatewayAuthorizedToReceiveMessagesFromServer,
            ) {
                    firstPartyEndpointNodeId,
                    thirdPartyEndpointNodeId,
                    authorizedReceivingMessagesFromServer,
                ->

                if (shouldAuthorizeReceivingMessages(
                        firstPartyEndpointNodeId,
                        thirdPartyEndpointNodeId,
                        authorizedReceivingMessagesFromServer,
                    )
                ) {
                    EndpointPairDataModel(
                        firstPartyEndpointNodeId = firstPartyEndpointNodeId!!,
                        thirdPartyEndpointNodeId = thirdPartyEndpointNodeId!!,
                    )
                } else {
                    null
                }
            }.collect { endpointPair ->
                endpointPair?.let {
                    authoriseReceivingMessagesFromThirdPartyEndpoint(
                        it.firstPartyEndpointNodeId,
                        it.thirdPartyEndpointNodeId,
                    )
                }
            }
        }
    }

    private fun updateIsGatewayFullySetup() {
        gatewayScope.launch {
            preferencesDataStoreRepository.isGatewayAuthorizedToReceiveMessagesFromServer.collect { isAuthorized ->
                _isGatewayFullySetup.emit(isAuthorized == true)
            }
        }
    }

    private fun importServerThirdPartyEndpointIfNeeded() {
        gatewayScope.launch {
            preferencesDataStoreRepository.serverThirdPartyEndpointNodeId.collect {
                if (it == null) {
                    importPublicThirdPartyEndpoint()
                }
            }
        }
    }

    private fun registerServerFirstPartyEndpointIfNeeded() {
        gatewayScope.launch {
            preferencesDataStoreRepository.serverFirstPartyEndpointNodeId.collect {
                if (it == null) {
                    registerFirstPartyEndpoint()
                }
            }
        }
    }

    private suspend fun registerFirstPartyEndpoint() {
        val endpoint = FirstPartyEndpoint.register()
        preferencesDataStoreRepository.saveServerFirstPartyEndpointNodeId(endpoint.nodeId)
    }

    private suspend fun importPublicThirdPartyEndpoint() {
        val endpoint = importPublicThirdPartyEndpoint(
            context.resources.openRawResource(R.raw.server_connection_params).use {
                it.readBytes()
            },
        )

        preferencesDataStoreRepository.saveServerThirdPartyEndpointNodeId(endpoint.nodeId)
    }

    private suspend fun authoriseReceivingMessagesFromThirdPartyEndpoint(
        firstPartyEndpointNodeId: String,
        thirdPartyEndpointNodeId: String,
    ) {
        val firstPartyEndpoint = FirstPartyEndpoint.load(firstPartyEndpointNodeId)
        val thirdPartyEndpoint = PublicThirdPartyEndpoint.load(thirdPartyEndpointNodeId)

        if (firstPartyEndpoint == null || thirdPartyEndpoint == null) {
            return
        }

        // Create the Parcel Delivery Authorisation (PDA)
        val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)

        // Send it to the server
        val authMessage = OutgoingMessage.build(
            ContentType.AuthorizeReceivingFromServer.value,
            auth,
            firstPartyEndpoint,
            thirdPartyEndpoint,
        )

        GatewayClient.sendMessage(authMessage)

        preferencesDataStoreRepository.saveAuthorizedReceivingMessagesFromServer(true)
    }

    @Throws(InvalidConnectionParams::class)
    private suspend fun importPublicThirdPartyEndpoint(connectionParams: ByteArray): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(connectionParams)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        return endpoint
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)
