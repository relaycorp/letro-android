package tech.relaycorp.letro.account.manage

import androidx.lifecycle.SavedStateHandle
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.models.account.createAccount
import tech.relaycorp.letro.utils.models.account.createAccountManageViewModel

class AccountManageViewModelTest {

    @Test
    fun `Test AccountRepository delete() method called after click on 'Delete account'`() {
        val repository: AccountRepository = mockk(relaxed = true)
        val account = createAccount()
        val viewModel = createAccountManageViewModel(
            accountRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf(Route.AccountManage.ACCOUNT_ID to account.id.toString())),
        )
        viewModel.onConfirmAccountDeleteClick(account)
        coVerify {
            repository.deleteAccount(account)
        }
    }
}
