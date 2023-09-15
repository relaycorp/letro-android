package tech.relaycorp.letro.server.messages

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.beUpperCase
import io.kotest.matchers.string.match
import org.bouncycastle.asn1.ASN1BitString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTF8String
import org.bouncycastle.asn1.ASN1VisibleString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.crypto.generateRSAKeyPair
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.RSASigning
import java.util.Locale

const val USER_NAME = "alice"
val LOCALE = Locale("EN", "GB")

val keyPair = generateRSAKeyPair()

class AccountRequestTest {
    @Nested
    inner class Serialize {
        @Nested
        inner class RequestSerialisation {
            @Nested
            inner class UserName {
                @Test
                fun `Should serialise the user name as UTF8String`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = LOCALE,
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val requestSequence = parseRequestSequence(serialisation)
                    val userNameEncoded = ASN1UTF8String.getInstance(
                        requestSequence.getObjectAt(0) as ASN1TaggedObject,
                        false,
                    )
                    userNameEncoded.string shouldBe USER_NAME
                }

                @Test
                fun `Should support non-ASCII characters`() {
                    val userName = "👩‍💻"
                    val request = AccountRequest(
                        userName = userName,
                        locale = LOCALE,
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

            @Nested
            inner class LocaleSerialisation {
                @Test
                fun `Should serialise the language as a string`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = LOCALE,
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val locale = parseLocaleSequence(serialisation)
                    locale.string.split("-")[0] shouldBe LOCALE.language.lowercase()
                }

                @Test
                fun `Should serialise the country as a lowercase string`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = LOCALE,
                        veraidMemberPublicKey = keyPair.public,
                    )
                    request.locale.country should beUpperCase()

                    val serialisation = request.serialise(keyPair.private)

                    val locale = parseLocaleSequence(serialisation)
                    val countryCode = locale.string.split("-")[1]
                    countryCode shouldBe LOCALE.country.lowercase()
                }

                @Test
                fun `Should result in empty string if language is missing`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = Locale("", LOCALE.country),
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val locale = parseLocaleSequence(serialisation)
                    locale.string shouldBe ""
                }

                @Test
                fun `Should only serialise the language if country is missing`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = Locale(LOCALE.language, ""),
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val locale = parseLocaleSequence(serialisation)
                    locale.string shouldBe LOCALE.language
                }

                @Test
                fun `Should not serialise the variant`() {
                    val request = AccountRequest(
                        userName = USER_NAME,
                        locale = Locale(LOCALE.language, LOCALE.country, "Oxford"),
                        veraidMemberPublicKey = keyPair.public,
                    )

                    val serialisation = request.serialise(keyPair.private)

                    val locale = parseLocaleSequence(serialisation)
                    locale.string should match(Regex("[a-z]{2}-[a-z]{2}"))
                }

                private fun parseLocaleSequence(serialisation: ByteArray): ASN1VisibleString {
                    val requestSequence = parseRequestSequence(serialisation)
                    return ASN1Utils.getVisibleString(requestSequence.getObjectAt(1) as ASN1TaggedObject)
                }
            }

            @Test
            fun `Should serialize the public key`() {
                val request = AccountRequest(
                    userName = USER_NAME,
                    locale = LOCALE,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val requestSequence = parseRequestSequence(serialisation)
                val publicKeyEncoded = SubjectPublicKeyInfo.getInstance(
                    requestSequence.getObjectAt(2) as ASN1TaggedObject,
                    false,
                )
                publicKeyEncoded.encoded shouldBe keyPair.public.encoded
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
                    userName = USER_NAME,
                    locale = LOCALE,
                    veraidMemberPublicKey = keyPair.public,
                )

                val serialisation = request.serialise(keyPair.private)

                val signatureSequence = ASN1Utils.deserializeHeterogeneousSequence(serialisation)
                ASN1BitString.getInstance(signatureSequence[1], false) // Should not throw
            }

            @Test
            fun `Signature should be computed over the request`() {
                val request = AccountRequest(
                    userName = USER_NAME,
                    locale = LOCALE,
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
