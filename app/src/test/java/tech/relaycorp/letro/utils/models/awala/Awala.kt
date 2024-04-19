package tech.relaycorp.letro.utils.models.awala

import io.mockk.ConstantAnswer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import tech.relaycorp.letro.utils.models.utils.createLogger
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
internal fun createAwalaManager(
    awalaInitializationResult: AwalaInitializationResult = AwalaInitializationResult.SUCCESS,
    awalaRepository: AwalaRepository = mockk<AwalaRepository>().also {
        coEvery { it.getServerThirdPartyEndpointNodeId(any()) } answers {
            if (awalaInitializationResult != AwalaInitializationResult.CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT) "" else null
        }
        coEvery { it.hasRegisteredEndpoints() } answers {
            awalaInitializationResult != AwalaInitializationResult.CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION
        }
    },
    awala: AwalaWrapper = createAwalaWrapper(awalaInitializationResult),
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = AwalaManagerImpl(
    awalaRepository = awalaRepository,
    processor = mockk(),
    logger = createLogger(),
    awala = awala,
    ioDispatcher = ioDispatcher,
    awalaThreadContext = EmptyCoroutineContext,
    context = mockk(relaxed = true),
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

    val onSuccessBindCallable = slot<() -> Unit>()
    val onFailureBindCallable = slot<(GatewayBindingException) -> Unit>()
    every { bindGateway(capture(onSuccessBindCallable), capture(onFailureBindCallable)) } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_GATEAWAY_BINDING) {
            val exc = GatewayBindingException("Gateway binding exception")
            onFailureBindCallable.captured(exc)
        } else {
            onSuccessBindCallable.captured()
        }
        ConstantAnswer(Unit)
    }
    coEvery { registerFirstPartyEndpoint() } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION) {
            throw RegistrationFailedException("Registration failed exception")
        } else {
            callOriginal()
        }
    }

    coEvery { importServerThirdPartyEndpoint(any(), any()) } answers {
        if (awalaInitializationResult == AwalaInitializationResult.CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT) {
            throw SetupPendingException()
        } else {
            callOriginal()
        }
    }
    coEvery { sendMessage(any(), any(), any()) } returns Unit
    every { receiveMessages() } returns emptyFlow()
    coEvery { loadNonNullPublicFirstPartyEndpoint(any()) } answers { callOriginal() }
    coEvery { loadNonNullPublicThirdPartyEndpoint(any()) } answers { callOriginal() }
    coEvery { loadNonNullPrivateThirdPartyEndpoint(any(), any()) } answers { callOriginal() }
    coEvery { authorizeIndefinitely(any(), any()) } returns Unit
}

internal enum class AwalaInitializationResult {
    SUCCESS,
    CRASH_ON_GATEAWAY_BINDING,
    CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION,
    CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT,
    ANDROID_SECURITY_LIBRARY_CRASH,
}
