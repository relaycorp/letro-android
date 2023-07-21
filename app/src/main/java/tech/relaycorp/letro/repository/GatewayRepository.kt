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
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.data.AccountCreatedDataModel
import tech.relaycorp.letro.data.ContentType
import tech.relaycorp.letro.data.EndpointPairDataModel
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestAdresses
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

    private val _pairingRequestSent: MutableSharedFlow<PairingRequestAdresses> =
        MutableSharedFlow()
    val pairingRequestSent: SharedFlow<PairingRequestAdresses> get() = _pairingRequestSent

    private val _pairingMatchReceived: MutableSharedFlow<PairingMatchDataModel> =
        MutableSharedFlow()
    val pairingMatchReceived: SharedFlow<PairingMatchDataModel> get() = _pairingMatchReceived

    private val _pairingAuthorizationSent: MutableSharedFlow<PairingMatchDataModel> =
        MutableSharedFlow()
    val pairingAuthorizationSent: SharedFlow<PairingMatchDataModel> get() = _pairingAuthorizationSent

    private val _pairingAuthorizationReceived: MutableSharedFlow<PairingMatchDataModel> =
        MutableSharedFlow()
    val pairingAuthorizationReceived: SharedFlow<PairingMatchDataModel> get() = _pairingAuthorizationReceived

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

    fun sendCreateAccountRequest(address: String) {
        gatewayScope.launch {
            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(serverFirstPartyEndpointNodeId.value)
            val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(serverThirdPartyEndpointNodeId.value)

            val message = OutgoingMessage.build(
                type = ContentType.AccountCreationRequest.value,
                content = address.toByteArray(),
                senderEndpoint = firstPartyEndpoint,
                recipientEndpoint = thirdPartyEndpoint,
            )

            GatewayClient.sendMessage(message)
        }
    }

    fun startPairingWithContact(pairingRequestAdresses: PairingRequestAdresses) {
        gatewayScope.launch {
            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(serverFirstPartyEndpointNodeId.value)
            val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(serverThirdPartyEndpointNodeId.value)

            val pairingRequestContent: ByteArray = generatePairingRequest(
                pairingRequestAdresses.requesterVeraId,
                pairingRequestAdresses.contactVeraId,
                firstPartyEndpoint,
            )

            val pairingRequestMessage = OutgoingMessage.build(
                ContentType.ContactPairingRequest.value,
                pairingRequestContent,
                firstPartyEndpoint,
                thirdPartyEndpoint,
            )

            GatewayClient.sendMessage(pairingRequestMessage)
            _pairingRequestSent.emit(pairingRequestAdresses)
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
                when (message.type) {
                    ContentType.AccountCreationCompleted.value -> {
                        val addresses =
                            message.content.toString(Charset.defaultCharset()).split(",")
                        _accountCreatedConfirmationReceivedFromServer.emit(
                            AccountCreatedDataModel(
                                addresses[0],
                                addresses[1],
                            ),
                        )
                        message.ack()
                    }
                    ContentType.ContactPairingMatch.value -> {
                        val pairingMatch = parsePairingMatch(message.content)
                        _pairingMatchReceived.emit(pairingMatch)
                        message.ack()
                    }
                    ContentType.ContactPairingAuthorization.value -> {
                        val pairingMatch = parsePairingMatch(message.content)
                        _pairingAuthorizationReceived.emit(pairingMatch)
                        message.ack()
                    }
                    else -> {
// TODO throw Exception("Unknown message type: ${message.type}")
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
        // TODO DELETE THIS COMMENTED CODE
        // NOTE to Gus: All this logic is done before in the AccountRepository
//        // Implement some app-specific logic to check that the pairing request exists.
//        if (!contactRequestExists(match.requesterVeraId, match.contactVeraId)) {
//            // Granting authorisation is a sensitive operation and we shouldn't blindly
//            // trust the server.
//            throw PairingRequestException("Pairing request does not exist ($match)")
//        }
//
//        // Implement some app-specific logic to store the contact's Awala endpoint id, as
//        // we'll need it later to (a) complete pairing and (b) send messages to them.
//        storeContactAwalaId(
//            match.requesterVeraId,
//            match.contactVeraId,
//            match.contactEndpointId,
//        )

        return firstPartyEndpoint.authorizeIndefinitely(
            match.contactEndpointPublicKey,
        )
    }

    // TODO Potentially delete the following function
    // NOTE to Gus: In this implementation this function is not needed.
    // Only if you tell me that the import has to be done for Gateway to function properly
//    private suspend fun importPairingAuth(auth: ByteArray) {
//        // For the following line, I'm getting Cannot access class 'tech.relaycorp.relaynet.SessionKey'. Check your module classpath for missing or conflicting dependencies
//        val contactEndpoint = PrivateThirdPartyEndpoint.import(auth)
//
//        // TODO DELETE THIS COMMENTED CODE
//        // NOTE to Gus: All this logic is done after in the AccountRepository
// //        // Do whatever you need to mark the pairing as complete. For example:
// //        val contacts = getContactsByAwalaId(contactEndpoint.nodeId)
// //        for (contact in contacts) {
// //            contact.markPairingAsComplete()
// //        }
//    }

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
