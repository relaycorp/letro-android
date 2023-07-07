package tech.relaycorp.letro.utility

import androidx.navigation.NavController
import tech.relaycorp.letro.Route

fun NavController.navigateWithPoppingAllBackStack(route: Route) {
    navigate(route.name) {
        popUpTo(0) {
            inclusive = true
        }
    }
}