package tech.relaycorp.letro.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.AwalaNotInstalledScreen
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.contacts.ui.ContactsScreenOverlayFloatingMenu
import tech.relaycorp.letro.contacts.ui.ManageContactScreen
import tech.relaycorp.letro.home.HomeScreen
import tech.relaycorp.letro.home.HomeViewModel
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.messages.compose.CreateNewMessageScreen
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.onboarding.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.onboarding.registration.ui.RegistrationScreen
import tech.relaycorp.letro.ui.common.LetroTopBar
import tech.relaycorp.letro.ui.common.SplashScreen
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.compose.rememberLifecycleEvent
import tech.relaycorp.letro.utils.ext.encodeToUTF
import tech.relaycorp.letro.utils.navigation.navigateWithDropCurrentScreen
import tech.relaycorp.letro.utils.navigation.navigateWithPoppingAllBackStack

@Composable
fun LetroNavHost(
    navController: NavHostController,
    snackbarStringsProvider: SnackbarStringsProvider,
    mainViewModel: MainViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val systemUiController: SystemUiController = rememberSystemUiController()
    var currentRoute: Route by remember { mutableStateOf(Route.Splash) }

    val uiState by mainViewModel.uiState.collectAsState()
    val showAwalaNotInstalledScreen by mainViewModel.showInstallAwalaScreen.collectAsState()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val floatingActionButtonConfig = homeUiState.floatingActionButtonConfig

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route.toRoute()
            Log.d("LetroNavHost", "New route: $currentRoute")
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

    LaunchedEffect(Unit) {
        homeViewModel.createNewMessageSignal.collect {
            navController.navigate(Route.CreateNewMessage.name)
        }
    }

    if (showAwalaNotInstalledScreen) {
        systemUiController.isStatusBarVisible = false
        AwalaNotInstalledScreen(
            mainViewModel = mainViewModel,
            onInstallAwalaClick = {
                mainViewModel.onInstallAwalaClick()
            },
        )
    } else {
        systemUiController.isStatusBarVisible = true
        val currentAccount = uiState.currentAccount
        Scaffold(
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    Column {
                        if (currentRoute.showTopBar && currentAccount != null) {
                            LetroTopBar(
                                accountVeraId = currentAccount,
                                isAccountCreated = uiState.isCurrentAccountCreated,
                                onChangeAccountClicked = { /*TODO*/ },
                                onSettingsClicked = { },
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
                                    onUseExistingAccountClick = {},
                                )
                            }
                            composable(Route.WelcomeToLetro.name) {
                                ActionTakingScreen(
                                    actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.NoContacts(
                                        title = R.string.onboarding_account_confirmation,
                                        image = R.drawable.account_created,
                                        onPairWithOthersClick = {
                                            navController.navigate(
                                                Route.ManageContact.getRouteName(
                                                    screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                                    currentAccountIdEncoded = uiState.currentAccount?.encodeToUTF(),
                                                ),
                                            )
                                        },
                                        onShareIdClick = {
                                            mainViewModel.onShareIdClick()
                                        },
                                    ),
                                )
                            }
                            composable(Route.NoContacts.name) {
                                ActionTakingScreen(
                                    actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.NoContacts(
                                        title = null,
                                        message = R.string.no_contacts_text,
                                        image = R.drawable.no_contacts_image,
                                        onPairWithOthersClick = {
                                            navController.navigate(
                                                Route.ManageContact.getRouteName(
                                                    screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                                    currentAccountIdEncoded = uiState.currentAccount?.encodeToUTF(),
                                                ),
                                            )
                                        },
                                        onShareIdClick = {
                                            mainViewModel.onShareIdClick()
                                        },
                                    ),
                                )
                            }
                            composable(Route.RegistrationProcessWaiting.name) {
                                ActionTakingScreen(
                                    actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.RegistrationWaiting,
                                )
                            }
                            composable(
                                route = "${Route.PairingRequestSent.name}/{${Route.PairingRequestSent.RECEIVER_ARGUMENT_VERA_ID}}",
                                arguments = listOf(
                                    navArgument(Route.PairingRequestSent.RECEIVER_ARGUMENT_VERA_ID) {
                                        type = NavType.StringType
                                        nullable = false
                                    },
                                ),
                            ) {
                                ActionTakingScreen(
                                    actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.PairingRequestSent(
                                        boldPartOfMessage = it.arguments?.getString(Route.PairingRequestSent.RECEIVER_ARGUMENT_VERA_ID)!!,
                                        onGotItClicked = {
                                            navController.popBackStack()
                                        },
                                    ),
                                )
                            }
                            composable(
                                route = "${Route.ManageContact.name}/{${Route.ManageContact.KEY_CURRENT_ACCOUNT_ID_ENCODED}}&{${Route.ManageContact.KEY_SCREEN_TYPE}}&{${Route.ManageContact.KEY_CONTACT_ID_TO_EDIT}}",
                                arguments = listOf(
                                    navArgument(Route.ManageContact.KEY_CURRENT_ACCOUNT_ID_ENCODED) {
                                        type = NavType.StringType
                                        nullable = true
                                    },
                                    navArgument(Route.ManageContact.KEY_SCREEN_TYPE) {
                                        type = NavType.IntType
                                        nullable = false
                                    },
                                    navArgument(Route.ManageContact.KEY_CONTACT_ID_TO_EDIT) {
                                        type = NavType.LongType
                                        nullable = false
                                        defaultValue = Route.ManageContact.NO_ID
                                    },
                                ),
                            ) { entry ->
                                ManageContactScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    },
                                    onActionCompleted = {
                                        when (val type = entry.arguments?.getInt(Route.ManageContact.KEY_SCREEN_TYPE)) {
                                            ManageContactViewModel.Type.NEW_CONTACT -> {
                                                navController.navigateWithDropCurrentScreen(
                                                    Route.PairingRequestSent.getRouteName(
                                                        receiverVeraId = it,
                                                    ),
                                                )
                                            }
                                            ManageContactViewModel.Type.EDIT_CONTACT -> {
                                                navController.popBackStack()
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = snackbarStringsProvider.contactEdited,
                                                    )
                                                }
                                            }
                                            else -> throw IllegalStateException("Unknown screen type: $type")
                                        }
                                    },
                                )
                            }
                            composable(Route.Home.name) {
                                HomeScreen(
                                    homeViewModel = homeViewModel,
                                    snackbarHostState = snackbarHostState,
                                    snackbarStringsProvider = snackbarStringsProvider,
                                    onEditContactClick = { contact ->
                                        navController.navigate(
                                            Route.ManageContact.getRouteName(
                                                screenType = ManageContactViewModel.Type.EDIT_CONTACT,
                                                currentAccountIdEncoded = uiState.currentAccount?.encodeToUTF(),
                                                contactIdToEdit = contact.id,
                                            ),
                                        )
                                    },
                                )
                            }
                            composable(Route.CreateNewMessage.name) {
                                CreateNewMessageScreen(
                                    onBackClicked = { navController.popBackStack() },
                                    onMessageSent = {
                                        navController.popBackStack()
                                        scope.launch {
                                            snackbarHostState.showSnackbar(snackbarStringsProvider.messageSent)
                                        }
                                    },
                                )
                            }
                        }
                    }
                    if (homeUiState.isAddContactFloatingMenuVisible && currentRoute == Route.Home) {
                        systemUiController.setStatusBarColor(LetroColor.statusBarUnderDialogOverlay())
                        ContactsScreenOverlayFloatingMenu(
                            homeViewModel = homeViewModel,
                            onShareIdClick = {
                                mainViewModel.onShareIdClick()
                                homeViewModel.onOptionFromContactsFloatingMenuClicked()
                            },
                            onPairWithOthersClick = {
                                navController.navigate(
                                    Route.ManageContact.getRouteName(
                                        screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                        currentAccountIdEncoded = uiState.currentAccount?.encodeToUTF(),
                                    ),
                                )
                                homeViewModel.onOptionFromContactsFloatingMenuClicked()
                            },
                        )
                    } else {
                        systemUiController.setStatusBarColor(
                            if (currentRoute.isStatusBarPrimaryColor) LetroColor.SurfaceContainerHigh else MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            },
            floatingActionButton = {
                if (
                    floatingActionButtonConfig != null &&
                    !homeUiState.isAddContactFloatingMenuVisible &&
                    currentRoute == Route.Home
                ) {
                    FloatingActionButton(
                        onClick = { homeViewModel.onFloatingActionButtonClick() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            painter = painterResource(id = floatingActionButtonConfig.icon),
                            contentDescription = stringResource(
                                id = floatingActionButtonConfig.contentDescription,
                            ),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
        )
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

        RootNavigationScreen.Home -> {
            navController.navigateWithPoppingAllBackStack(Route.Home)
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
