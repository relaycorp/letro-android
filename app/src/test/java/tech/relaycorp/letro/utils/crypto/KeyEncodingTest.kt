package tech.relaycorp.letro.utils.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.ASN1BitString
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.testing.crypto.generateRSAKeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey

class KeyEncodingTest {
    @Nested
    inner class PublicKeySpkiEncoding {
        val rsaPublicKey: PublicKey = generateRSAKeyPair().public

        @Test
        fun `Non-RSA keys should be refused`() {
            val keyGen = KeyPairGenerator.getInstance("DSA", BC_PROVIDER)
            keyGen.initialize(1024)
            val keyPair = keyGen.generateKeyPair()

            val exception = shouldThrow<IllegalArgumentException> {
                keyPair.public.spkiEncode()
            }

            exception.message shouldBe "Only RSA keys are supported"
        }

        @Nested
        inner class Algorithm {
            @Test
            fun `Algorithm should be RSA-PSS`() {
                val encoding = rsaPublicKey.spkiEncode()

                encoding.algorithm.algorithm shouldBe PKCSObjectIdentifiers.id_RSASSA_PSS
            }

            @Nested
            inner class Params {
                @Test
                fun `Hash should be SHA-256`() {
                    val encoding = rsaPublicKey.spkiEncode()

                    val parameters = encoding.algorithm.parameters as RSASSAPSSparams
                    parameters.hashAlgorithm.algorithm shouldBe NISTObjectIdentifiers.id_sha256
                    parameters.hashAlgorithm.parameters shouldBe DERNull.INSTANCE
                }

                @Test
                fun `MGF should be MGF1 with SHA-256`() {
                    val encoding = rsaPublicKey.spkiEncode()

                    val parameters = encoding.algorithm.parameters as RSASSAPSSparams
                    parameters.maskGenAlgorithm.algorithm shouldBe PKCSObjectIdentifiers.id_mgf1
                    val mgfAlgorithmParams = parameters.maskGenAlgorithm.parameters
                    mgfAlgorithmParams shouldBe AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)
                }

                @Test
                fun `Salt length should be 32`() {
                    val encoding = rsaPublicKey.spkiEncode()

                    val parameters = encoding.algorithm.parameters as RSASSAPSSparams
                    parameters.saltLength.intValueExact() shouldBe 32
                }

                @Test
                fun `Trailer field should be 1`() {
                    val encoding = rsaPublicKey.spkiEncode()

                    val parameters = encoding.algorithm.parameters as RSASSAPSSparams
                    parameters.trailerField.intValueExact() shouldBe 1
                }
            }
        }

        @Test
        fun `Key should be just the key without the algorithm`() {
            val encoding = rsaPublicKey.spkiEncode()

            val keyWrapperEncoded = DERSequence.getInstance(rsaPublicKey.encoded)
            val keyEncoded = ASN1BitString.getInstance(keyWrapperEncoded.getObjectAt(1))
            encoding.publicKeyData shouldBe keyEncoded
        }
    }
}
