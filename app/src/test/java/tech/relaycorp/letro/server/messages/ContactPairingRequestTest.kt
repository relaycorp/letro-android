package tech.relaycorp.letro.server.messages

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.bouncycastle.asn1.DERUTF8String
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.awala.message.ContactPairingRequest
import tech.relaycorp.letro.testing.awala.AWALA_ID_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_ID
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.veraid.VeraidSignature
import tech.relaycorp.veraid.pki.MemberIdBundle

class ContactPairingRequestTest {
    @Nested
    inner class Serialise {
        private val contactVeraidId = "not-$VERAID_MEMBER_ID"
        private val mockMemberIdBundle = mockk<MemberIdBundle>()

        private val request = ContactPairingRequest(
            AWALA_ID_KEY_PAIR.public,
            contactVeraidId,
        )

        private val stubSignatureBundleSerialised = "signature bundle".toByteArray()

        @AfterEach
        fun clearMocks() {
            mockkObject(VeraidSignature)
        }

        @Test
        fun `VeraId SignatureBundle serialisation should be output`() {
            mockSignatureProducer(stubSignatureBundleSerialised)

            val signatureBundleSerialised =
                request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            signatureBundleSerialised shouldBe stubSignatureBundleSerialised
        }

        @Test
        fun `Requester Awala id key and contact VeraId id should be encapsulated`() {
            mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            val expectedRequestSerialisation = ASN1Utils.serializeSequence(
                listOf(AWALA_ID_KEY_PAIR.public.spkiEncode(), DERUTF8String(contactVeraidId)),
                false,
            )
            verify {
                VeraidSignature.produce(
                    expectedRequestSerialisation,
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `Specified member id bundle should be used`() {
            mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            verify {
                VeraidSignature.produce(
                    any(),
                    mockMemberIdBundle,
                    any(),
                )
            }
        }

        @Test
        fun `Specified private key should be used`() {
            mockSignatureProducer(stubSignatureBundleSerialised)

            request.serialise(mockMemberIdBundle, VERAID_MEMBER_KEY_PAIR.private)

            verify {
                VeraidSignature.produce(
                    any(),
                    any(),
                    VERAID_MEMBER_KEY_PAIR.private,
                )
            }
        }

        private fun mockSignatureProducer(signatureBundleSerialised: ByteArray) {
            mockkObject(VeraidSignature)
            every {
                VeraidSignature.produce(
                    any(),
                    any(),
                    any(),
                )
            } returns signatureBundleSerialised
        }
    }
}
