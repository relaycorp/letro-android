package tech.relaycorp.letro.ui.main

sealed interface InitialAppNavigationUIModel {
    object NoGateway : InitialAppNavigationUIModel
    object AccountCreation : InitialAppNavigationUIModel
    object WaitingForAccountCreationConfirmation : InitialAppNavigationUIModel
    object Conversations : InitialAppNavigationUIModel
    object AccountCreationConfirmed : InitialAppNavigationUIModel
    object Splash : InitialAppNavigationUIModel
}
