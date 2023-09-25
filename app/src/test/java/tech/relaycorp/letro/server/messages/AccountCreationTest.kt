package tech.relaycorp.letro.server.messages

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVisibleString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.crypto.generateRSAKeyPair
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_ID
import tech.relaycorp.letro.testing.veraid.VERAID_MEMBER_KEY_PAIR
import tech.relaycorp.letro.testing.veraid.VERAID_ORG_NAME
import tech.relaycorp.letro.testing.veraid.VERAID_USER_NAME
import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.letro.utils.asn1.ASN1Exception
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.i18n.normaliseString
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import tech.relaycorp.veraid.pki.PkiException
import java.security.PublicKey
import java.time.ZonedDateTime
import java.util.Locale

@MockKExtension.ConfirmVerification
@MockKExtension.CheckUnnecessaryStub
class AccountCreationTest {
    val requestedUserName = VERAID_USER_NAME
    val locale = Locale("EN", "GB")
    val assignedUserId = VERAID_MEMBER_ID
    val veraidBundle = "the bundle".toByteArray()

    @Nested
    inner class Validate {
        private val accountCreation = AccountCreation(
            requestedUserName,
            locale,
            assignedUserId,
            veraidBundle,
        )
        private val veraidMember = Member(VERAID_ORG_NAME, VERAID_USER_NAME)
        private val memberPublicKey = VERAID_MEMBER_KEY_PAIR.public

        @AfterEach
        fun clearMocks() {
            unmockkObject(MemberIdBundle)
        }

        @Test
        fun `Malformed bundles should be refused`() = runTest {
            mockkObject(MemberIdBundle)
            val pkiException = PkiException("Whoops")
            every { MemberIdBundle.deserialise(any()) } throws pkiException

            val exception = shouldThrow<InvalidAccountCreationException> {
                accountCreation.validate(memberPublicKey)
            }

            exception.message shouldBe "Member id bundle is malformed"
            exception.cause shouldBe pkiException
        }

        @Test
        fun `Should verify bundle against Letro service OID`() = runTest {
            val mockBundle = mockMemberIdBundle(veraidMember, memberPublicKey)

            accountCreation.validate(memberPublicKey)

            coVerify { mockBundle.verify(LetroOids.LETRO_VERAID_OID, any()) }
        }

        @Test
        fun `Should be valid at the current time`() = runTest {
            val mockBundle = mockMemberIdBundle(veraidMember, memberPublicKey)
            val timeBefore = ZonedDateTime.now()

            accountCreation.validate(memberPublicKey)

            val timeAfterwards = ZonedDateTime.now()
            coVerify {
                mockBundle.verify(
                    any(),
                    match {
                        timeBefore <= it.start &&
                            it.endInclusive <= timeAfterwards &&
                            it.start == it.endInclusive
                    },
                )
            }
        }

        @Test
        fun `Member public key should match expected one`() = runTest {
            val differentKeyPair = generateRSAKeyPair()
            mockMemberIdBundle(veraidMember, differentKeyPair.public)

            val exception = shouldThrow<InvalidAccountCreationException> {
                accountCreation.validate(memberPublicKey)
            }

            exception.message shouldBe "Member id bundle does not have expected member key"
        }

        @Test
        fun `Bundle member user name should match that of assigned id`() = runTest {
            val differentMember = veraidMember.copy(userName = "not-${veraidMember.userName}")
            mockMemberIdBundle(differentMember, memberPublicKey)

            val exception = shouldThrow<InvalidAccountCreationException> {
                accountCreation.validate(memberPublicKey)
            }

            exception.message shouldBe "Member id bundle does not have expected user name"
        }

        @Test
        fun `Bundle member user name should be absent if assigned id is for bot`() = runTest {
            val differentMember = veraidMember.copy(userName = null)
            mockMemberIdBundle(differentMember, memberPublicKey)
            val botAccountCreation = AccountCreation(
                requestedUserName,
                locale,
                VERAID_ORG_NAME, // Just the domain name
                veraidBundle,
            )

            botAccountCreation.validate(memberPublicKey)
        }

        @Test
        fun `Bundle member org should match that of assigned id`() = runTest {
            val differentMember = veraidMember.copy(orgName = "not-${veraidMember.orgName}")
            mockMemberIdBundle(differentMember, memberPublicKey)

            val exception = shouldThrow<InvalidAccountCreationException> {
                accountCreation.validate(memberPublicKey)
            }

            exception.message shouldBe "Member id bundle does not have expected org name"
        }

        @Test
        fun `Validation error should be wrapped`() = runTest {
            val mockBundle = mockMemberIdBundle(veraidMember, memberPublicKey)
            val exception = PkiException("Something went wrong")
            coEvery { mockBundle.verify(any(), any()) } throws exception

            val wrappedException = shouldThrow<InvalidAccountCreationException> {
                accountCreation.validate(memberPublicKey)
            }

            wrappedException.message shouldBe "Member id bundle is invalid"
            wrappedException.cause shouldBe exception
        }

        private fun mockMemberIdBundle(member: Member, publicKey: PublicKey): MemberIdBundle {
            val mockBundle = mockk<MemberIdBundle>()
            coEvery { mockBundle.verify(any(), any()) } returns member
            every { mockBundle.memberPublicKey } returns publicKey

            mockkObject(MemberIdBundle)
            every { MemberIdBundle.deserialise(any()) } returns mockBundle

            return mockBundle
        }
    }

    @Nested
    inner class Deserialise {
        val nonAsciiUsername = "久美子"
        val nonAsciiDomainName = "はじめよう.みんな"

        @Test
        fun `Serialisation should be a DER-encoded sequence`() {
            val exception = shouldThrow<InvalidAccountCreationException> {
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

            val exception = shouldThrow<InvalidAccountCreationException> {
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

                val exception = shouldThrow<InvalidAccountCreationException> {
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

                val exception = shouldThrow<InvalidAccountCreationException> {
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

                val exception = shouldThrow<InvalidAccountCreationException> {
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

                val exception = shouldThrow<InvalidAccountCreationException> {
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

    @Nested
    inner class ToString {
        @Test
        fun `Should include requested user name, locale and assigned user id`() {
            val accountCreation = AccountCreation(
                requestedUserName,
                locale,
                assignedUserId,
                veraidBundle,
            )

            val localeNormalised = locale.normaliseString()
            val params = listOf(
                "requestedUserName=$requestedUserName",
                "locale=$localeNormalised",
                "assignedUserId=$assignedUserId",
            ).joinToString(", ")
            accountCreation.toString() shouldBe "AccountCreation($params)"
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
