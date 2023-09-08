package tech.relaycorp.letro.ui.navigation

sealed class Route(
    val name: String,
    val showTopBar: Boolean = true,
) {

    object Splash : Route(
        name = "splash",
        showTopBar = false,
    )

    object Registration : Route(
        name = "registration",
        showTopBar = false,
    )

    object AwalaNotInstalled : Route(
        name = "awala_not_installed",
        showTopBar = false,
    )

    object RegistrationProcessWaiting : Route(
        name = "registration_waiting",
        showTopBar = true,
    )

    object NoContacts : Route(
        name = "no_contacts",
        showTopBar = true,
    )

    object WelcomeToLetro : Route(
        name = "welcome_to_letro",
        showTopBar = true
    )

    object PairingRequestSent : Route(
        name = "pairing_request_sent",
        showTopBar = true
    )

}

fun String?.toRoute(): Route {
    this?.let {
        return when (it) {
            Route.Registration.name -> Route.Registration
            Route.AwalaNotInstalled.name -> Route.AwalaNotInstalled
            Route.RegistrationProcessWaiting.name -> Route.RegistrationProcessWaiting
            Route.WelcomeToLetro.name -> Route.WelcomeToLetro
            Route.NoContacts.name -> Route.NoContacts
            Route.PairingRequestSent.name -> Route.PairingRequestSent
            Route.Splash.name -> Route.Splash
            else -> throw IllegalArgumentException("Define the Route by the name of the Route $it")
        }
    }
    throw IllegalArgumentException("Define the Route by the name of the Route")
}
