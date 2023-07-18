package tech.relaycorp.letro.ui.main

sealed interface FirstNavigationUIModel {
    object NoGateway : FirstNavigationUIModel
    object AccountCreation : FirstNavigationUIModel
    object WaitingForAccountCreationConfirmation : FirstNavigationUIModel
    object Conversations : FirstNavigationUIModel
    object Splash : FirstNavigationUIModel
}
