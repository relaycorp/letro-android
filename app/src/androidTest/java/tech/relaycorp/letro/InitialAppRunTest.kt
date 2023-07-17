package tech.relaycorp.letro

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tech.relaycorp.letro.repository.PreferencesDataStoreRepository
import tech.relaycorp.letro.ui.main.MainActivity
import javax.inject.Inject

@HiltAndroidTest
class InitialAppRunTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var preferencesDataStoreRepository: PreferencesDataStoreRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun assert_allViewsAreDisplayed_on_GatewayNotInstalledScreen() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_install_awala_title)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onbaording_install_awala_message)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_install_awala_button)).assertIsDisplayed()
    }

    // TODO Fix "There are multiple DataStores active for the same file" issue
    @Test
    fun click_DownloadGatewayButton_goesTo_GooglePlayStore() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_install_awala_button)).performClick()
        // TODO Assert that the next screen is displayed
    }

    @After
    fun tearDown() {
        // Clean up the dataStore
        runBlocking {
            preferencesDataStoreRepository.clear()
        }
    }
}
