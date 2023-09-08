package tech.relaycorp.letro.utils.awala

import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import java.lang.Exception

suspend fun loadNonNullFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading FirstPartyEndpoint is null")
    return FirstPartyEndpoint.load(nodeId) ?: throw Exception("FirstPartyEndpoint couldn't be loaded")
}

suspend fun loadNonNullThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading ThirdPartyEndpoint is null")
    return PublicThirdPartyEndpoint.load(nodeId) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
}
