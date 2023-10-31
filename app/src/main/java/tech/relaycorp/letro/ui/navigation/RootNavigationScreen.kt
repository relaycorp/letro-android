package tech.relaycorp.letro.ui.navigation

sealed interface RootNavigationScreen {
    object Registration : RootNavigationScreen
    object AccountCreationWaiting : RootNavigationScreen
    object AccountLinkingWaiting : RootNavigationScreen
    object AccountCreationFailed : RootNavigationScreen
    object WelcomeToLetro : RootNavigationScreen
    object NoContactsScreen : RootNavigationScreen
    object Home : RootNavigationScreen
    object AwalaNotInstalled : RootNavigationScreen
    data class AwalaInitializationError(
        val type: Int,
    ) : RootNavigationScreen
    object AwalaInitializing : RootNavigationScreen

    fun toRoute() = when (this) {
        Registration -> {
            Route.Registration
        }

        AccountCreationFailed -> {
            Route.AccountCreationFailed
        }

        Home -> {
            Route.Home
        }

        NoContactsScreen -> {
            Route.NoContacts
        }

        WelcomeToLetro -> {
            Route.WelcomeToLetro
        }

        AccountCreationWaiting -> {
            Route.AccountCreationWaiting
        }

        AccountLinkingWaiting -> {
            Route.AccountLinkingWaiting
        }

        AwalaNotInstalled -> {
            Route.AwalaNotInstalled
        }

        AwalaInitializing -> {
            Route.AwalaInitializing
        }

        is AwalaInitializationError -> {
            Route.AwalaInitializationError(
                type = type,
            )
        }
    }
}
