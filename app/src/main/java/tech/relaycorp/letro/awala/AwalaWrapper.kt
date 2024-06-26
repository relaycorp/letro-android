@file:Suppress("NAME_SHADOWING")

package tech.relaycorp.letro.awala

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject
import kotlin.Exception

interface AwalaWrapper {
    suspend fun setUp()
    fun bindGateway(
        onBindSuccessful: () -> Unit,
        onBindFailure: (GatewayBindingException) -> Unit,
    )
    suspend fun registerFirstPartyEndpoint(): FirstPartyEndpoint
    suspend fun importServerThirdPartyEndpoint(
        connectionParams: ByteArray,
        firstPartyEndpoint: FirstPartyEndpoint,
    ): PublicThirdPartyEndpoint
    fun receiveMessages(): Flow<IncomingMessage>

    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    )

    suspend fun loadNonNullPublicFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint

    suspend fun loadNonNullPublicThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint

    suspend fun loadNonNullPrivateThirdPartyEndpoint(firstPartyNodeId: String, thirdPartyNodeId: String): PrivateThirdPartyEndpoint

    suspend fun authorizeIndefinitely(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    )

    suspend fun revokeAuthorization(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: AwalaEndpoint,
    )
}

class AwalaWrapperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger,
) : AwalaWrapper {

    override suspend fun setUp() {
        Awala.setUp(context)
    }

    override fun bindGateway(
        onBindSuccessful: () -> Unit,
        onBindFailure: (GatewayBindingException) -> Unit,
    ) {
        GatewayClient.bindAutomatically(onBindSuccessful, onBindFailure = onBindFailure)
    }

    override suspend fun registerFirstPartyEndpoint(): FirstPartyEndpoint {
        return FirstPartyEndpoint.register()
    }

    override fun receiveMessages(): Flow<IncomingMessage> {
        return GatewayClient.receiveMessages()
    }

    override suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    ) {
        logger.i(AwalaManagerImpl.TAG, "sendMessage() from ${firstPartyEndpoint.nodeId} to ${thirdPartyEndpoint.nodeId}: ${outgoingMessage.type})")
        GatewayClient.sendMessage(
            OutgoingMessage.build(
                type = outgoingMessage.type.value,
                content = outgoingMessage.content,
                senderEndpoint = firstPartyEndpoint,
                recipientEndpoint = thirdPartyEndpoint,
            ),
        )
        logger.i(AwalaManagerImpl.TAG, "Message sent (${outgoingMessage.type})")
    }

    override suspend fun importServerThirdPartyEndpoint(connectionParams: ByteArray, firstPartyEndpoint: FirstPartyEndpoint): PublicThirdPartyEndpoint {
        return try {
            PublicThirdPartyEndpoint.import(connectionParams, firstPartyEndpoint)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
    }

    override suspend fun authorizeIndefinitely(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    ) {
        val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)
        sendMessage(
            outgoingMessage = AwalaOutgoingMessage(
                type = MessageType.AuthorizeReceivingFromServer,
                content = auth,
            ),
            firstPartyEndpoint = firstPartyEndpoint,
            thirdPartyEndpoint = thirdPartyEndpoint,
        )
    }

    override suspend fun revokeAuthorization(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: AwalaEndpoint,
    ) {
        val thirdPartyEndpoint = when (thirdPartyEndpoint) {
            is AwalaEndpoint.Private -> loadNonNullPrivateThirdPartyEndpoint(
                firstPartyNodeId = firstPartyEndpoint.nodeId,
                thirdPartyNodeId = thirdPartyEndpoint.nodeId,
            )
            is AwalaEndpoint.Public -> loadNonNullPublicThirdPartyEndpoint(thirdPartyEndpoint.nodeId)
        }
        thirdPartyEndpoint.delete(firstPartyEndpoint)
    }

    override suspend fun loadNonNullPublicFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint {
        if (nodeId == null) throw AwalaException("nodeId for loading FirstPartyEndpoint is null")
        return FirstPartyEndpoint.load(nodeId) ?: throw AwalaException("FirstPartyEndpoint couldn't be loaded")
    }

    override suspend fun loadNonNullPublicThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint {
        if (nodeId == null) throw AwalaException("nodeId for loading ThirdPartyEndpoint is null")
        return PublicThirdPartyEndpoint.load(nodeId) ?: throw AwalaException("ThirdPartyEndpoint couldn't be loaded")
    }

    override suspend fun loadNonNullPrivateThirdPartyEndpoint(firstPartyNodeId: String, thirdPartyNodeId: String): PrivateThirdPartyEndpoint {
        return PrivateThirdPartyEndpoint.load(
            thirdPartyAddress = thirdPartyNodeId,
            firstPartyAddress = firstPartyNodeId,
        ) ?: throw AwalaException("ThirdPartyEndpoint couldn't be loaded")
    }
}

open class AwalaException(message: String) : AwaladroidException(message)

class FirstPartyEndpointRegisteringException(cause: Exception) : AwaladroidException("Couldn't register first party endpoint", cause)
