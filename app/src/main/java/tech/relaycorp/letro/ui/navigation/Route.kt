package tech.relaycorp.letro.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class Route(
    val name: String,
    val showStatusBar: Boolean = true,
    val showTopBar: Boolean = true,
    val isTopBarContainerColorPrimary: Boolean = true,
    val showAccountNameAndActions: Boolean = true,
    val showTabs: Boolean = true,
    val floatingActionButtonFeatures: FloatingActionButtonFeatures? = null,
) {

    object AccountConfirmation : Route(
        name = "accountConfirmation",
        showTabs = false,
    )

    object AccountCreation : Route(
        name = "accountCreation",
        showStatusBar = false,
        showTopBar = false,
        showTabs = false,
    )

    object Contacts : Route(name = "contacts")

    object GatewayNotInstalled : Route(
        name = "gatewayNotInstalled",
        showStatusBar = false,
        showTopBar = true,
        isTopBarContainerColorPrimary = false,
        showAccountNameAndActions = false,
        showTabs = false,
    )

    object Conversations : Route(
        name = "conversations",
        floatingActionButtonFeatures = FloatingActionButtonFeatures(
            iconResource = R.drawable.pencil,
            contentDescriptionResource = R.string.general_start_conversation,
            routeToNavigateTo = NewMessage,
        ),
    )

    object Messages : Route(name = "messages")

    object NewMessage : Route(
        name = "newMessage",
        showTopBar = false,
    )

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
        showAccountNameAndActions = false,
        showTabs = false,
    )

    object UseExistingAccount : Route(
        name = "useExistingAccount",
        showAccountNameAndActions = false,
        showTabs = false,
    )

    object WaitingForAccountCreation : Route(
        name = "waitingForAccountCreation",
        showStatusBar = true,
        showTopBar = true,
        showAccountNameAndActions = true,
        showTabs = false,
    )
}

data class FloatingActionButtonFeatures(
    @DrawableRes val iconResource: Int,
    @StringRes val contentDescriptionResource: Int,
    val routeToNavigateTo: Route,
)

fun String?.getRouteByName(): Route {
    this?.let {
        return when (it) {
            Route.AccountConfirmation.name -> Route.AccountConfirmation
            Route.AccountCreation.name -> Route.AccountCreation
            Route.Contacts.name -> Route.Contacts
            Route.Conversations.name -> Route.Conversations
            Route.GatewayNotInstalled.name -> Route.GatewayNotInstalled
            Route.Messages.name -> Route.Messages
            Route.NewMessage.name -> Route.NewMessage
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
