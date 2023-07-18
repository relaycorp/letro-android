package tech.relaycorp.letro.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.main.MainActivity

@HiltAndroidTest
class InitialAppRunTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun assertIsDisplayed_letroLogo_on_SplashScreen() {
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.splash_welcome)).assertIsDisplayed()
    }

    @Test
    fun wait_for_nextScreen() {
//        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.splash_welcome)).assertIsDisplayed()
//
//        composeRule.mainClock.autoAdvance = true
//        composeRule.waitForIdle()
//        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.splash_welcome)).assertDoesNotExist()
//
//        composeRule.mainClock.autoAdvance = true
//        composeRule.waitForIdle()
//        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_install_awala_title)).assertIsDisplayed()
    }
}
