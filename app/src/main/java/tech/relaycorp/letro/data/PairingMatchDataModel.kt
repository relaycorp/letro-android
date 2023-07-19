package tech.relaycorp.letro.data

data class PairingMatchDataModel(
    val requesterVeraId: String,
    val contactVeraId: String,
    val contactEndpointId: String,
    val contactEndpointPublicKey: ByteArray
)