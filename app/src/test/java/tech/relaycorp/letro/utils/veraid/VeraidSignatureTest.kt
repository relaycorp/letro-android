package tech.relaycorp.letro.utils.veraid

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_ORG_NAME
import tech.relaycorp.letro.testing.veraid.VERAID_USER_NAME
import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.veraid.DatePeriod
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.SignatureBundle
import tech.relaycorp.veraid.SignatureBundleVerification
import tech.relaycorp.veraid.SignatureException
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class VeraidSignatureTest {
    private val stubPlaintext = "plaintext".toByteArray()

    val ninetyDays = 90.days.toJavaDuration()


    @Nested
    inner class Produce {
        private val stubSignatureBundleSerialised = "signature bundle".toByteArray()
        private val mockMemberIdBundle = mockk<MemberIdBundle>()

        @Test
        fun `Output should be a VeraId signature bundle`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))

            val bundleSerialised = VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            bundleSerialised shouldBe stubSignatureBundleSerialised
        }

        @Test
        fun `Plaintext should be encapsulated`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))

            VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                VeraidSignature.signatureBundleGenerator(
                    stubPlaintext,
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    true,
                )
            }
        }

        @Test
        fun `Signature should be bound to the Letro VeraId service`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))

            VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                VeraidSignature.signatureBundleGenerator(
                    any(),
                    LetroOids.LETRO_VERAID_OID,
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `Signature should expire in 90 days`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))
            val timeBefore = ZonedDateTime.now()

            VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            val timeAfter = ZonedDateTime.now()
            verify {
                VeraidSignature.signatureBundleGenerator(
                    any(),
                    any(),
                    any(),
                    any(),
                    match {
                        timeBefore.plus(ninetyDays) <= it && it <= timeAfter.plus(ninetyDays)
                    },
                    match { timeBefore <= it && it <= timeAfter },
                    any(),
                )
            }
        }

        @Test
        fun `Signature should be bound to the specified member bundle`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))

            VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                VeraidSignature.signatureBundleGenerator(
                    any(),
                    any(),
                    mockMemberIdBundle,
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `Signature should be produced with specified private key`() {
            mockSignatureBundleGenerator(Result.success(stubSignatureBundleSerialised))

            VeraidSignature.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                VeraidSignature.signatureBundleGenerator(
                    any(),
                    any(),
                    any(),
                    VERAID_MEMBER_KEY_PAIR.private,
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `Signature generation exceptions should be wrapped`() {
            val exc = SignatureException("test exception")
            mockSignatureBundleGenerator(Result.failure(exc))

            val exception = shouldThrow<VeraidSignatureException> {
                VeraidSignature.produce(
                    stubPlaintext,
                    mockMemberIdBundle,
                    VERAID_MEMBER_KEY_PAIR.private,
                )
            }

            exception.message shouldBe "Failed to generate VeraId signature"
            exception.cause shouldBe exc
        }

        private fun mockSignatureBundleGenerator(result: Result<ByteArray>) {
            VeraidSignature.signatureBundleGenerator = mockk()
            if (result.isSuccess) {
                val mockSignatureBundle = mockk<SignatureBundle>()
                every {
                    mockSignatureBundle.serialise()
                } returns result.getOrThrow()
                every {
                    VeraidSignature.signatureBundleGenerator.invoke(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns mockSignatureBundle
            } else {
                every {
                    VeraidSignature.signatureBundleGenerator.invoke(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } throws result.exceptionOrNull()!!
            }
        }
    }

    @Nested
    inner class Verify {
        private val stubSignatureBundleSerialised = "signature bundle".toByteArray()
        private val stubVerification = SignatureBundleVerification(
            "the plaintext".toByteArray(),
            Member(orgName = VERAID_ORG_NAME, userName = VERAID_USER_NAME),
        )

        @Test
        fun `Malformed SignatureBundle should be refused`() = runTest {
            val exc = SignatureException("Malformed")
            mockSignatureBundleDeserialiser(Result.failure(exc))

            val exception = shouldThrow<VeraidSignatureException> {
                VeraidSignature.verify(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Failed to deserialise VeraId signature"
            exception.cause shouldBe exc
        }

        @Test
        fun `Invalid SignatureBundle should be refused`() = runTest {
            val originalException = SignatureException("Invalid")
            val mockSignatureBundle = mockBundleVerifier(Result.failure(originalException))
            mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))

            val exception = shouldThrow<VeraidSignatureException> {
                VeraidSignature.verify(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Invalid VeraId signature"
            exception.cause shouldBe originalException
        }

        @Test
        fun `Signature should be bound to the Letro VeraId service`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))

            VeraidSignature.verify(stubSignatureBundleSerialised)

            coVerify {
                mockSignatureBundle.verify(
                    any(),
                    LetroOids.LETRO_VERAID_OID,
                    any<DatePeriod>(),
                )
            }
        }

        @Test
        fun `Signature should have been valid in the past 90 days`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val timeBefore = ZonedDateTime.now()

            VeraidSignature.verify(stubSignatureBundleSerialised)

            val timeAfter = ZonedDateTime.now()
            coVerify {
                mockSignatureBundle.verify(
                    any(),
                    any(),
                    match<DatePeriod> {
                        timeBefore.minus(ninetyDays) <= it.start &&
                                it.start <= timeAfter.minus(ninetyDays) &&
                                it.endInclusive <= timeAfter &&
                                timeBefore <= it.endInclusive
                    },
                )
            }
        }

        @Test
        fun `Encapsulated plaintext should be output`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))

            val verification = VeraidSignature.verify(stubSignatureBundleSerialised)

            verification.plaintext shouldBe stubVerification.plaintext
        }

        @Test
        fun `Signer should be output`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))

            val verification = VeraidSignature.verify(stubSignatureBundleSerialised)

            verification.member shouldBe stubVerification.member
        }

        private fun mockSignatureBundleDeserialiser(result: Result<SignatureBundle>) {
            VeraidSignature.signatureBundleDeserialiser = mockk()
            if (result.isSuccess) {
                every {
                    VeraidSignature.signatureBundleDeserialiser(any())
                } returns result.getOrThrow()
            } else {
                every {
                    VeraidSignature.signatureBundleDeserialiser(any())
                } throws result.exceptionOrNull()!!
            }
        }

        private fun mockBundleVerifier(result: Result<SignatureBundleVerification>): SignatureBundle {
            val mockSignatureBundle = mockk<SignatureBundle>()
            if (result.isSuccess) {
                coEvery {
                    mockSignatureBundle.verify(null, any(), any<DatePeriod>())
                } returns result.getOrThrow()
            } else {
                coEvery {
                    mockSignatureBundle.verify(null, any(), any<DatePeriod>())
                } throws result.exceptionOrNull()!!
            }
            return mockSignatureBundle
        }
    }

    companion object {
        private val originalBundleGenerator = VeraidSignature.signatureBundleGenerator
        private val originalBundleDeserialiser = VeraidSignature.signatureBundleDeserialiser

        @JvmStatic
        @AfterAll
        fun restoreOriginalBundleFunction() {
            VeraidSignature.signatureBundleGenerator = originalBundleGenerator
            VeraidSignature.signatureBundleDeserialiser = originalBundleDeserialiser
        }
    }
}
