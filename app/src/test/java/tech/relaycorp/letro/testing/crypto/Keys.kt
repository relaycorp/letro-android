package tech.relaycorp.letro.testing.crypto

import java.security.KeyPair
import java.security.KeyPairGenerator

fun generateRSAKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(2048)
    return keyGen.generateKeyPair()
}
