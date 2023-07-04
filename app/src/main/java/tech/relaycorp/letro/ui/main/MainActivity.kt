package tech.relaycorp.letro.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import tech.relaycorp.letro.R
import tech.relaycorp.letro.Route
import tech.relaycorp.letro.getRouteByName
import tech.relaycorp.letro.ui.SplashScreen
import tech.relaycorp.letro.ui.onboarding.accountCreation.AccountCreationRoute
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.onboarding.gatewayNotInstalled.GatewayNotInstalledScreen
import tech.relaycorp.letro.ui.onboarding.useExistingAccount.UseExistingAccountRoute
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var currentRoute: Route by remember { mutableStateOf(Route.Splash) }
            val systemUiController: SystemUiController = rememberSystemUiController()
            val accountUsername by mainViewModel.accountUsernameFlow.collectAsState()
            var tabIndex by remember { mutableIntStateOf(0) }
            val awalaGatewayAppLink = stringResource(id = R.string.url_awala_gateway_app)

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
                val topBarUIState = getTopBarUIState(currentRoute)
                systemUiController.isStatusBarVisible = topBarUIState.showStatusBar

                Scaffold(
                    topBar = {
                        LetroTopBar(
                            accountUsername = accountUsername,
                            onChangeAccountClicked = { /*TODO*/ },
                            onSettingsClicked = { /*TODO*/ },
                            tabIndex = tabIndex,
                            updateTabIndex = { tabIndex = it },
                            navigateToHomeScreen = { /*TODO*/ },
                            topBarUIState = topBarUIState,
                        )
                    },
                    content = {
                        LetroNavHostContainer(
                            navController = navController,
                            paddingValues = it,
                            onNavigateToGooglePlay = {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(awalaGatewayAppLink),
                                    ),
                                )
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun LetroNavHostContainer(
    navController: NavHostController,
    onNavigateToGooglePlay: () -> Unit,
    paddingValues: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.name,
        modifier = Modifier.padding(paddingValues),
    ) {
        composable(Route.AccountConfirmation.name) {
            ActionTakingScreen(ActionTakingScreenUIStateModel.AccountConfirmation)
        }
        composable(Route.AccountCreation.name) {
            AccountCreationRoute(
                onCreateAccount = {
                    navController.navigate(
                        Route.WaitingForAccountCreation.name,
                    ) // TODO Change to correct functionality
                },
                onUseExistingAccount = {
                    navController.navigate(
                        Route.UseExistingAccount.name,
                    ) // TODO Change to correct functionality
                },
            )
        }
        composable(Route.GatewayNotInstalled.name) {
            GatewayNotInstalledScreen(
                onNavigateToGooglePlay = onNavigateToGooglePlay,
            )
        }
        composable(Route.PairingRequestSent.name) {
            ActionTakingScreen(ActionTakingScreenUIStateModel.PairingRequestSent)
        }
        composable(Route.Splash.name) {
            SplashScreen()
        }
        composable(Route.UseExistingAccount.name) {
            UseExistingAccountRoute(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToAccountConfirmationScreen = {
                    navController.navigate(Route.AccountConfirmation.name)
                },
            )
        }
        composable(Route.WaitingForAccountCreation.name) {
            ActionTakingScreen(ActionTakingScreenUIStateModel.Waiting)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetroTopBar(
    accountUsername: String,
    modifier: Modifier = Modifier,
    onChangeAccountClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    tabIndex: Int,
    updateTabIndex: (Int) -> Unit,
    navigateToHomeScreen: (Route) -> Unit,
    topBarUIState: TopBarUIState,
) {
    if (topBarUIState.showTopBar) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                modifier = modifier,
                title = {
                    if (topBarUIState.showAccountName) {
                        Row(
                            modifier = Modifier
                                .clickable { onChangeAccountClicked() }
                                .padding(
                                    horizontal = HorizontalScreenPadding,
                                    vertical = VerticalScreenPadding,
                                ),
                        ) {
                            Text(
                                text = accountUsername,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(ItemPadding))
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.width(ItemPadding))
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_down),
                                contentDescription = stringResource(id = R.string.top_bar_change_account),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            painterResource(id = R.drawable.settings),
                            contentDescription = stringResource(id = R.string.top_bar_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            )
            if (topBarUIState.showTabs) {
                LetroTabs(
                    tabIndex = tabIndex,
                    updateTabIndex = updateTabIndex,
                    navigateToHomeScreen = navigateToHomeScreen,
                )
            }
        }
    }
}

private fun showTopBar(currentRoute: Route): Boolean = currentRoute != Route.Splash
        && currentRoute != Route.AccountCreation

private fun showTabs(currentRoute: Route): Boolean = currentRoute.name.contains(Route.Messages.name)
        || currentRoute.name.contains(Route.Contacts.name)
        || currentRoute.name.contains(Route.Notifications.name)

private fun showAccountName(currentRoute: Route): Boolean = currentRoute != Route.AccountCreation

private const val TAB_MESSAGES = 0
private const val TAB_CONTACTS = 1
private const val TAB_NOTIFICATIONS = 2

@Composable
fun LetroTabs(
    tabIndex: Int,
    updateTabIndex: (Int) -> Unit,
    navigateToHomeScreen: (Route) -> Unit,
) {
    val tabTitles = listOf(
        stringResource(id = R.string.top_bar_tab_messages),
        stringResource(id = R.string.top_bar_tab_contacts),
        stringResource(id = R.string.top_bar_tab_notifications),
    )
    TabRow(
        selectedTabIndex = tabIndex,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = tabIndex == index,
                onClick = {
                    updateTabIndex(index)
                    when (index) {
                        TAB_MESSAGES -> navigateToHomeScreen(Route.Messages)
                        TAB_CONTACTS -> navigateToHomeScreen(Route.Contacts)
                        TAB_NOTIFICATIONS -> navigateToHomeScreen(Route.Notifications)
                    }
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
            )
        }
    }
}
