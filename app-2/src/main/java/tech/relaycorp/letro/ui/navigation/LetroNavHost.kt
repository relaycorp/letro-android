package tech.relaycorp.letro.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.awala.AwalaNotInstalledScreen
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.common.LetroTopBar
import tech.relaycorp.letro.onboarding.registration.ui.RegistrationScreen
import tech.relaycorp.letro.ui.common.SplashScreen
import tech.relaycorp.letro.utils.compose.rememberLifecycleEvent
import tech.relaycorp.letro.utils.navigation.navigateWithPoppingAllBackStack

@Composable
fun LetroNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val systemUiController: SystemUiController = rememberSystemUiController()
    var currentRoute: Route by remember { mutableStateOf(Route.Splash) }

    val uiState by mainViewModel.uiState.collectAsState()
    val showAwalaNotInstalledScreen by mainViewModel.showInstallAwalaScreen.collectAsState()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route.toRoute()
        }
    }

    val lifecycleEvent = rememberLifecycleEvent()
    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            mainViewModel.onScreenResumed(currentRoute)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.rootNavigationScreen.collect { firstNavigation ->
            handleFirstNavigation(navController, firstNavigation)
        }
    }

    if (showAwalaNotInstalledScreen) {
        systemUiController.isStatusBarVisible = false
        AwalaNotInstalledScreen(
            mainViewModel = mainViewModel,
            onInstallAwalaClick = {
                mainViewModel.onInstallAwalaClick()
            }
        )
    } else {
        systemUiController.isStatusBarVisible = true
        systemUiController.setStatusBarColor(
            if (currentRoute.isStatusBarPrimaryColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
        val currentAccount = uiState.currentAccount
        Column {
            if (currentRoute.showTopBar && currentAccount != null) {
                LetroTopBar(
                    accountVeraId = currentAccount,
                    isAccountCreated = uiState.isCurrentAccountCreated,
                    onChangeAccountClicked = { /*TODO*/ },
                    onSettingsClicked = { }
                )
            }
            NavHost(
                navController = navController,
                startDestination = Route.Splash.name,
            ) {
                composable(Route.Splash.name) {
                    SplashScreen()
                }
                composable(Route.Registration.name) {
                    RegistrationScreen(
                        onUseExistingAccountClick = {}
                    )
                }
                composable(Route.WelcomeToLetro.name) {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.NoContacts(
                            title = R.string.onboarding_account_confirmation,
                            image = R.drawable.account_created,
                            onPairWithPeople = { /* TODO */ },
                            onShareId = { /* TODO */ }
                        )
                    )
                }
                composable(Route.NoContacts.name) {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.NoContacts(
                            title = null,
                            message = R.string.no_contacts_text,
                            image = R.drawable.no_contacts_image,
                            onPairWithPeople = { /* TODO */ },
                            onShareId = { /* TODO */ }
                        )
                    )
                }
                composable(Route.RegistrationProcessWaiting.name) {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.RegistrationWaiting
                    )
                }
                composable(Route.PairingRequestSent.name) {
                    ActionTakingScreen(
                        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.PairingRequestSent(
                            onGotItClicked = { /* TODO */ }
                        )
                    )
                }
            }
        }
    }

}

private fun handleFirstNavigation(
    navController: NavHostController,
    firstNavigation: RootNavigationScreen,
) {
    when (firstNavigation) {
        RootNavigationScreen.Splash -> {
            navController.navigateWithPoppingAllBackStack(Route.Splash)
        }
        RootNavigationScreen.Registration -> {
            navController.navigateWithPoppingAllBackStack(Route.Registration)
        }

        RootNavigationScreen.Conversations -> {
            navController.navigateWithPoppingAllBackStack(Route.WelcomeToLetro)
        }

        RootNavigationScreen.NoContactsScreen -> {
            navController.navigateWithPoppingAllBackStack(Route.NoContacts)
        }

        RootNavigationScreen.WelcomeToLetro -> {
            navController.navigateWithPoppingAllBackStack(Route.WelcomeToLetro)
        }

        RootNavigationScreen.RegistrationWaiting -> {
            navController.navigateWithPoppingAllBackStack(Route.RegistrationProcessWaiting)
        }
    }
}