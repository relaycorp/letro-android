@file:OptIn(ExperimentalCoroutinesApi::class)

package tech.relaycorp.letro.account.registration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.R
import tech.relaycorp.letro.utils.models.account.createAccount
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.account.registration.createRegistrationDomainProvider
import tech.relaycorp.letro.utils.models.account.registration.createRegistrationRepository

class DuplicateIdTest {

    @Test
    fun `Test that registration button is not enabled and error is shown if the ID already exists`() {
        val existingAccountName = "name"
        val domain = "chores.fans"
        val accountId = "$existingAccountName@$domain"
        val viewModel = RegistrationViewModel(
            registrationRepository = createRegistrationRepository(
                accountRepository = createAccountRepository(
                    accounts = listOf(
                        createAccount(
                            accountId = accountId,
                        ),
                    ),
                ),
            ),
            domainProvider = createRegistrationDomainProvider(domain),
        )

        // Check the initial state
        viewModel.uiState.value.isError shouldBe false
        viewModel.uiState.value.username shouldBe ""
        viewModel.uiState.value.inputSuggestionText shouldBe R.string.onboarding_create_account_username_unavailable_hint
        viewModel.uiState.value.isCreateAccountButtonEnabled shouldBe false

        // Check that after user inputted the correct user name and account id is not occupied, error is not shown and button is enabled
        val userNameTyped = "test"
        viewModel.onUsernameInput(userNameTyped)
        viewModel.uiState.value.isError shouldBe false
        viewModel.uiState.value.username shouldBe userNameTyped
        viewModel.uiState.value.inputSuggestionText shouldBe R.string.onboarding_create_account_username_unavailable_hint
        viewModel.uiState.value.isCreateAccountButtonEnabled shouldBe true

        // Check that after user inputted user name, which is occupied, error is shown and button is not enabled
        val occupiedUserName = existingAccountName
        viewModel.onUsernameInput(occupiedUserName)
        viewModel.uiState.value.isError shouldBe true
        viewModel.uiState.value.username shouldBe occupiedUserName
        viewModel.uiState.value.inputSuggestionText shouldBe R.string.you_already_have_account_with_this_id
        viewModel.uiState.value.isCreateAccountButtonEnabled shouldBe false

        // Check that if user changed the input with occupied name, they're free to continue again
        viewModel.onUsernameInput(viewModel.uiState.value.username + "123")
        viewModel.uiState.value.isError shouldBe false
        viewModel.uiState.value.username shouldBe "${occupiedUserName}123"
        viewModel.uiState.value.inputSuggestionText shouldBe R.string.onboarding_create_account_username_unavailable_hint
        viewModel.uiState.value.isCreateAccountButtonEnabled shouldBe true
    }

    private companion object {
        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeAll
        @JvmStatic
        fun setUp() {
            Dispatchers.setMain(UnconfinedTestDispatcher())
        }
    }
}
