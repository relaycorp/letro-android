package tech.realycorp.letro.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import tech.realycorp.letro.Route
import tech.realycorp.letro.getRouteByName
import tech.realycorp.letro.ui.SplashScreen
import tech.realycorp.letro.ui.onboarding.accountCreation.AccountCreationScreen
import tech.realycorp.letro.ui.onboarding.actionTaking.ActionTakingScreen
import tech.realycorp.letro.ui.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.realycorp.letro.ui.onboarding.gatewayNotInstalled.GatewayNotInstalledScreen
import tech.realycorp.letro.ui.theme.LetroTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var currentRoute: Route by remember { mutableStateOf(Route.Splash) }
            val systemUiController: SystemUiController = rememberSystemUiController()

            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    currentRoute = backStackEntry.destination.route.getRouteByName()
                }
            }

            LaunchedEffect(mainViewModel) {
                mainViewModel.firstNavigationUIModelFlow.collect { firstNavigation ->
                    when (firstNavigation) {
                        FirstNavigationUIModel.AccountCreation -> navController.navigate(Route.AccountCreation.name) {
                            popUpTo(Route.Splash.name) {
                                inclusive = true
                            }
                        }
                        FirstNavigationUIModel.NoGateway -> navController.navigate(Route.GatewayNotInstalled.name) {
                            popUpTo(Route.Splash.name) {
                                inclusive = true
                            }
                        }
                        else -> {}
                    }
                }
            }

            LetroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHostContainer(
                        navController = navController,
                        showStatusBar = { show ->
                            systemUiController.isStatusBarVisible = show
                        },
                        onNavigateToGooglePlay = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=tech.relaycorp.gateway"),
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    showStatusBar: (Boolean) -> Unit,
    onNavigateToGooglePlay: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.name,
    ) {
        composable(Route.AccountConfirmation.name) {
            showStatusBar(true)
            ActionTakingScreen(ActionTakingScreenUIStateModel.AccountConfirmation)
        }
        composable(Route.AccountCreation.name) {
            showStatusBar(false)
            AccountCreationScreen(
                onCreateAccount = {
                    navController.navigate(
                        Route.WaitingForAccountCreation.name,
                    ) // TODO Change to correct functionality
                },
                onUseExistingAccount = {
                    navController.navigate(
                        Route.AccountConfirmation.name,
                    ) // TODO Change to correct functionality
                },
            )
        }
        composable(Route.GatewayNotInstalled.name) {
            showStatusBar(false)
            GatewayNotInstalledScreen(
                onNavigateToGooglePlay = onNavigateToGooglePlay,
            )
        }
        composable(Route.PairingRequestSent.name) {
            showStatusBar(true)
            ActionTakingScreen(ActionTakingScreenUIStateModel.PairingRequestSent)
        }
        composable(Route.Splash.name) {
            showStatusBar(false)
            SplashScreen()
        }
        composable(Route.WaitingForAccountCreation.name) {
            showStatusBar(true)
            ActionTakingScreen(ActionTakingScreenUIStateModel.Waiting)
        }
    }
}
