package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.main.MainActivity
import tech.relaycorp.letro.ui.theme.LetroTheme

@RunWith(AndroidJUnit4::class)
class AccountCreationScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
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
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.onboarding_create_account_button)).assertIsDisplayed()
    }

    @Test
    fun click_CreateAccountButton_goesTo_WaitingForAccountCreation() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.onboarding_create_account_button)).performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.onboarding_waiting_title)).assertIsDisplayed()
    }
}
