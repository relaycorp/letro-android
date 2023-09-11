package tech.relaycorp.letro.utils.navigation

import androidx.navigation.NavController
import tech.relaycorp.letro.ui.navigation.Route

fun NavController.navigateWithPoppingAllBackStack(route: Route) {
    navigate(route.name) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
