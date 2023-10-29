package tech.relaycorp.letro.utils.veraid

import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.veraid.SignatureBundle
import tech.relaycorp.veraid.SignatureException
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.security.PrivateKey
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class VeraidSignatureException(message: String, cause: Throwable) : Exception(message, cause)

typealias BundleGenerator = (
    plaintext: ByteArray,
    serviceOid: String,
    memberIdBundle: MemberIdBundle,
    signingKey: PrivateKey,
    expiryDate: ZonedDateTime,
    startDate: ZonedDateTime,
    encapsulatePlaintext: Boolean,
) -> SignatureBundle

object VeraidSignature {
    private val SIGNATURE_BUNDLE_TTL = 90.days

    var signatureBundleGenerator: BundleGenerator = SignatureBundle.Companion::generate

    @Throws(VeraidSignatureException::class)
    fun produce(
        plaintext: ByteArray,
        memberIdBundle: MemberIdBundle,
        memberPrivateKey: PrivateKey,
    ): ByteArray {
        val creationDate = ZonedDateTime.now()
        val signatureBundle = try {
            signatureBundleGenerator(
                plaintext,
                LetroOids.LETRO_VERAID_OID,
                memberIdBundle,
                memberPrivateKey,
                creationDate.plus(SIGNATURE_BUNDLE_TTL.toJavaDuration()),
                creationDate,
                true,
            )
        } catch (exc: SignatureException) {
            throw VeraidSignatureException("Failed to generate VeraId signature", exc)
        }
        return signatureBundle.serialise()
    }
}
