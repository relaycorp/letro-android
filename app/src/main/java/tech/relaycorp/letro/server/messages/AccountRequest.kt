package tech.relaycorp.letro.server.messages

import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.RSASigning
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Locale

class AccountRequest(
    val userName: String,
    val locale: Locale,
    val veraidMemberPublicKey: PublicKey,
) {
    fun serialise(veraidMemberPrivateKey: PrivateKey): ByteArray {
        val requestEncoded = ASN1Utils.makeSequence(
            listOf(
                DERUTF8String(userName),
                serialiseLocale(),
                SubjectPublicKeyInfo.getInstance(veraidMemberPublicKey.encoded),
            ),
            explicitTagging = false,
        )
        val signature = RSASigning.sign(requestEncoded.encoded, veraidMemberPrivateKey)
        return ASN1Utils.serializeSequence(listOf(requestEncoded, DERBitString(signature)), false)
    }

    private fun serialiseLocale(): DERVisibleString {
        val languageCode = locale.language.lowercase()
        val countryCode = locale.country.lowercase()
        val localeNormalised = if (languageCode.isEmpty()) {
            ""
        } else if (countryCode.isEmpty()) {
            languageCode
        } else {
            "$languageCode-$countryCode"
        }
        return DERVisibleString(localeNormalised)
    }
}
