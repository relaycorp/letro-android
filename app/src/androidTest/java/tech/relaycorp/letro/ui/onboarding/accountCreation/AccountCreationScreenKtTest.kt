package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.LetroTheme

@HiltAndroidTest
class AccountCreationScreenKtTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            LetroTheme {
                AccountCreationRoute(
                    onNavigateToAccountCreationWaitingScreen = {},
                    onUseExistingAccount = {},
                    viewModel = hiltViewModel(),
                )
            }
        }
    }

    private fun verify_UseExistingAccountButton_isDisplayed() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_use_existing_account_button)).assertIsDisplayed()
    }

    private fun verify_CreateAccountButton_isDisplayed() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_create_account_button)).assertIsDisplayed()
    }

    @Test
    fun click_CreateAccountButton_goesTo_WaitingForAccountCreationScreen() {
        verify_CreateAccountButton_isDisplayed()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_create_account_button)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_waiting_title)).assertIsDisplayed()
    }

    @Test
    fun click_CreateAccountButton_withWrongInput_showsError() {
        // TODO
    }

    @Test
    fun if_accountExists_navigateTo_MainScreen() {
        // TODO
    }

    @Test
    fun click_BackButton_with_accountExists_navigateTo_MainScreen() {
        // TODO or navigate back to whatever screen was before
    }

    @Test
    fun click_BackButton_without_accountExists_exitTheApp() {
        // TODO
    }

    @Test
    fun click_UseExistingAccountButton_goesTo_UseExistingAccountScreen() {
        verify_UseExistingAccountButton_isDisplayed()

        // TODO
    }
}
