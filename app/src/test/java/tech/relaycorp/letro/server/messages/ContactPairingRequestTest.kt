package tech.relaycorp.letro.server.messages

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import tech.relaycorp.letro.awala.message.ContactPairingRequest
import tech.relaycorp.letro.awala.message.InvalidPairingRequestException
import tech.relaycorp.letro.testing.awala.AWALA_ID_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_ID
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_ORG_NAME
import tech.relaycorp.letro.testing.veraid.VERAID_USER_NAME
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.veraid.VeraidSignatureException
import tech.relaycorp.letro.utils.veraid.VeraidSignatureProcessor
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.SignatureBundleVerification
import tech.relaycorp.veraid.pki.MemberIdBundle

@Isolated
class ContactPairingRequestTest {
    private val contactVeraidId = "not-$VERAID_MEMBER_ID"

    private val request = ContactPairingRequest(
        AWALA_ID_KEY_PAIR.public.encoded,
        contactVeraidId,
    )

    private val stubSignatureBundleSerialised = "signature bundle".toByteArray()

    @AfterEach
    fun clearMocks() {
        ContactPairingRequest.veraidSignatureProcessor = VeraidSignatureProcessor()
    }

    @Nested
    inner class Serialise {
        private val mockMemberIdBundle = mockk<MemberIdBundle>()

        @Test
        fun `VeraId SignatureBundle serialisation should be output`() {
            mockSignatureProducer(stubSignatureBundleSerialised)

            val signatureBundleSerialised =
                request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            signatureBundleSerialised shouldBe stubSignatureBundleSerialised
        }

        @Test
        fun `Requester Awala id key and contact VeraId id should be encapsulated`() {
            val mockProcessor = mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            val awalaIdKeyEncoded =
                SubjectPublicKeyInfo.getInstance(AWALA_ID_KEY_PAIR.public.encoded)
            val expectedRequestSerialisation = ASN1Utils.serializeSequence(
                listOf(awalaIdKeyEncoded, DERUTF8String(contactVeraidId)),
                false,
            )
            verify {
                mockProcessor.produce(
                    expectedRequestSerialisation,
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `Specified member id bundle should be used`() {
            val mockProcessor = mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            verify {
                mockProcessor.produce(
                    any(),
                    mockMemberIdBundle,
                    any(),
                )
            }
        }

        @Test
        fun `Specified private key should be used`() {
            val mockProcessor = mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            verify {
                mockProcessor.produce(
                    any(),
                    any(),
                    VERAID_MEMBER_KEY_PAIR.private,
                )
            }
        }

        private fun mockSignatureProducer(signatureBundleSerialised: ByteArray): VeraidSignatureProcessor {
            val processor = mockk<VeraidSignatureProcessor>()
            every {
                processor.produce(
                    any(),
                    any(),
                    any(),
                )
            } returns signatureBundleSerialised
            ContactPairingRequest.veraidSignatureProcessor = processor
            return processor
        }
    }

    @Nested
    inner class Deserialise {
        private val stubVerification = SignatureBundleVerification(
            ASN1Utils.serializeSequence(
                listOf(
                    SubjectPublicKeyInfo.getInstance(request.requesterEndpointPublicKey),
                    DERUTF8String(VERAID_MEMBER_ID),
                ),
                false,
            ),
            Member(orgName = "not-$VERAID_ORG_NAME", userName = "not-$VERAID_USER_NAME"),
        )

        @Test
        fun `Specified serialisation should be verified`() = runTest {
            val mockProcessor = mockSignatureVerifier(Result.success(stubVerification))

            ContactPairingRequest.deserialise(stubSignatureBundleSerialised)

            coVerify {
                mockProcessor.verify(stubSignatureBundleSerialised)
            }
        }

        @Test
        fun `Signature validation exceptions should be wrapped`() = runTest {
            val originalException = VeraidSignatureException("error")
            mockSignatureVerifier(Result.failure(originalException))

            val exception = shouldThrow<InvalidPairingRequestException> {
                ContactPairingRequest.deserialise(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Invalid VeraId signature"
            exception.cause shouldBe originalException
        }

        @Test
        fun `Signer should be a VeraId user`() = runTest {
            val verification =
                stubVerification.copy(member = stubVerification.member.copy(userName = null))
            mockSignatureVerifier(Result.success(verification))

            val exception = shouldThrow<InvalidPairingRequestException> {
                ContactPairingRequest.deserialise(stubSignatureBundleSerialised)
            }

            exception.message shouldBe "Signer is a VeraId org bot"
        }

        @Test
        fun `VeraId id of signer should be output`() = runTest {
            mockSignatureVerifier(Result.success(stubVerification))

            val (signerVeraidId, _) = ContactPairingRequest.deserialise(
                stubSignatureBundleSerialised,
            )

            val signer = stubVerification.member
            signerVeraidId shouldBe "${signer.userName}@${signer.orgName}"
        }

        @Nested
        inner class ContactPairingRequestDeserialisation {
            @Test
            fun `Sequence should have at least 2 items`() = runTest {
                val verification = stubVerification.copy(
                    plaintext = ASN1Utils.serializeSequence(
                        listOf(
                            SubjectPublicKeyInfo.getInstance(
                                request.requesterEndpointPublicKey,
                            ),
                        ),
                        false,
                    ),
                )
                mockSignatureVerifier(Result.success(verification))

                val exception = shouldThrow<InvalidPairingRequestException> {
                    ContactPairingRequest.deserialise(stubSignatureBundleSerialised)
                }

                exception.message shouldBe
                    "ContactPairingRequest sequence contains fewer than 2 items"
            }

            @Test
            fun `Requester Awala id key should be a public key`() = runTest {
                val verification = stubVerification.copy(
                    plaintext = ASN1Utils.serializeSequence(
                        listOf(
                            DERUTF8String("not-a-public-key"),
                            DERUTF8String(contactVeraidId),
                        ),
                        false,
                    ),
                )
                mockSignatureVerifier(Result.success(verification))

                val exception = shouldThrow<InvalidPairingRequestException> {
                    ContactPairingRequest.deserialise(stubSignatureBundleSerialised)
                }

                exception.message shouldBe
                    "Requester endpoint public key is not a SubjectPublicKeyInfo"
                exception.cause should beInstanceOf<IllegalStateException>()
            }

            @Test
            fun `Requester Awala id key should be output`() = runTest {
                mockSignatureVerifier(Result.success(stubVerification))

                val (_, contactPairingRequest) = ContactPairingRequest.deserialise(
                    stubSignatureBundleSerialised,
                )

                contactPairingRequest.requesterEndpointPublicKey shouldBe
                    request.requesterEndpointPublicKey
            }

            @Test
            fun `Contact VeraId id should be output`() = runTest {
                mockSignatureVerifier(Result.success(stubVerification))

                val (_, contactPairingRequest) = ContactPairingRequest.deserialise(
                    stubSignatureBundleSerialised,
                )

                contactPairingRequest.contactVeraidId shouldBe VERAID_MEMBER_ID
            }
        }

        private fun mockSignatureVerifier(result: Result<SignatureBundleVerification>): VeraidSignatureProcessor {
            val processor = mockk<VeraidSignatureProcessor>()
            if (result.isSuccess) {
                coEvery {
                    processor.verify(any())
                } returns result.getOrThrow()
            } else {
                coEvery {
                    processor.verify(any())
                } throws result.exceptionOrNull()!!
            }
            ContactPairingRequest.veraidSignatureProcessor = processor
            return processor
        }
    }
}
