package tech.relaycorp.letro.awala

import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.utils.models.awala.createAwalaManager

@OptIn(ExperimentalCoroutinesApi::class)
class AwalaManagerTest {

    @Test
    fun `Test Awala initialization`() {
        val messagesFlow = mockk<Flow<IncomingMessage>>(relaxed = true)
        val awala = mockk<AwalaWrapper>(relaxed = true) {
            every { receiveMessages() } returns messagesFlow
        }
        createAwalaManager(
            awala = awala,
        )
        coVerifyAll {
            awala.setUp()
            awala.bindGateway()
            awala.receiveMessages()
            messagesFlow.collect(any())
        }
    }

    @Test
    fun `Test that message is being sent if there are first and third party endpoints registred`() {
        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
        val awala = mockk<AwalaWrapper>(relaxed = true)
        val awalaManager = createAwalaManager(
            awalaRepository = object : AwalaRepository {
                override fun getServerFirstPartyEndpointNodeId(): String {
                    return ""
                }
                override fun getServerThirdPartyEndpointNodeId(): String {
                    return ""
                }
                override fun saveServerFirstPartyEndpointNodeId(nodeId: String) = Unit
                override fun saveServerThirdPartyEndpointNodeId(nodeId: String) = Unit
            },
            awala = awala,
        )

        val message = AwalaOutgoingMessage(
            type = MessageType.NewMessage,
            content = ByteArray(0),
        )
        val recipient = AwalaEndpoint.Private("")
        coroutineScope.launch {
            awalaManager.sendMessage(
                outgoingMessage = message,
                recipient = recipient,
            )
        }
        coVerify {
            awala.sendMessage(message, any(), any())
        }
    }
}
