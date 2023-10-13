package tech.relaycorp.letro.account.registration.server.dto

data class PublicKeyImportData(
    val publicKeyImportToken: String,
    val publicKey: String,
)
