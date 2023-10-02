package tech.relaycorp.letro.ui.navigation

sealed interface RootNavigationScreen {
    object Splash : RootNavigationScreen
    object Registration : RootNavigationScreen
    object RegistrationWaiting : RootNavigationScreen
    object WelcomeToLetro : RootNavigationScreen
    object NoContactsScreen : RootNavigationScreen
    object Home : RootNavigationScreen
    object AwalaNotInstalled : RootNavigationScreen
    object AwalaInitializationError : RootNavigationScreen
    object AwalaInitializing : RootNavigationScreen
}
