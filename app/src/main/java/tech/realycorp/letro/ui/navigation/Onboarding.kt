package tech.realycorp.letro.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

private val OnboardingGraphRoutePattern = "onboarding"

//fun NavGraphBuilder.onboardingGraph(navController: NavController) {
//    navigation(
//        startDestination = OnboardingGraphRoutePattern,
//        route = OnboardingGraphRoutePattern,
//    )
//}
fun NavGraphBuilder.gatewayNotAvailableScreen(
    onNavigateToGooglePlayStore: () -> Unit,
) {
    composable("gatewayNotAvailable") {

    }
}

fun NavController.navigateToGatewayNotAvailableScreen() {
    this.navigate("gatewayNotAvailable")
}


