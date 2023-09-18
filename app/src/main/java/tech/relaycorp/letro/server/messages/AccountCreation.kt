package tech.relaycorp.letro.server.messages

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTF8String
import tech.relaycorp.letro.utils.asn1.ASN1Exception
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.i18n.parseLocale
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
    companion object {
        fun deserialise(serialised: ByteArray): AccountCreation {
            val accountCreationSequence = try {
                ASN1Utils.deserializeSequence(serialised)
            } catch (exc: ASN1Exception) {
                throw MalformedAccountCreationException(
                    "AccountCreation should be a DER-encoded sequence",
                    exc,
                )
            }
            if (accountCreationSequence.size() < 4) {
                throw MalformedAccountCreationException(
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
                throw MalformedAccountCreationException(
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
                throw MalformedAccountCreationException(
                    "AccountCreation locale should be a DER-encoded VisibleString",
                    exc,
                )
            }
            return localeEncoded.string.parseLocale()
        }

        private fun decodeAssignedUserId(assignedUserIdTagged: ASN1Encodable): String {
            val assignedUserIdEncoded = try {
                ASN1UTF8String.getInstance(assignedUserIdTagged as ASN1TaggedObject, false)
            } catch (exc: RuntimeException) {
                throw MalformedAccountCreationException(
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
                throw MalformedAccountCreationException(
                    "AccountCreation veraidBundle should be a DER-encoded OCTET STRING",
                    exc,
                )
            }
            return veraidBundleEncoded.octets
        }
    }
}
