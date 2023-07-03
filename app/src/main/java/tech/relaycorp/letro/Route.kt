package tech.relaycorp.letro

sealed class Route(val name: String) {

    object AccountConfirmation : Route("accountConfirmation")
    object AccountCreation : Route("accountCreation")
    object Contacts : Route("contacts")
    object GatewayNotInstalled : Route("gatewayNotInstalled")
    object Messages : Route("messages")
    object Notifications : Route("notifications")
    object PairingRequestSent : Route("pairingRequestSent")
    object Splash : Route("splash")
    object WaitingForAccountCreation : Route("waitingForAccountCreation")
}

fun String?.getRouteByName(): Route {
    this?.let {
        return when (it) {
            Route.AccountConfirmation.name -> Route.AccountConfirmation
            Route.AccountCreation.name -> Route.AccountCreation
            Route.Contacts.name -> Route.Contacts
            Route.GatewayNotInstalled.name -> Route.GatewayNotInstalled
            Route.Messages.name -> Route.Messages
            Route.Notifications.name -> Route.Notifications
            Route.PairingRequestSent.name -> Route.PairingRequestSent
            Route.Splash.name -> Route.Splash
            Route.WaitingForAccountCreation.name -> Route.WaitingForAccountCreation
            else -> throw IllegalArgumentException("Define the Route by the name of the Route")
        }
    }

    throw IllegalArgumentException("Define the Route by the name of the Route")
}
