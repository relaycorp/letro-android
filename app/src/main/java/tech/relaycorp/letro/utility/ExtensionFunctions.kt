package tech.relaycorp.letro.utility

import androidx.navigation.NavController
import tech.relaycorp.awaladroid.endpoint.Endpoint
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.letro.ui.navigation.Route
import java.lang.Exception

fun NavController.navigateWithPoppingAllBackStack(route: Route) {
    navigate(route.name) {
        popUpTo(0) {
            inclusive = true
        }
    }
}

suspend fun loadNonNullFirstPartyEndpoint(nodeId: String?): FirstPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading FirstPartyEndpoint is null")
    return FirstPartyEndpoint.load(nodeId) ?: throw Exception("FirstPartyEndpoint couldn't be loaded")
}

suspend fun loadNonNullThirdPartyEndpoint(nodeId: String?): PublicThirdPartyEndpoint {
    if (nodeId == null) throw Exception("nodeId for loading ThirdPartyEndpoint is null")
    return PublicThirdPartyEndpoint.load(nodeId) ?: throw Exception("ThirdPartyEndpoint couldn't be loaded")
}
