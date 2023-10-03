package tech.relaycorp.letro.utils.compose.navigation

import androidx.navigation.NavController
import tech.relaycorp.letro.ui.navigation.Route

fun NavController.navigateWithPoppingAllBackStack(route: Route) {
    navigate(route.name) {
        popUpTo(0) {
            inclusive = true
        }
    }
}

fun NavController.navigateSingleTop(route: Route) {
    navigate(route.name) {
        launchSingleTop = true
    }
}

fun NavController.navigateWithDropCurrentScreen(route: String) {
    navigate(route) {
        popBackStack()
    }
}
