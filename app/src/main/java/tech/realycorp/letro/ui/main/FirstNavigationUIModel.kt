package tech.realycorp.letro.ui.main

sealed interface FirstNavigationUIModel {
    object NoGateway : FirstNavigationUIModel
    object AccountCreation : FirstNavigationUIModel
    object Splash : FirstNavigationUIModel
}
