package tech.relaycorp.letro.server.messages

import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.ASN1BitString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTF8String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_USER_NAME
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.RSASigning
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.i18n.normaliseString
import java.util.Locale

class AccountRequestTest {
    val userName = VERAID_USER_NAME
    val locale = Locale("EN", "GB")

    val keyPair = VERAID_MEMBER_KEY_PAIR

    @Nested
    inner class Serialize {
        @Nested
        inner class RequestSerialisation {
            @Nested
            inner class UserName {
                @Test
                fun `Should serialise the user name as UTF8String`() {
                    val request = AccountRequest(
                        userName = userName,
                        locale = locale,
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val requestSequence = parseRequestSequence(serialisation)
                    val userNameEncoded = ASN1UTF8String.getInstance(
                        requestSequence.getObjectAt(0) as ASN1TaggedObject,
                        false,
                    )
                    userNameEncoded.string shouldBe userName
                }

                @Test
                fun `Should support non-ASCII characters`() {
                    val userName = "👩‍💻"
                    val request = AccountRequest(
                        userName = userName,
                        locale = locale,
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val requestSequence = parseRequestSequence(serialisation)
                    val userNameEncoded = ASN1UTF8String.getInstance(
                        requestSequence.getObjectAt(0) as ASN1TaggedObject,
                        false,
                    )
                    userNameEncoded.string shouldBe userName
                }
            }

            @Test
            fun `Should serialise the locale as a VisibleString`() {
                val request = AccountRequest(
                    userName = userName,
                    locale = locale,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val requestSequence = parseRequestSequence(serialisation)
                val localeEncoded =
                    ASN1Utils.getVisibleString(requestSequence.getObjectAt(1) as ASN1TaggedObject)
                localeEncoded.string shouldBe locale.normaliseString()
            }

            @Test
            fun `Should serialize the public key`() {
                val request = AccountRequest(
                    userName = userName,
                    locale = locale,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val requestSequence = parseRequestSequence(serialisation)
                val publicKeyEncoded = SubjectPublicKeyInfo.getInstance(
                    requestSequence.getObjectAt(2) as ASN1TaggedObject,
                    false,
                )
                publicKeyEncoded shouldBe keyPair.public.spkiEncode()
            }

            private fun parseRequestSequence(serialisation: ByteArray): ASN1Sequence {
                val signatureSequence = ASN1Utils.deserializeHeterogeneousSequence(serialisation)
                return DERSequence.getInstance(signatureSequence[0], false)
            }
        }

        @Nested
        inner class Signature {
            @Test
            fun `Signature should be serialised as a BIT STRING`() {
                val request = AccountRequest(
                    userName = userName,
                    locale = locale,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val signatureSequence = ASN1Utils.deserializeHeterogeneousSequence(serialisation)
                ASN1BitString.getInstance(signatureSequence[1], false) // Should not throw
            }

            @Test
            fun `Signature should be computed over the request`() {
                val request = AccountRequest(
                    userName = userName,
                    locale = locale,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val signatureSequence = ASN1Utils.deserializeHeterogeneousSequence(serialisation)
                val requestSequence = DERSequence.getInstance(signatureSequence[0], false)
                val signatureEncoded = ASN1BitString.getInstance(signatureSequence[1], false)
                RSASigning.verify(
                    signatureEncoded.bytes,
                    keyPair.public,
                    requestSequence.encoded,
                ) shouldBe true
            }
        }
    }
}
