package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.main.MainActivity
import tech.relaycorp.letro.ui.theme.LetroTheme

@RunWith(AndroidJUnit4::class)
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
                )
            }
        }
    }

    @Test
    fun verify_CreateAccountButton_isDisplayed() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_create_account_button)).assertIsDisplayed()
    }

    @Test
    fun click_CreateAccountButton_goesTo_WaitingForAccountCreationScreen() {
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
    fun verify_UseExistingAccountButton_isDisplayed() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_use_existing_account_button)).assertIsDisplayed()
    }
}
