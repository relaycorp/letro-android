package tech.relaycorp.letro.ui.navigation

sealed interface RootNavigationScreen {
    object Splash : RootNavigationScreen
    object Registration : RootNavigationScreen
    object RegistrationWaiting : RootNavigationScreen
    object Conversations : RootNavigationScreen
    object WelcomeToLetro : RootNavigationScreen
    object NoContactsScreen : RootNavigationScreen
}
