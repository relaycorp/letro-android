package tech.relaycorp.letro.data

data class PairingMatchDataModel(
    val senderVeraId: String,
    val receiverVeraId: String,
    val receiverEndpointId: String,
    val receiverEndpointPublicKey: ByteArray,
)
