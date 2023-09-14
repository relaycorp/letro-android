package tech.relaycorp.letro.ui.navigation

import tech.relaycorp.letro.contacts.ManageContactViewModel

/**
 * Class which contains all possible routes
 *
 * NOTE: the route name must end with _route suffix
 */
sealed class Route(
    val name: String,
    val showTopBar: Boolean = true,
    val isStatusBarPrimaryColor: Boolean = false,
) {

    object Splash : Route(
        name = "splash_route",
        showTopBar = false,
    )

    object Registration : Route(
        name = "registration_route",
        showTopBar = false,
    )

    object AwalaNotInstalled : Route(
        name = "awala_not_installed_route",
        showTopBar = false,
    )

    object RegistrationProcessWaiting : Route(
        name = "registration_waiting_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )

    object NoContacts : Route(
        name = "no_contacts_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )

    object WelcomeToLetro : Route(
        name = "welcome_to_letro_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )

    object PairingRequestSent : Route(
        name = "pairing_request_sent_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    ) {

        const val RECEIVER_ARGUMENT_VERA_ID = "receiver_vera_id"

        fun getRouteName(receiverVeraId: String): String {
            return "$name/$receiverVeraId"
        }
    }

    object ManageContact : Route(
        name = "manage_contact_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    ) {
        const val KEY_CURRENT_ACCOUNT_ID_ENCODED = "current_account_id_encoded"
        const val KEY_SCREEN_TYPE = "screen_type"
        const val KEY_CONTACT_ID_TO_EDIT = "contact_id"
        const val NO_ID = -1L

        fun getRouteName(
            @ManageContactViewModel.Type screenType: Int,
            currentAccountIdEncoded: String?,
            contactIdToEdit: Long = NO_ID,
        ) = "${ManageContact.name}/$currentAccountIdEncoded&$screenType&$contactIdToEdit"
    }

    object Home : Route(
        name = "home_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )
}

fun String?.toRoute(): Route {
    this?.let {
        return when {
            it.startsWith(Route.Splash.name) -> Route.Splash
            it.startsWith(Route.AwalaNotInstalled.name) -> Route.AwalaNotInstalled
            it.startsWith(Route.Registration.name) -> Route.Registration
            it.startsWith(Route.RegistrationProcessWaiting.name) -> Route.RegistrationProcessWaiting
            it.startsWith(Route.WelcomeToLetro.name) -> Route.WelcomeToLetro
            it.startsWith(Route.NoContacts.name) -> Route.NoContacts
            it.startsWith(Route.ManageContact.name) -> Route.ManageContact
            it.startsWith(Route.PairingRequestSent.name) -> Route.PairingRequestSent
            it.startsWith(Route.Home.name) -> Route.Home
            else -> throw IllegalArgumentException("Define the Route by the name of the Route $it")
        }
    }
    throw IllegalArgumentException("Define the Route by the name of the Route")
}
