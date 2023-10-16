package tech.relaycorp.letro.ui.navigation

sealed interface RootNavigationScreen {
    object Splash : RootNavigationScreen
    object Registration : RootNavigationScreen
    object RegistrationWaiting : RootNavigationScreen
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
        Splash -> {
            Route.Splash
        }
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

        RegistrationWaiting -> {
            Route.RegistrationProcessWaiting
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
