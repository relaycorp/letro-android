package tech.realycorp.letro.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import tech.realycorp.letro.Route
import tech.realycorp.letro.ui.onboarding.gatewayNotInstalled.GatewayNotInstalledScreen

fun NavGraphBuilder.gatewayNotAvailableScreen(
    onNavigateToGooglePlayStore: () -> Unit,
) {
    composable(Route.GatewayNotInstalled.name) {
        GatewayNotInstalledScreen(onNavigateToGooglePlay = onNavigateToGooglePlayStore)
    }
}

fun NavController.navigateToGatewayNotAvailableScreen() {
    this.navigate(Route.GatewayNotInstalled.name)
}
