package tech.relaycorp.letro.utils.veraid

import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.veraid.SignatureBundle
import tech.relaycorp.veraid.SignatureBundleVerification
import tech.relaycorp.veraid.SignatureException
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.security.PrivateKey
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

typealias BundleGenerator = (
    plaintext: ByteArray,
    serviceOid: String,
    memberIdBundle: MemberIdBundle,
    signingKey: PrivateKey,
    expiryDate: ZonedDateTime,
    startDate: ZonedDateTime,
    encapsulatePlaintext: Boolean,
) -> SignatureBundle

typealias BundleDeserialiser = (serialised: ByteArray) -> SignatureBundle

object VeraidSignature {
    private val SIGNATURE_BUNDLE_TTL = 90.days

    var signatureBundleGenerator: BundleGenerator = SignatureBundle.Companion::generate
    var signatureBundleDeserialiser: BundleDeserialiser = SignatureBundle.Companion::deserialise

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

    @Throws(VeraidSignatureException::class)
    suspend fun verify(signatureBundleSerialised: ByteArray): SignatureBundleVerification {
        val signatureBundle = try {
            signatureBundleDeserialiser(signatureBundleSerialised)
        } catch (exc: SignatureException) {
            throw VeraidSignatureException("Failed to deserialise VeraId signature", exc)
        }
        val now = ZonedDateTime.now()
        return try {
            signatureBundle.verify(
                null,
                LetroOids.LETRO_VERAID_OID,
                now.minus(SIGNATURE_BUNDLE_TTL.toJavaDuration())..now,
            )
        } catch (exc: SignatureException) {
            throw VeraidSignatureException("Invalid VeraId signature", exc)
        }
    }
}
