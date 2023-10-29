package tech.relaycorp.letro.utils.veraid

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.veraid.SignatureBundle
import tech.relaycorp.veraid.SignatureException
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class VeraidSignatureTest {
    private val stubPlaintext = "plaintext".toByteArray()

    private val originalBundleGenerator = VeraidSignature.signatureBundleGenerator

    @AfterEach
    fun restoreOriginalBundleGenerator() {
        VeraidSignature.signatureBundleGenerator = originalBundleGenerator
    }

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
            val ninetyDays = 90.days.toJavaDuration()
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
            val mockSignatureBundle = mockk<SignatureBundle>()

            VeraidSignature.signatureBundleGenerator = mockk()
            if (result.isSuccess) {
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
}
