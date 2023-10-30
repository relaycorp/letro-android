package tech.relaycorp.letro.ui.navigation

import androidx.annotation.IntDef
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel
import tech.relaycorp.letro.utils.ext.encodeToUTF
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank

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

    object Registration : Route(
        name = "registration_route",
        showTopBar = false,
    ) {

        const val WITH_BACK_BUTTON = "with_back_button"

        fun getRouteName(
            withBackButton: Boolean,
        ) = "${Registration.name}?$WITH_BACK_BUTTON=$withBackButton"
    }

    object UseExistingAccount : Route(
        name = "use_existing_acccount_route",
        showTopBar = false,
        isStatusBarPrimaryColor = true,
    ) {

        const val DOMAIN_ENCODED = "domain"
        const val AWALA_ENDPOINT_ENCODED = "awala_endpoint"
        const val TOKEN_ENCODED = "token"

        fun getRouteName(
            domain: String = "",
            awalaEndpoint: String = "",
            token: String = "",
        ) = "${UseExistingAccount.name}?" +
            "$DOMAIN_ENCODED=${domain.encodeToUTF()}" +
            "&$AWALA_ENDPOINT_ENCODED=${awalaEndpoint.encodeToUTF()}" +
            "&$TOKEN_ENCODED=${token.encodeToUTF()}"
    }

    object AccountCreationFailed : Route(
        name = "account_creation_failed",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )

    object AwalaNotInstalled : Route(
        name = "awala_not_installed_route",
        showTopBar = false,
        isStatusBarVisible = false,
    )

    object AwalaInitializing : Route(
        name = "awala_initializing_route",
        showTopBar = false,
        isStatusBarPrimaryColor = false,
    )

    data class AwalaInitializationError(
        @Type val type: Int,
    ) : Route(
        name = "$NAME_PREFIX$type",
        showTopBar = false,
        isStatusBarPrimaryColor = true,
    ) {

        @IntDef(TYPE_FATAL_ERROR, TYPE_NON_FATAL_ERROR, TYPE_NEED_TO_OPEN_AWALA)
        annotation class Type
        companion object {
            const val TYPE_FATAL_ERROR = 0
            const val TYPE_NON_FATAL_ERROR = 1
            const val TYPE_NEED_TO_OPEN_AWALA = 2

            internal const val NAME_PREFIX = "awala_initialization_error_"
        }
    }

    object AccountCreationWaiting : Route(
        name = "registration_waiting_route",
        showTopBar = true,
        isStatusBarPrimaryColor = true,
    )

    object AccountLinkingWaiting : Route(
        name = "account_linking_waiting_route",
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
        const val KEY_SCREEN_TYPE = "screen_type"
        const val KEY_CONTACT_ID_TO_EDIT = "contact_id"
        const val KEY_PREFILLED_ACCOUNT_ID_ENCODED = "prefilled_account_id_encoded"
        const val NO_ID = -1L

        fun getRouteName(
            @ManageContactViewModel.Type screenType: Int,
            contactIdToEdit: Long = NO_ID,
            prefilledContactAccountId: String = "",
        ) = "${ManageContact.name}?" +
            "$KEY_SCREEN_TYPE=$screenType" +
            "&$KEY_CONTACT_ID_TO_EDIT=$contactIdToEdit" +
            if (prefilledContactAccountId.isNotEmptyOrBlank()) "&$KEY_PREFILLED_ACCOUNT_ID_ENCODED=${prefilledContactAccountId.encodeToUTF()}" else ""
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
        const val KEY_WITH_ATTACHED_FILES = "with_attached_files"
        const val KEY_CONTACT_ID = "contact_id"
        const val NO_ID = -1L

        fun getRouteName(
            @ComposeNewMessageViewModel.ScreenType screenType: Int,
            withAttachedFiles: Boolean = false,
            contactId: Long = NO_ID,
            conversationId: String? = null,
        ) =
            "${CreateNewMessage.name}?" +
                "$KEY_SCREEN_TYPE=$screenType" +
                "&$KEY_WITH_ATTACHED_FILES=$withAttachedFiles" +
                "&$KEY_CONTACT_ID=$contactId" +
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

    object Settings : Route(
        name = "settings_route",
        showTopBar = false,
        isStatusBarPrimaryColor = false,
    )
}

fun String?.toRoute(): Route {
    this?.let {
        return when {
            it.startsWith(Route.AwalaNotInstalled.name) -> Route.AwalaNotInstalled
            it.startsWith(Route.AwalaInitializing.name) -> Route.AwalaInitializing
            it.startsWith(Route.AwalaInitializationError.NAME_PREFIX) -> Route.AwalaInitializationError(this.removePrefix(Route.AwalaInitializationError.NAME_PREFIX).toInt())
            it.startsWith(Route.Registration.name) -> Route.Registration
            it.startsWith(Route.AccountCreationWaiting.name) -> Route.AccountCreationWaiting
            it.startsWith(Route.AccountLinkingWaiting.name) -> Route.AccountLinkingWaiting
            it.startsWith(Route.AccountCreationFailed.name) -> Route.AccountCreationFailed
            it.startsWith(Route.UseExistingAccount.name) -> Route.UseExistingAccount
            it.startsWith(Route.WelcomeToLetro.name) -> Route.WelcomeToLetro
            it.startsWith(Route.NoContacts.name) -> Route.NoContacts
            it.startsWith(Route.ManageContact.name) -> Route.ManageContact
            it.startsWith(Route.Home.name) -> Route.Home
            it.startsWith(Route.CreateNewMessage.name) -> Route.CreateNewMessage
            it.startsWith(Route.Conversation.name) -> Route.Conversation
            it.startsWith(Route.Settings.name) -> Route.Settings
            else -> throw IllegalArgumentException("Define the Route by the name of the Route $it")
        }
    }
    throw IllegalArgumentException("Define the Route by the name of the Route")
}
