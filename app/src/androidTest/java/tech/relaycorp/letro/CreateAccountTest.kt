package tech.relaycorp.letro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.relaycorp.letro.ui.main.MainActivity

@HiltAndroidTest
class CreateAccountTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun click_CreateAccountButton_goesTo_WaitingForAccountCreationScreen() {
        // TODO navigate to AccountCreationScreen
        // Assert that CreateAccountButton is displayed
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_create_account_button)).performClick()

        // Assert that screen has changed to WaitingForAccountCreation
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_waiting_title)).assertIsDisplayed()
    }
}
