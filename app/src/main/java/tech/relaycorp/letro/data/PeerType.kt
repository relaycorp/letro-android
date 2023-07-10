package tech.relaycorp.letro.data

sealed interface PeerType {
    object Public : PeerType
    object Private : PeerType
}
