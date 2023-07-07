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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
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
import tech.relaycorp.letro.ui.conversations.ConversationsRoute
import tech.relaycorp.letro.ui.conversations.messages.MessagesRoute
import tech.relaycorp.letro.ui.conversations.newMessage.NewMessageRoute
import tech.relaycorp.letro.ui.onboarding.accountCreation.AccountCreationRoute
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingRoute
import tech.relaycorp.letro.ui.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.onboarding.gatewayNotInstalled.GatewayNotInstalledRoute
import tech.relaycorp.letro.ui.onboarding.pair.PairWithPeopleRoute
import tech.relaycorp.letro.ui.onboarding.useExistingAccount.UseExistingAccountRoute
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme
import tech.relaycorp.letro.ui.theme.VerticalScreenPadding
import tech.relaycorp.letro.utility.navigateWithPoppingAllBackStack

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
                        FirstNavigationUIModel.AccountCreation -> {
                            navController.navigateWithPoppingAllBackStack(Route.AccountCreation)
                        }

                        FirstNavigationUIModel.NoGateway -> {
                            navController.navigateWithPoppingAllBackStack(Route.GatewayNotInstalled)
                        }

                        else -> {}
                    }
                }
            }

            LetroTheme {
                systemUiController.isStatusBarVisible = currentRoute.showStatusBar

                Scaffold(
                    topBar = {
                        LetroTopBar(
                            accountUsername = accountUsername,
                            onChangeAccountClicked = { /*TODO*/ },
                            onSettingsClicked = { /*TODO*/ },
                            tabIndex = tabIndex,
                            updateTabIndex = { tabIndex = it },
                            navigateToHomeScreen = { route ->
                                navController.navigateWithPoppingAllBackStack(route)
                            },
                            currentRoute = currentRoute,
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
                    floatingActionButton = {
                        currentRoute.floatingActionButtonFeatures?.let { features ->
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(features.routeToNavigateTo.name)
                                },
                                modifier = Modifier.padding(
                                    bottom = VerticalScreenPadding,
                                    end = HorizontalScreenPadding,
                                ),
                                containerColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Icon(
                                    painter = painterResource(id = features.iconResource),
                                    contentDescription = stringResource(
                                        id = features.contentDescriptionResource,
                                    ),
                                )
                            }
                        }
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
            ActionTakingRoute(
                ActionTakingScreenUIStateModel.AccountConfirmation(
                    onPairWithPeople = {
                        navController.navigate(Route.PairWithPeople.name)
                    },
                    onShareId = {
                        // TODO Replace with sharing id functionality
                        navController.navigateWithPoppingAllBackStack(Route.Conversations)
                    },
                ),
            )
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
        composable(Route.Conversations.name) {
            ConversationsRoute(onChangeConversationsTypeClicked = { /*TODO*/ })
        }
        composable(Route.GatewayNotInstalled.name) {
            GatewayNotInstalledRoute(
                onNavigateToGooglePlay = onNavigateToGooglePlay,
            )
        }
        composable(Route.Messages.name) {
            MessagesRoute(
                onBackClicked = {
                    navController.popBackStack()
                },
                onReplyClicked = {
                    navController.navigate(Route.NewMessage.name)
                },
            )
        }
        composable(Route.NewMessage.name) {
            NewMessageRoute(onBackClicked = {
                navController.popBackStack()
            })
        }
        composable(Route.PairWithPeople.name) {
            PairWithPeopleRoute(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToPairingRequestSentScreen = {
                    navController.navigate(Route.PairingRequestSent.name)
                },
            )
        }
        composable(Route.PairingRequestSent.name) {
            ActionTakingRoute(
                ActionTakingScreenUIStateModel.PairingRequestSent(
                    onGotItClicked = {
                        navController.navigateWithPoppingAllBackStack(Route.Conversations)
                    },
                ),
            )
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
            ActionTakingRoute(ActionTakingScreenUIStateModel.Waiting)
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
    currentRoute: Route,
) {
    if (currentRoute.showTopBar) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                modifier = modifier,
                title = {
                    if (currentRoute.showAccountNameAndActions) {
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
                    } else {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = if (currentRoute.isTopBarContainerColorPrimary) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                },
                actions = {
                    if (currentRoute.showAccountNameAndActions) {
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                painterResource(id = R.drawable.settings),
                                contentDescription = stringResource(id = R.string.top_bar_settings),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (currentRoute.isTopBarContainerColorPrimary) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
            )
            if (currentRoute.showTabs) {
                LetroTabs(
                    tabIndex = tabIndex,
                    updateTabIndex = updateTabIndex,
                    navigateToHomeScreen = navigateToHomeScreen,
                )
            }
        }
    }
}

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
        stringResource(id = R.string.top_bar_tab_conversations),
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
                        TAB_MESSAGES -> navigateToHomeScreen(Route.Conversations)
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
