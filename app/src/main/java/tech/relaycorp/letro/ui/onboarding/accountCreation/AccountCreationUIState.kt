package tech.relaycorp.letro.ui.onboarding.accountCreation

data class AccountCreationUIState(
    val username: String = "",
    val domain: String = "",
    val isLoading: Boolean = false,
)
