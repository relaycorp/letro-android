package tech.relaycorp.letro.utils.awala

import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import java.lang.Exception

suspend fun loadNonNullPublicFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading FirstPartyEndpoint is null")
    return FirstPartyEndpoint.load(nodeId) ?: throw Exception("FirstPartyEndpoint couldn't be loaded")
}

suspend fun loadNonNullPublicThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading ThirdPartyEndpoint is null")
    return PublicThirdPartyEndpoint.load(nodeId) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
}

suspend fun loadNonNullPrivateThirdPartyEndpoint(senderNodeId: String, recipientNodeId: String): PrivateThirdPartyEndpoint {
    return PrivateThirdPartyEndpoint.load(
        thirdPartyAddress = recipientNodeId,
        firstPartyAddress = senderNodeId,
    ) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
}
