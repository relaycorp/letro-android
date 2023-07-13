package tech.relaycorp.letro.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.navigation.LetroNavHostContainer
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.navigation.getRouteByName
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
            val mainUIState by mainViewModel.mainUIStateFlow.collectAsState()
            var tabIndex by remember { mutableIntStateOf(0) }
            val awalaGatewayAppLink = stringResource(id = R.string.url_awala_gateway_app)

            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    currentRoute = backStackEntry.destination.route.getRouteByName()
                }
            }

            if (currentRoute == Route.WaitingForAccountCreation && mainUIState.isAccountCreated) {
                navController.navigateWithPoppingAllBackStack(Route.Conversations)
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
                MainScreen(
                    systemUiController = systemUiController,
                    navController = navController,
                    currentRoute = currentRoute,
                    uiState = mainUIState,
                    tabIndex = tabIndex,
                    onTabIndexChanged = { tabIndex = it },
                    onNavigateToGooglePlay = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(awalaGatewayAppLink),
                            ),
                        )
                    },

                )
            }
        }
    }
}

@Composable
fun MainScreen(
    systemUiController: SystemUiController,
    navController: NavHostController,
    currentRoute: Route,
    uiState: MainUIState,
    tabIndex: Int,
    onTabIndexChanged: (Int) -> Unit,
    onNavigateToGooglePlay: () -> Unit,
) {
    systemUiController.isStatusBarVisible = currentRoute.showStatusBar

    Scaffold(
        topBar = {
            LetroTopBar(
                accountAddress = uiState.address,
                onChangeAccountClicked = { /*TODO*/ },
                onSettingsClicked = { /*TODO*/ },
                tabIndex = tabIndex,
                updateTabIndex = onTabIndexChanged,
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
                onNavigateToGooglePlay = onNavigateToGooglePlay,
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
                    shape = CircleShape,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LetroTopBar(
    accountAddress: String,
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = accountAddress,
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
private fun LetroTabs(
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

@Preview(showBackground = true)
@Composable
private fun LetroTopBarPreview() {
    LetroTheme {
        LetroTopBar(
            accountAddress = "John Doe",
            onChangeAccountClicked = {},
            onSettingsClicked = {},
            tabIndex = 0,
            updateTabIndex = {},
            navigateToHomeScreen = {},
            currentRoute = Route.Conversations,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LetroTopBarPreviewDark() {
    LetroTheme(darkTheme = true) {
        LetroTopBar(
            accountAddress = "John Doe",
            onChangeAccountClicked = {},
            onSettingsClicked = {},
            tabIndex = 0,
            updateTabIndex = {},
            navigateToHomeScreen = {},
            currentRoute = Route.Conversations,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationsPreview() {
    LetroTheme {
        MainScreen(
            systemUiController = rememberSystemUiController(),
            navController = rememberNavController(),
            currentRoute = Route.Conversations,
            uiState = MainUIState(
                address = "John Doe",
            ),
            tabIndex = 0,
            onTabIndexChanged = {},
            onNavigateToGooglePlay = {},
        )
    }
}
