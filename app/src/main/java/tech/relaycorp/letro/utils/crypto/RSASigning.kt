package tech.relaycorp.letro.utils.crypto

import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

/**
 * Plain RSA signatures (without PKCS#7/CMS SignedData).
 */
internal object RSASigning {
    fun sign(plaintext: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature = makeSignature()
        signature.initSign(privateKey)
        signature.update(plaintext)
        return signature.sign()
    }

    fun verify(
        signatureSerialisation: ByteArray,
        publicKey: PublicKey,
        expectedPlaintext: ByteArray,
    ): Boolean {
        val signature = makeSignature()
        signature.initVerify(publicKey)
        signature.update(expectedPlaintext)
        return signature.verify(signatureSerialisation)
    }

    private fun makeSignature() = Signature.getInstance("SHA256withRSAandMGF1", BC_PROVIDER)
}
