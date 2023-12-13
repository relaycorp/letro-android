package tech.relaycorp.letro.ui.navigation

sealed interface RootNavigationScreen {
    object Registration : RootNavigationScreen
    object AccountCreationWaiting : RootNavigationScreen
    object AccountLinkingWaiting : RootNavigationScreen
    object AccountLinkingFailed : RootNavigationScreen
    object AccountCreationFailed : RootNavigationScreen
    data class WelcomeToLetro(
        val withAnimation: Boolean = false,
    ) : RootNavigationScreen
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

        AccountLinkingFailed -> {
            Route.AccountLinkingFailed
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

        is WelcomeToLetro -> {
            Route.WelcomeToLetro(withAnimation = this.withAnimation)
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
