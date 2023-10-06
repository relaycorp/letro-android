package tech.relaycorp.letro.utils.models

import io.mockk.ConstantAnswer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.awaladroid.EncryptionInitializationException
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.RegistrationFailedException
import tech.relaycorp.awaladroid.SetupPendingException
import tech.relaycorp.letro.awala.AwalaManagerImpl
import tech.relaycorp.letro.awala.AwalaRepository
import tech.relaycorp.letro.awala.AwalaWrapper
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
internal fun createAwalaManager(
    awalaInitializationResult: AwalaInitializationResult = AwalaInitializationResult.SUCCESS,
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = AwalaManagerImpl(
    awalaRepository = mockk<AwalaRepository>().also {
        every { it.getServerFirstPartyEndpointNodeId() } answers {
            if (awalaInitializationResult != AwalaInitializationResult.CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION) "" else null
        }
        every { it.getServerThirdPartyEndpointNodeId() } answers {
            if (awalaInitializationResult != AwalaInitializationResult.CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT) "" else null
        }
        every { it.saveServerFirstPartyEndpointNodeId(any()) } returns Unit
        every { it.saveServerFirstPartyEndpointNodeId(any()) } returns Unit
    },
    processor = mockk(),
    logger = createLogger(),
    awala = createAwalaWrapper(awalaInitializationResult),
    ioDispatcher = ioDispatcher,
    awalaThreadContext = EmptyCoroutineContext,
)

private fun createAwalaWrapper(awalaInitializationResult: AwalaInitializationResult) = mockk<AwalaWrapper> {
    coEvery { setUp() } answers {
        if (awalaInitializationResult == AwalaInitializationResult.ANDROID_SECURITY_LIBRARY_CRASH) {
            throw EncryptionInitializationException(
                "Android security library exception",
                IllegalStateException("Android security library exception"),
            )
        } else {
            ConstantAnswer(Unit)
        }
    }
    coEvery { bindGateway() } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_GATEAWAY_BINDING) {
            throw GatewayBindingException("Gateway binding exception")
        } else {
            ConstantAnswer(Unit)
        }
    }
    coEvery { registerFirstPartyEndpoint() } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION) {
            throw RegistrationFailedException("Registration failed exception")
        } else {
            callOriginal()
        }
    }

    coEvery { importServerThirdPartyEndpoint(any()) } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT) {
            throw SetupPendingException()
        } else {
            callOriginal()
        }
    }
    coEvery { sendMessage(any(), any(), any()) } returns Unit
    coEvery { receiveMessages() } returns emptyFlow()
    coEvery { loadNonNullPublicFirstPartyEndpoint(any()) } answers { callOriginal() }
    coEvery { loadNonNullPublicThirdPartyEndpoint(any()) } answers { callOriginal() }
    coEvery { loadNonNullPrivateThirdPartyEndpoint(any(), any()) } answers { callOriginal() }
    coEvery { authorizeIndefinitely(any(), any()) } returns ByteArray(0)
}

internal enum class AwalaInitializationResult {
    SUCCESS,
    CRASH_ON_GATEAWAY_BINDING,
    CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION,
    CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT,
    ANDROID_SECURITY_LIBRARY_CRASH,
}
