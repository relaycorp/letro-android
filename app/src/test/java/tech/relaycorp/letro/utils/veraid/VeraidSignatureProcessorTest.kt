package tech.relaycorp.letro.utils.veraid

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class VeraidSignatureProcessorTest {
    private val stubPlaintext = "plaintext".toByteArray()

    val ninetyDays = 90.days.toJavaDuration()

    @Nested
    inner class Produce {
        private val stubSignatureBundle = "signature bundle".toByteArray()
        private val mockMemberIdBundle = mockk<MemberIdBundle>()

        @Test
        fun `Output should be a VeraId signature bundle`() {
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)

            val bundleSerialised = processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            bundleSerialised shouldBe stubSignatureBundle
        }

        @Test
        fun `Plaintext should be encapsulated`() {
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                generator(
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
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                generator(
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
        fun `Signature creation date should be within 5 minutes of now`() {
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)
            val timeBefore = ZonedDateTime.now()

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            val timeAfter = ZonedDateTime.now()
            verify {
                generator(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    match {
                        timeBefore.minus(5.minutes.toJavaDuration()) <= it &&
                            it <= timeAfter.plus(5.minutes.toJavaDuration())
                    },
                    any(),
                )
            }
        }

        @Test
        fun `Signature should expire in 90 days`() {
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)
            val timeBefore = ZonedDateTime.now()

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            val timeAfter = ZonedDateTime.now()
            verify {
                generator(
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
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                generator(
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
            val generator = mockSignatureBundleGenerator(Result.success(stubSignatureBundle))
            val processor = VeraidSignatureProcessor(generator)

            processor.produce(
                stubPlaintext,
                mockMemberIdBundle,
                VERAID_MEMBER_KEY_PAIR.private,
            )

            verify {
                generator(
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
            val generator = mockSignatureBundleGenerator(Result.failure(exc))
            val processor = VeraidSignatureProcessor(generator)

            val exception = shouldThrow<VeraidSignatureException> {
                processor.produce(
                    stubPlaintext,
                    mockMemberIdBundle,
                    VERAID_MEMBER_KEY_PAIR.private,
                )
            }

            exception.message shouldBe "Failed to generate VeraId signature"
            exception.cause shouldBe exc
        }

        private fun mockSignatureBundleGenerator(result: Result<ByteArray>): BundleGenerator {
            val mockGenerator = mockk<BundleGenerator>()
            if (result.isSuccess) {
                val mockSignatureBundle = mockk<SignatureBundle>()
                every {
                    mockSignatureBundle.serialise()
                } returns result.getOrThrow()
                every {
                    mockGenerator(
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
                    mockGenerator(
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
            return mockGenerator
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
            val deserialiser = mockSignatureBundleDeserialiser(Result.failure(exc))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)

            val exception = shouldThrow<VeraidSignatureException> {
                processor.verify(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Failed to deserialise VeraId signature"
            exception.cause shouldBe exc
        }

        @Test
        fun `Invalid SignatureBundle should be refused`() = runTest {
            val originalException = SignatureException("Invalid")
            val mockSignatureBundle = mockBundleVerifier(Result.failure(originalException))
            val deserialiser = mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)

            val exception = shouldThrow<VeraidSignatureException> {
                processor.verify(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Invalid VeraId signature"
            exception.cause shouldBe originalException
        }

        @Test
        fun `Signature should be bound to the Letro VeraId service`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            val deserialiser = mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)

            processor.verify(stubSignatureBundleSerialised)

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
            val deserialiser = mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)
            val timeBefore = ZonedDateTime.now()

            processor.verify(stubSignatureBundleSerialised)

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
            val deserialiser = mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)

            val verification = processor.verify(stubSignatureBundleSerialised)

            verification.plaintext shouldBe stubVerification.plaintext
        }

        @Test
        fun `Signer should be output`() = runTest {
            val mockSignatureBundle = mockBundleVerifier(Result.success(stubVerification))
            val deserialiser = mockSignatureBundleDeserialiser(Result.success(mockSignatureBundle))
            val processor = VeraidSignatureProcessor(bundleDeserialiser = deserialiser)

            val verification = processor.verify(stubSignatureBundleSerialised)

            verification.member shouldBe stubVerification.member
        }

        private fun mockSignatureBundleDeserialiser(result: Result<SignatureBundle>): BundleDeserialiser {
            val deserialiser = mockk<BundleDeserialiser>()
            if (result.isSuccess) {
                every {
                    deserialiser(any())
                } returns result.getOrThrow()
            } else {
                every {
                    deserialiser(any())
                } throws result.exceptionOrNull()!!
            }
            return deserialiser
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
}
