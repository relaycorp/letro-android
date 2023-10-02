package tech.relaycorp.letro.ui.navigation

import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel

/**
 * Class which contains all possible routes
 *
 * NOTE: the route name must end with _route suffix
 */
sealed class Route(
    val name: String,
    val showTopBar: Boolean = true,
    val isStatusBarVisible: Boolean = true,
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
        isStatusBarVisible = false,
    )

    object AwalaInitializing : Route(
        name = "awala_initializing_route",
        showTopBar = false,
        isStatusBarPrimaryColor = true,
    )

    object AwalaInitializationError : Route(
        name = "awala_initialization_error",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
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

    object CreateNewMessage : Route(
        name = "create_new_message_route",
        showTopBar = false,
        isStatusBarPrimaryColor = false,
    ) {
        const val KEY_SCREEN_TYPE = "screen_type"
        const val KEY_CONVERSATION_ID = "conversation_id"

        fun getRouteName(
            @ComposeNewMessageViewModel.ScreenType screenType: Int,
            conversationId: String? = null,
        ) =
            "${CreateNewMessage.name}?" +
                "$KEY_SCREEN_TYPE=$screenType" +
                if (conversationId != null) "&$KEY_CONVERSATION_ID=$conversationId" else ""
    }

    object Conversation : Route(
        name = "conversation_route",
        showTopBar = false,
        isStatusBarPrimaryColor = false,
    ) {

        const val KEY_CONVERSATION_ID = "conversation_id"

        fun getRouteName(conversationId: String) =
            "${Conversation.name}/$conversationId"
    }
}

fun String?.toRoute(): Route {
    this?.let {
        return when {
            it.startsWith(Route.Splash.name) -> Route.Splash
            it.startsWith(Route.AwalaNotInstalled.name) -> Route.AwalaNotInstalled
            it.startsWith(Route.AwalaInitializing.name) -> Route.AwalaInitializing
            it.startsWith(Route.AwalaInitializationError.name) -> Route.AwalaInitializationError
            it.startsWith(Route.Registration.name) -> Route.Registration
            it.startsWith(Route.RegistrationProcessWaiting.name) -> Route.RegistrationProcessWaiting
            it.startsWith(Route.WelcomeToLetro.name) -> Route.WelcomeToLetro
            it.startsWith(Route.NoContacts.name) -> Route.NoContacts
            it.startsWith(Route.ManageContact.name) -> Route.ManageContact
            it.startsWith(Route.Home.name) -> Route.Home
            it.startsWith(Route.CreateNewMessage.name) -> Route.CreateNewMessage
            it.startsWith(Route.Conversation.name) -> Route.Conversation
            else -> throw IllegalArgumentException("Define the Route by the name of the Route $it")
        }
    }
    throw IllegalArgumentException("Define the Route by the name of the Route")
}
