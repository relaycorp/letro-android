package tech.relaycorp.letro.server.messages

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVisibleString
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.utils.asn1.ASN1Exception
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.i18n.normaliseString
import java.util.Locale

class AccountCreationTest {
    val requestedUserName = "alice"
    val locale = Locale("EN", "GB")
    val assignedUserId = "alice@example.com"
    val veraidBundle = byteArrayOf(0x00)

    val nonAsciiUsername = "久美子"
    val nonAsciiDomainName = "はじめよう.みんな"

    @Nested
    inner class Deserialise {
        @Test
        fun `Serialisation should be a DER-encoded sequence`() {
            val exception = shouldThrow<MalformedAccountCreationException> {
                AccountCreation.deserialise(byteArrayOf(0x00))
            }

            exception.message shouldBe "AccountCreation should be a DER-encoded sequence"
            exception.cause should beInstanceOf<ASN1Exception>()
        }

        @Test
        fun `Sequence should have at least 4 items`() {
            val malformedSerialisation = ASN1Utils.serializeSequence(
                listOf(DERNull.INSTANCE, DERNull.INSTANCE, DERNull.INSTANCE),
                false,
            )

            val exception = shouldThrow<MalformedAccountCreationException> {
                AccountCreation.deserialise(malformedSerialisation)
            }

            exception.message shouldBe "AccountCreation SEQUENCE should have at least 4 items"
        }

        @Nested
        inner class RequestedUserName {
            @Test
            fun `Should be a DER-encoded UTF8String`() {
                val malformedSerialisation = DERSequence(
                    arrayOf(
                        DERTaggedObject(true, 0, DERNull.INSTANCE),
                        DERNull.INSTANCE,
                        DERNull.INSTANCE,
                        DERNull.INSTANCE,
                    ),
                ).encoded

                val exception = shouldThrow<MalformedAccountCreationException> {
                    AccountCreation.deserialise(malformedSerialisation)
                }

                exception.message shouldBe
                    "AccountCreation requestedUserName should be a DER-encoded UTF8String"
            }

            @Test
            fun `Should be decoded as a UTF-8 string`() {
                val serialisation = AccountCreation(
                    nonAsciiUsername,
                    locale,
                    assignedUserId,
                    veraidBundle,
                ).serialise()

                val accountCreation = AccountCreation.deserialise(serialisation)

                accountCreation.requestedUserName shouldBe nonAsciiUsername
            }
        }

        @Nested
        inner class Locale {
            @Test
            fun `Should be a DER-encoded VisibleString`() {
                val malformedSerialisation = DERSequence(
                    arrayOf(
                        DERTaggedObject(false, 0, DERUTF8String(requestedUserName)),
                        DERTaggedObject(true, 0, DERVisibleString(locale.normaliseString())),
                        DERUTF8String(assignedUserId),
                        DEROctetString(veraidBundle),
                    ),
                ).encoded

                val exception = shouldThrow<MalformedAccountCreationException> {
                    AccountCreation.deserialise(malformedSerialisation)
                }

                exception.message shouldBe
                    "AccountCreation locale should be a DER-encoded VisibleString"
            }

            @Test
            fun `Should be decoded as a Locale`() {
                val serialisation = AccountCreation(
                    requestedUserName,
                    locale,
                    assignedUserId,
                    veraidBundle,
                ).serialise()

                val accountCreation = AccountCreation.deserialise(serialisation)

                accountCreation.locale shouldBe locale
            }
        }

        @Nested
        inner class AssignedUserId {
            @Test
            fun `Should be a DER-encoded UTF8String`() {
                val malformedSerialisation = DERSequence(
                    arrayOf(
                        DERTaggedObject(false, 0, DERUTF8String(requestedUserName)),
                        DERTaggedObject(false, 1, DERVisibleString(locale.normaliseString())),
                        DERTaggedObject(true, 2, DERVisibleString(assignedUserId)),
                        DERNull.INSTANCE,
                    ),
                ).encoded

                val exception = shouldThrow<MalformedAccountCreationException> {
                    AccountCreation.deserialise(malformedSerialisation)
                }

                exception.message shouldBe
                    "AccountCreation assignedUserId should be a DER-encoded UTF8String"
            }

            @Test
            fun `Should be decoded as a UTF-8 string`() {
                val nonAsciiAssignedUserId = "$nonAsciiUsername@$nonAsciiDomainName"
                val serialisation = AccountCreation(
                    requestedUserName,
                    locale,
                    nonAsciiAssignedUserId,
                    veraidBundle,
                ).serialise()

                val accountCreation = AccountCreation.deserialise(serialisation)

                accountCreation.assignedUserId shouldBe nonAsciiAssignedUserId
            }
        }

        @Nested
        inner class VeraidBundle {
            @Test
            fun `Should be a DER-encoded OCTET STRING`() {
                val malformedSerialisation = DERSequence(
                    arrayOf(
                        DERTaggedObject(false, 0, DERUTF8String(requestedUserName)),
                        DERTaggedObject(false, 1, DERVisibleString(locale.normaliseString())),
                        DERTaggedObject(false, 2, DERVisibleString(assignedUserId)),
                        DERTaggedObject(true, 3, DERNull.INSTANCE),
                    ),
                ).encoded

                val exception = shouldThrow<MalformedAccountCreationException> {
                    AccountCreation.deserialise(malformedSerialisation)
                }

                exception.message shouldBe
                    "AccountCreation veraidBundle should be a DER-encoded OCTET STRING"
            }

            @Test
            fun `Should be decoded as a DER-encoded OCTET STRING`() {
                val serialisation = AccountCreation(
                    requestedUserName,
                    locale,
                    assignedUserId,
                    veraidBundle,
                ).serialise()

                val accountCreation = AccountCreation.deserialise(serialisation)

                accountCreation.veraidBundle shouldBe veraidBundle
            }
        }
    }

    private fun AccountCreation.serialise(): ByteArray {
        return ASN1Utils.serializeSequence(
            listOf(
                DERUTF8String(requestedUserName),
                DERVisibleString(locale.normaliseString()),
                DERUTF8String(assignedUserId),
                DEROctetString(veraidBundle),
            ),
            false,
        )
    }
}
