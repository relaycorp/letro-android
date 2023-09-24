@file:JvmName("KeyEncoding")

package tech.relaycorp.letro.utils.crypto

import org.bouncycastle.asn1.ASN1BitString
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec

private val rsaPssSha256Mgf1Algorithm = AlgorithmIdentifier(
    PKCSObjectIdentifiers.id_RSASSA_PSS,
    RSASSAPSSparams(
        AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE),
        AlgorithmIdentifier(
            PKCSObjectIdentifiers.id_mgf1,
            AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
        ),
        ASN1Integer(32),
        ASN1Integer(1),
    ),
)

/**
 * Encode the public key as a SubjectPublicKeyInfo structure with the RSA-PSS algorithm.
 *
 * Otherwise it'd be encoded as an RSA key (no padding specified), which isn't supported by
 * the server (which uses the Node.js crypto module).
 */
fun PublicKey.spkiEncode(): SubjectPublicKeyInfo {
    if (algorithm != "RSA") {
        throw IllegalArgumentException("Only RSA keys are supported")
    }

    val keyWrapperEncoded = DERSequence.getInstance(encoded)
    val keyEncoded = ASN1BitString.getInstance(keyWrapperEncoded.getObjectAt(1))
    return SubjectPublicKeyInfo(rsaPssSha256Mgf1Algorithm, keyEncoded.bytes)
}

@Throws(IllegalArgumentException::class)
private fun ByteArray.deserialiseRsaPrivateKey(): RSAPrivateCrtKey {
    val privateKeySpec = PKCS8EncodedKeySpec(this)
    val keyFactory = KeyFactory.getInstance("RSA", BC_PROVIDER)
    return try {
        keyFactory.generatePrivate(privateKeySpec) as RSAPrivateCrtKey
    } catch (exc: InvalidKeySpecException) {
        throw IllegalArgumentException("Only RSA keys are supported", exc)
    }
}

@Throws(IllegalArgumentException::class)
fun ByteArray.deserialiseKeyPair(): KeyPair {
    val privateKey = this.deserialiseRsaPrivateKey()
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec = RSAPublicKeySpec(privateKey.modulus, privateKey.publicExponent)
    val publicKey = keyFactory.generatePublic(publicKeySpec)
    return KeyPair(publicKey, privateKey)
}
