package tech.relaycorp.letro.awala.message

import org.bouncycastle.asn1.DERUTF8String
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.veraid.VeraidSignature
import tech.relaycorp.letro.utils.veraid.VeraidSignatureException
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.security.PrivateKey
import java.security.PublicKey

class ContactPairingRequest(
    private val requesterAwalaEndpointPublicKey: PublicKey,
    private val contactVeraidId: String,
) {
    @Throws(VeraidSignatureException::class)
    fun serialise(veraidBundle: MemberIdBundle, veraidPrivateKey: PrivateKey): ByteArray {
        val requestSerialised = ASN1Utils.serializeSequence(
            listOf(requesterAwalaEndpointPublicKey.spkiEncode(), DERUTF8String(contactVeraidId)),
            false,
        )
        return VeraidSignature.produce(requestSerialised, veraidBundle, veraidPrivateKey)
    }
}
