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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import tech.realycorp.letro.Route
import tech.realycorp.letro.getRouteByName
import tech.realycorp.letro.ui.SplashScreen
import tech.realycorp.letro.ui.onboarding.accountCreation.AccountCreationScreen
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

            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    currentRoute = backStackEntry.destination.route.getRouteByName()
                }
            }

            LaunchedEffect(mainViewModel) {
                mainViewModel.firstNavigationUIModelFlow.collect { firstNavigation ->
                    when (firstNavigation) {
                        FirstNavigationUIModel.AccountCreation -> navController.navigate(Route.AccountCreation.name)
                        FirstNavigationUIModel.NoGateway -> navController.navigate(Route.GatewayNotInstalled.name)
                        else -> {}
                    }
                }
            }

            LetroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Route.Splash.name,
                    ) {
                        composable(Route.AccountCreation.name) {
                            AccountCreationScreen()
                        }
                        composable(Route.GatewayNotInstalled.name) {
                            GatewayNotInstalledScreen(
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
                        composable(Route.Splash.name) {
                            SplashScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LetroTheme {
        Greeting("Android")
    }
}
