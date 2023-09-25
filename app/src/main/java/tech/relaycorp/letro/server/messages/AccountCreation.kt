package tech.relaycorp.letro.server.messages

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTF8String
import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.letro.utils.asn1.ASN1Exception
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.i18n.normaliseString
import tech.relaycorp.letro.utils.i18n.parseLocale
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import tech.relaycorp.veraid.pki.PkiException
import java.security.PublicKey
import java.time.ZonedDateTime
import java.util.Locale

/**
 * This message signifies that a VeraId identifier has been created.
 *
 * See: https://docs.relaycorp.tech/letro-server/account-creation#account-creation-1
 */
class AccountCreation(
    val requestedUserName: String,
    val locale: Locale,
    val assignedUserId: String,
    val veraidBundle: ByteArray,
) {
    @Throws(InvalidAccountCreationException::class)
    suspend fun validate(memberPublicKey: PublicKey) {
        val (bundle, bundleMember) = verifyBundle()

        if (memberPublicKey != bundle.memberPublicKey) {
            throw InvalidAccountCreationException(
                "Member id bundle does not have expected member key",
            )
        }

        val (assignedUserName, assignedOrg) = parseAssignedUserId()
        if (assignedUserName != bundleMember.userName) {
            throw InvalidAccountCreationException(
                "Member id bundle does not have expected user name",
            )
        }
        if (assignedOrg != bundleMember.orgName) {
            throw InvalidAccountCreationException(
                "Member id bundle does not have expected org name",
            )
        }
    }

    @Throws(InvalidAccountCreationException::class)
    private suspend fun verifyBundle(): Pair<MemberIdBundle, Member> {
        val bundle = try {
            MemberIdBundle.deserialise(veraidBundle)
        } catch (exc: PkiException) {
            throw InvalidAccountCreationException("Member id bundle is malformed", exc)
        }

        val now = ZonedDateTime.now()
        val verificationPeriod = now..now
        val bundleMember = try {
            bundle.verify(LetroOids.LETRO_VERAID_OID, verificationPeriod)
        } catch (exc: PkiException) {
            throw InvalidAccountCreationException("Member id bundle is invalid", exc)
        }
        return Pair(bundle, bundleMember)
    }

    private fun parseAssignedUserId(): Pair<String?, String> {
        val assignedUserIdParts = assignedUserId.split("@", limit = 2)
        val assignedUserName = if (assignedUserIdParts.size == 1) null else assignedUserIdParts[0]
        val assignedOrg = if (assignedUserIdParts.size == 1) {
            assignedUserIdParts[0]
        } else {
            assignedUserIdParts[1]
        }
        return Pair(assignedUserName, assignedOrg)
    }

    override fun toString(): String {
        val params = listOf(
            "requestedUserName=$requestedUserName",
            "locale=${locale.normaliseString()}",
            "assignedUserId=$assignedUserId",
        ).joinToString(", ")
        return "AccountCreation($params)"
    }

    companion object {
        @Throws(InvalidAccountCreationException::class)
        fun deserialise(serialised: ByteArray): AccountCreation {
            val accountCreationSequence = try {
                ASN1Utils.deserializeSequence(serialised)
            } catch (exc: ASN1Exception) {
                throw InvalidAccountCreationException(
                    "AccountCreation should be a DER-encoded sequence",
                    exc,
                )
            }
            if (accountCreationSequence.size() < 4) {
                throw InvalidAccountCreationException(
                    "AccountCreation SEQUENCE should have at least 4 items",
                )
            }

            val requestedUserName = decodeRequestedUserName(accountCreationSequence.getObjectAt(0))
            val locale = decodeLocale(accountCreationSequence.getObjectAt(1))
            val assignedUserId = decodeAssignedUserId(accountCreationSequence.getObjectAt(2))
            val veraidBundle = decodeVeraidBundle(accountCreationSequence.getObjectAt(3))
            return AccountCreation(requestedUserName, locale, assignedUserId, veraidBundle)
        }

        private fun decodeRequestedUserName(userNameTagged: ASN1Encodable): String {
            val requestedUserNameEncoded = try {
                ASN1UTF8String.getInstance(userNameTagged as ASN1TaggedObject, false)
            } catch (exc: RuntimeException) {
                throw InvalidAccountCreationException(
                    "AccountCreation requestedUserName should be a DER-encoded UTF8String",
                    exc,
                )
            }
            return requestedUserNameEncoded.string
        }

        private fun decodeLocale(localeTagged: ASN1Encodable): Locale {
            val localeEncoded = try {
                ASN1Utils.getVisibleString(localeTagged as ASN1TaggedObject)
            } catch (exc: RuntimeException) {
                throw InvalidAccountCreationException(
                    "AccountCreation locale should be a DER-encoded VisibleString",
                    exc,
                )
            }
            return parseLocale(localeEncoded.string)
        }

        private fun decodeAssignedUserId(assignedUserIdTagged: ASN1Encodable): String {
            val assignedUserIdEncoded = try {
                ASN1UTF8String.getInstance(assignedUserIdTagged as ASN1TaggedObject, false)
            } catch (exc: RuntimeException) {
                throw InvalidAccountCreationException(
                    "AccountCreation assignedUserId should be a DER-encoded UTF8String",
                    exc,
                )
            }
            return assignedUserIdEncoded.string
        }

        private fun decodeVeraidBundle(veraidBundleTagged: ASN1Encodable): ByteArray {
            val veraidBundleEncoded = try {
                ASN1Utils.getOctetString(veraidBundleTagged as ASN1TaggedObject)
            } catch (exc: RuntimeException) {
                throw InvalidAccountCreationException(
                    "AccountCreation veraidBundle should be a DER-encoded OCTET STRING",
                    exc,
                )
            }
            return veraidBundleEncoded.octets
        }
    }
}
