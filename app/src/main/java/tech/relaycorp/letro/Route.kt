package tech.relaycorp.letro

sealed class Route(
    val name: String,
    val showStatusBar: Boolean = true,
    val showTopBar: Boolean = true,
    val showAccountName: Boolean = true,
    val showTabs: Boolean = true,
) {

    object AccountConfirmation : Route(
        name = "accountConfirmation",
        showTabs = false,
    )

    object AccountCreation : Route(
        name = "accountCreation",
        showTabs = false,
    )

    object Contacts : Route(name = "contacts")
    object GatewayNotInstalled : Route(
        name = "gatewayNotInstalled",
        showStatusBar = false,
        showTopBar = false, // TODO Maybe should be true
        showAccountName = false,
        showTabs = false,
    )

    object Messages : Route(name = "messages")
    object Notifications : Route(name = "notifications")
    object PairWithPeople : Route(
        name = "pairWithPeople",
        showTabs = false,
    )

    object PairingRequestSent : Route(
        name = "pairingRequestSent",
        showTabs = false,
    )

    object Splash : Route(
        name = "splash",
        showStatusBar = false,
        showTopBar = false,
        showAccountName = false,
        showTabs = false,
    )

    object UseExistingAccount : Route(
        name = "useExistingAccount",
        showAccountName = false,
        showTabs = false,
    )

    object WaitingForAccountCreation : Route(
        name = "waitingForAccountCreation",
        showStatusBar = true,
        showTopBar = true,
        showAccountName = true,
        showTabs = false,
    )
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
            Route.PairWithPeople.name -> Route.PairWithPeople
            Route.PairingRequestSent.name -> Route.PairingRequestSent
            Route.Splash.name -> Route.Splash
            Route.UseExistingAccount.name -> Route.UseExistingAccount
            Route.WaitingForAccountCreation.name -> Route.WaitingForAccountCreation
            else -> throw IllegalArgumentException("Define the Route by the name of the Route")
        }
    }

    throw IllegalArgumentException("Define the Route by the name of the Route")
}
