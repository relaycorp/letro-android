package tech.realycorp.letro

sealed class Route(val name: String) {

    object AccountCreation : Route("accountCreation")
    object GatewayNotInstalled : Route("gatewayNotInstalled")
    object Splash : Route("splash")
}

fun String?.getRouteByName(): Route {
    this?.let {
        return when (it) {
            Route.AccountCreation.name -> Route.AccountCreation
            Route.GatewayNotInstalled.name -> Route.GatewayNotInstalled
            Route.Splash.name -> Route.Splash
            else -> throw IllegalArgumentException("Define the Route by the name of the Route")
        }
    }

    throw IllegalArgumentException("Define the Route by the name of the Route")
}
