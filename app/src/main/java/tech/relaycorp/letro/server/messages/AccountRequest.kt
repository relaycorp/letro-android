package tech.relaycorp.letro.server.messages

import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVisibleString
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.RSASigning
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.i18n.normaliseString
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Locale

/**
 * This message signifies a Letro user's intention to create a VeraId identifier.
 *
 * See https://docs.relaycorp.tech/letro-server/account-creation#account-creation-request
 */
class AccountRequest(
    val userName: String,
    val locale: Locale,
    val veraidMemberPublicKey: PublicKey,
) {
    fun serialise(veraidMemberPrivateKey: PrivateKey): ByteArray {
        val requestEncoded = ASN1Utils.makeSequence(
            listOf(
                DERUTF8String(userName),
                encodeLocale(),
                veraidMemberPublicKey.spkiEncode(),
            ),
            explicitTagging = false,
        )
        val signature = RSASigning.sign(requestEncoded.encoded, veraidMemberPrivateKey)
        return ASN1Utils.serializeSequence(listOf(requestEncoded, DERBitString(signature)), false)
    }

    private fun encodeLocale() = DERVisibleString(locale.normaliseString())
}
