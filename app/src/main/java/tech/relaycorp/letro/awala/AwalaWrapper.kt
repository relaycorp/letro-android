package tech.relaycorp.letro.awala

import android.content.Context
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import java.lang.Exception
import javax.inject.Inject

interface AwalaWrapper {
    suspend fun setUp()
    suspend fun bindGateway()
    suspend fun registerFirstPartyEndpoint(): FirstPartyEndpoint
    suspend fun importServerThirdPartyEndpoint(
        @RawRes connectionParams: Int,
    ): PublicThirdPartyEndpoint
    suspend fun receiveMessages(): Flow<IncomingMessage>

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
    ): ByteArray

    suspend fun revokeAuthorization(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpointNodeId: String,
    )
}

class AwalaWrapperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AwalaWrapper {

    override suspend fun setUp() {
        Awala.setUp(context)
    }

    override suspend fun bindGateway() {
        GatewayClient.bind()
    }

    override suspend fun registerFirstPartyEndpoint(): FirstPartyEndpoint {
        return FirstPartyEndpoint.register()
    }

    override suspend fun receiveMessages(): Flow<IncomingMessage> {
        return GatewayClient.receiveMessages()
    }

    override suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    ) {
        GatewayClient.sendMessage(
            OutgoingMessage.build(
                type = outgoingMessage.type.value,
                content = outgoingMessage.content,
                senderEndpoint = firstPartyEndpoint,
                recipientEndpoint = thirdPartyEndpoint,
            ),
        )
    }

    override suspend fun importServerThirdPartyEndpoint(connectionParams: Int): PublicThirdPartyEndpoint {
        return try {
            PublicThirdPartyEndpoint.import(
                context.resources.openRawResource(connectionParams).use {
                    it.readBytes()
                },
            )
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
    }

    override suspend fun authorizeIndefinitely(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpoint: ThirdPartyEndpoint,
    ): ByteArray {
        return firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)
    }

    override suspend fun revokeAuthorization(
        firstPartyEndpoint: FirstPartyEndpoint,
        thirdPartyEndpointNodeId: String,
    ) {
        val thirdPartyEndpoint = loadNonNullPrivateThirdPartyEndpoint(
            firstPartyNodeId = firstPartyEndpoint.nodeId,
            thirdPartyNodeId = thirdPartyEndpointNodeId,
        )
        thirdPartyEndpoint.delete()
    }

    override suspend fun loadNonNullPublicFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint {
        if (nodeId == null) throw Exception("nodeId for loading FirstPartyEndpoint is null")
        return FirstPartyEndpoint.load(nodeId) ?: throw Exception("FirstPartyEndpoint couldn't be loaded")
    }

    override suspend fun loadNonNullPublicThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint {
        if (nodeId == null) throw Exception("nodeId for loading ThirdPartyEndpoint is null")
        return PublicThirdPartyEndpoint.load(nodeId) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
    }

    override suspend fun loadNonNullPrivateThirdPartyEndpoint(firstPartyNodeId: String, thirdPartyNodeId: String): PrivateThirdPartyEndpoint {
        return PrivateThirdPartyEndpoint.load(
            thirdPartyAddress = thirdPartyNodeId,
            firstPartyAddress = firstPartyNodeId,
        ) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
    }
}
