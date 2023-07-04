package tech.relaycorp.letro.ui.main

import tech.relaycorp.letro.Route

sealed class TopBarUIState(
    val showStatusBar: Boolean,
    val showTopBar: Boolean,
    val showAccountName: Boolean,
    val showTabs: Boolean,
)

object Splash : TopBarUIState(
    showStatusBar = false,
    showTopBar = false,
    showAccountName = false,
    showTabs = false,
)

object AccountCreation : TopBarUIState(
    showStatusBar = false,
    showTopBar = false,
    showAccountName = false,
    showTabs = false,
)

object AccountConfirmation : TopBarUIState(
    showStatusBar = true,
    showTopBar = true,
    showAccountName = true,
    showTabs = false,
)

object UseExistingAccount : TopBarUIState(
    showStatusBar = true,
    showTopBar = true,
    showAccountName = false,
    showTabs = false,
)

object WaitingForAccountCreation : TopBarUIState(
    showStatusBar = true,
    showTopBar = true,
    showAccountName = false,
    showTabs = false,
)

object GatewayNotInstalled : TopBarUIState(
    showStatusBar = true,
    showTopBar = true,
    showAccountName = false,
    showTabs = false,
)

object PairingRequestSent : TopBarUIState(
    showStatusBar = true,
    showTopBar = true,
    showAccountName = false,
    showTabs = false,
)

fun getTopBarUIState(route: Route): TopBarUIState = when (route) {
    Route.AccountCreation -> AccountCreation
    Route.AccountConfirmation -> AccountConfirmation
    Route.GatewayNotInstalled -> GatewayNotInstalled
    Route.Splash -> Splash
    Route.UseExistingAccount -> UseExistingAccount
    Route.WaitingForAccountCreation -> WaitingForAccountCreation
    else -> throw IllegalArgumentException("Define the TopBarUIState by the name of the Route")
}
