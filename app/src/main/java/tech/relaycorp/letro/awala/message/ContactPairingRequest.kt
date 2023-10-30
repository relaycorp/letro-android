package tech.relaycorp.letro.awala.message

import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTF8String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import tech.relaycorp.letro.utils.asn1.ASN1Utils
import tech.relaycorp.letro.utils.veraid.VeraidSignatureException
import tech.relaycorp.letro.utils.veraid.VeraidSignatureProcessor
import tech.relaycorp.veraid.pki.MemberIdBundle
import java.security.PrivateKey

class ContactPairingRequest(
    val requesterEndpointPublicKey: ByteArray,
    val contactVeraidId: String,
) {
    @Throws(VeraidSignatureException::class)
    fun serialise(veraidBundle: MemberIdBundle, veraidPrivateKey: PrivateKey): ByteArray {
        val requestSerialised = ASN1Utils.serializeSequence(
            listOf(
                SubjectPublicKeyInfo.getInstance(requesterEndpointPublicKey),
                DERUTF8String(contactVeraidId),
            ),
            false,
        )
        return veraidSignatureProcessor.produce(requestSerialised, veraidBundle, veraidPrivateKey)
    }

    companion object {
        var veraidSignatureProcessor = VeraidSignatureProcessor()

        @Throws(InvalidPairingRequestException::class)
        suspend fun deserialise(signatureBundleSerialised: ByteArray): Pair<String, ContactPairingRequest> {
            val (requestSerialised, member) = try {
                veraidSignatureProcessor.verify(signatureBundleSerialised)
            } catch (exc: VeraidSignatureException) {
                throw InvalidPairingRequestException("Invalid VeraId signature", exc)
            }

            if (member.userName == null) {
                throw InvalidPairingRequestException("Signer is a VeraId org bot")
            }
            val memberUserId = "${member.userName}@${member.orgName}"

            val request = deserialiseRequest(requestSerialised)

            return Pair(memberUserId, request)
        }

        private fun deserialiseRequest(requestSerialised: ByteArray): ContactPairingRequest {
            val requestSequence = ASN1Utils.deserializeSequence(requestSerialised)
            if (requestSequence.size() < 2) {
                throw InvalidPairingRequestException(
                    "ContactPairingRequest sequence contains fewer than 2 items",
                )
            }

            val requesterEndpointPublicKey = try {
                SubjectPublicKeyInfo.getInstance(
                    requestSequence.getObjectAt(0) as ASN1TaggedObject?,
                    false,
                ).encoded
            } catch (exc: IllegalStateException) {
                throw InvalidPairingRequestException(
                    "Requester endpoint public key is not a SubjectPublicKeyInfo",
                    exc,
                )
            }
            val contactVeraidId = ASN1UTF8String.getInstance(
                requestSequence.getObjectAt(1) as ASN1TaggedObject?,
                false,
            ).string
            return ContactPairingRequest(requesterEndpointPublicKey, contactVeraidId)
        }
    }
}

class InvalidPairingRequestException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
