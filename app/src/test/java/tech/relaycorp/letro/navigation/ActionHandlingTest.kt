package tech.relaycorp.letro.navigation

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.utils.models.account.createAccount
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.contact.createContact
import tech.relaycorp.letro.utils.models.contact.createContactsRepository
import tech.relaycorp.letro.utils.models.conversation.createConversation
import tech.relaycorp.letro.utils.models.main.createMainViewModel
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ActionHandlingTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `Test that if there are 2 accounts, and there is an action came to inactive account, this action is started processing only after account and root navigation screen were changed`() = runTest(UnconfinedTestDispatcher()) {
        val activeAccountOnStart = createAccount(
            accountId = "test@account.id",
            isCurrent = true,
        )
        val accountForAction = createAccount(
            accountId = "action@account.id",
            isCurrent = false,
        )
        val accountRepository = createAccountRepository(
            accounts = listOf(activeAccountOnStart, accountForAction),
        )

        val contactOfSecondAccount = createContact(ownerVeraId = accountForAction.accountId)

        val conversationId = UUID.randomUUID()
        val conversation = createConversation(
            id = conversationId,
            ownerVeraId = accountForAction.accountId,
            contactVeraId = contactOfSecondAccount.contactVeraId,
        )

        val mainViewModel = createMainViewModel(
            accountRepository = accountRepository,
            contactsRepository = createContactsRepository(
                accountRepository = accountRepository,
                contacts = listOf(
                    createContact(ownerVeraId = activeAccountOnStart.accountId),
                    contactOfSecondAccount,
                ),
            ),
            conversations = listOf(conversation),
            mainDispatcher = dispatcher,
        )

        // Test initial state:
        mainViewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.Home
        mainViewModel.uiState.value.currentAccount shouldBe activeAccountOnStart.accountId

        var lastReceivedAction: Action? = null

        val actionCollectionJob = launch(UnconfinedTestDispatcher()) {
            mainViewModel.actions.collect {
                lastReceivedAction = it
            }
        }

        val actionToHandle = Action.OpenConversation(
            conversationId = conversation.conversationId.toString(),
            accountId = accountForAction.accountId,
        )
        mainViewModel.onNewAction(actionToHandle)

        // While account is being changed, coroutine will suspend and action will be null for a while (= waiting when account will have changed to proceed)
        lastReceivedAction shouldBe null

        // Wait when account will have changed to check if action was emitted
        delay(1000L)

        actionCollectionJob.cancel()

        mainViewModel.uiState.value.currentAccount shouldBe accountForAction.accountId
        mainViewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.Home
        lastReceivedAction shouldBe actionToHandle
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
