package tech.relaycorp.letro.ui.main

sealed interface FirstNavigationUIModel {
    object NoGateway : FirstNavigationUIModel
    object AccountCreation : FirstNavigationUIModel
    object WaitingForAccountCreationConfirmation : FirstNavigationUIModel
    object Conversations : FirstNavigationUIModel
    object PairWithPeople : FirstNavigationUIModel
    object Splash : FirstNavigationUIModel
}
