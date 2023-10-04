@file:OptIn(DelicateCoroutinesApi::class)

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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.SwitchAccountViewModel
import tech.relaycorp.letro.account.registration.ui.RegistrationScreen
import tech.relaycorp.letro.account.ui.SwitchAccountsBottomSheet
import tech.relaycorp.letro.awala.ui.error.AwalaInitializationError
import tech.relaycorp.letro.awala.ui.initialization.AwalaInitializationInProgress
import tech.relaycorp.letro.awala.ui.notinstalled.AwalaNotInstalledScreen
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.contacts.ui.ContactsScreenOverlayFloatingMenu
import tech.relaycorp.letro.contacts.ui.ManageContactScreen
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel
import tech.relaycorp.letro.conversation.compose.ui.ComposeNewMessageScreen
import tech.relaycorp.letro.conversation.viewing.ui.ConversationScreen
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.main.home.HomeViewModel
import tech.relaycorp.letro.main.home.TAB_CONTACTS
import tech.relaycorp.letro.main.home.ui.HomeScreen
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.push.model.PushAction
import tech.relaycorp.letro.settings.SettingsScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.common.LetroTopBar
import tech.relaycorp.letro.ui.common.SplashScreen
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.utils.compose.navigation.navigateSingleTop
import tech.relaycorp.letro.utils.compose.navigation.navigateWithPoppingAllBackStack
import tech.relaycorp.letro.utils.compose.showSnackbar
import tech.relaycorp.letro.utils.ext.encodeToUTF

@Composable
fun LetroNavHost(
    stringsProvider: StringsProvider,
    onGoToNotificationsSettingsClick: () -> Unit,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    switchAccountViewModel: SwitchAccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val systemUiController: SystemUiController = rememberSystemUiController()
    var currentRoute: Route by remember { mutableStateOf(Route.Splash) }

    val uiState by mainViewModel.uiState.collectAsState()

    val switchAccountsBottomSheetState by switchAccountViewModel.switchAccountBottomSheetState.collectAsState()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val floatingActionButtonConfig = homeUiState.floatingActionButtonConfig

    val snackbarHostState = remember { SnackbarHostState() }

    var isHomeScreenInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route.toRoute()
            Log.d("LetroNavHost", "New route: $currentRoute")
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.rootNavigationScreen.collect { firstNavigation ->
            handleFirstNavigation(navController, firstNavigation)
            if (firstNavigation == RootNavigationScreen.Home) {
                isHomeScreenInitialized = true
            }
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.createNewConversationSignal.collect {
            navController.navigate(Route.CreateNewMessage.getRouteName(ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION))
        }
    }

    LaunchedEffect(isHomeScreenInitialized) {
        if (!isHomeScreenInitialized) {
            return@LaunchedEffect
        }
        mainViewModel.pushAction.collect { pushAction ->
            withContext(Dispatchers.IO) {
                when (pushAction) {
                    is PushAction.OpenConversation -> {
                        GlobalScope.launch(Dispatchers.Main) {
                            navController.navigate(
                                Route.Conversation.getRouteName(
                                    conversationId = pushAction.conversationId,
                                ),
                            )
                        }
                        switchAccountViewModel.onSwitchAccountRequested(pushAction.accountId)
                    }
                    is PushAction.OpenMainPage -> {
                        switchAccountViewModel.onSwitchAccountRequested(pushAction.accountId)
                    }
                }
            }
        }
    }

    val currentAccount = uiState.currentAccount
    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                if (switchAccountsBottomSheetState.isShown) {
                    SwitchAccountsBottomSheet(
                        accounts = switchAccountsBottomSheetState.accounts,
                        onAccountClick = { switchAccountViewModel.onSwitchAccountRequested(it) },
                        onManageContactsClick = {
                            navController.navigateSingleTop(Route.Settings)
                            switchAccountViewModel.onSwitchAccountDialogDismissed()
                        },
                        onDismissRequest = { switchAccountViewModel.onSwitchAccountDialogDismissed() },
                    )
                }
                Column {
                    if (currentRoute.showTopBar && currentAccount != null) {
                        LetroTopBar(
                            accountVeraId = currentAccount,
                            isAccountCreated = uiState.isCurrentAccountCreated,
                            onChangeAccountClicked = { switchAccountViewModel.onSwitchAccountsClick() },
                            onSettingsClicked = { navController.navigateSingleTop(Route.Settings) },
                        )
                    }
                    NavHost(
                        navController = navController,
                        startDestination = Route.Splash.name,
                    ) {
                        composable(Route.AwalaNotInstalled.name) {
                            AwalaNotInstalledScreen(
                                awalaInitializationTexts = stringsProvider.awalaInitializationStringsProvider.awalaInitializationAmusingTexts,
                                onInstallAwalaClick = {
                                    mainViewModel.onInstallAwalaClick()
                                },
                            )
                        }
                        composable(Route.AwalaInitializing.name) {
                            AwalaInitializationInProgress(texts = stringsProvider.awalaInitializationStringsProvider.awalaInitializationAmusingTexts)
                        }
                        composable(
                            route = "${Route.AwalaInitializationError.NAME_PREFIX}${Route.AwalaInitializationError.FATAL_SUFFIX}",
                        ) {
                            AwalaInitializationError(isFatal = true)
                        }
                        composable(
                            route = "${Route.AwalaInitializationError.NAME_PREFIX}${Route.AwalaInitializationError.NONFATAL_SUFFIX}",
                        ) {
                            AwalaInitializationError(isFatal = false)
                        }
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
                                onEditContactCompleted = {
                                    when (val type = entry.arguments?.getInt(Route.ManageContact.KEY_SCREEN_TYPE)) {
                                        ManageContactViewModel.Type.EDIT_CONTACT -> {
                                            navController.popBackStack()
                                            snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.contactEdited)
                                        }
                                        else -> throw IllegalStateException("Unknown screen type: $type")
                                    }
                                },
                                showGoToSettingsPermissionSnackbar = {
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = stringsProvider.snackbar.notificationPermissionDenied,
                                            actionLabel = stringsProvider.snackbar.goToSettings,
                                            duration = SnackbarDuration.Long,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onGoToNotificationsSettingsClick()
                                        }
                                    }
                                },
                            )
                        }
                        composable(Route.Home.name) {
                            HomeScreen(
                                homeViewModel = homeViewModel,
                                snackbarHostState = snackbarHostState,
                                stringsProvider = stringsProvider,
                                onConversationClick = {
                                    navController.navigate(
                                        Route.Conversation.getRouteName(
                                            conversationId = it.conversationId.toString(),
                                        ),
                                    )
                                },
                                onEditContactClick = { contact ->
                                    navController.navigate(
                                        Route.ManageContact.getRouteName(
                                            screenType = ManageContactViewModel.Type.EDIT_CONTACT,
                                            currentAccountIdEncoded = uiState.currentAccount?.encodeToUTF(),
                                            contactIdToEdit = contact.id,
                                        ),
                                    )
                                },
                                onNotificationsAction = { notificationsAction ->
                                    when (notificationsAction) {
                                        NotificationClickAction.OpenContacts -> {
                                            homeViewModel.onTabClick(TAB_CONTACTS)
                                        }
                                    }
                                },
                            )
                        }
                        composable(
                            route = "${Route.CreateNewMessage.name}" +
                                "?${Route.CreateNewMessage.KEY_SCREEN_TYPE}={${Route.CreateNewMessage.KEY_SCREEN_TYPE}}" +
                                "&${Route.CreateNewMessage.KEY_CONVERSATION_ID}={${Route.CreateNewMessage.KEY_CONVERSATION_ID}}",
                            arguments = listOf(
                                navArgument(Route.CreateNewMessage.KEY_SCREEN_TYPE) {
                                    type = NavType.IntType
                                    nullable = false
                                },
                                navArgument(Route.CreateNewMessage.KEY_CONVERSATION_ID) {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                            ),
                        ) {
                            val screenType = it.arguments?.getInt(Route.CreateNewMessage.KEY_SCREEN_TYPE)
                            ComposeNewMessageScreen(
                                conversationsStringsProvider = stringsProvider.conversations,
                                onBackClicked = { navController.popBackStack() },
                                onMessageSent = {
                                    when (screenType) {
                                        ComposeNewMessageViewModel.ScreenType.REPLY_TO_EXISTING_CONVERSATION -> {
                                            navController.popBackStack(
                                                route = Route.Home.name,
                                                inclusive = false,
                                            )
                                        }
                                        ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION -> {
                                            navController.popBackStack()
                                        }
                                    }
                                    snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.messageSent)
                                },
                            )
                        }
                        composable(
                            route = "${Route.Conversation.name}/{${Route.Conversation.KEY_CONVERSATION_ID}}",
                            arguments = listOf(
                                navArgument(Route.Conversation.KEY_CONVERSATION_ID) {
                                    type = NavType.StringType
                                    nullable = false
                                },
                            ),
                        ) {
                            val conversationId = it.arguments?.getString(Route.Conversation.KEY_CONVERSATION_ID)
                            ConversationScreen(
                                conversationsStringsProvider = stringsProvider.conversations,
                                onConversationDeleted = {
                                    navController.popBackStack()
                                    snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.conversationDeleted)
                                },
                                onConversationArchived = { isArchived ->
                                    navController.popBackStack()
                                    snackbarHostState.showSnackbar(scope, if (isArchived) stringsProvider.snackbar.conversationArchived else stringsProvider.snackbar.conversationUnarchived)
                                },
                                onReplyClick = {
                                    navController.navigate(
                                        route = Route.CreateNewMessage.getRouteName(
                                            screenType = ComposeNewMessageViewModel.ScreenType.REPLY_TO_EXISTING_CONVERSATION,
                                            conversationId = conversationId,
                                        ),
                                    )
                                },
                                onBackClicked = {
                                    navController.popBackStack()
                                },
                                onAttachmentClick = { fileId ->
                                    mainViewModel.onAttachmentClick(fileId)
                                },
                            )
                        }
                        composable(Route.Settings.name) {
                            SettingsScreen(
                                onAddAccountClick = { navController.navigate(Route.Registration.name) },
                                onNotificationsClick = onGoToNotificationsSettingsClick,
                                onTermsAndConditionsClick = { mainViewModel.onTermsAndConditionsClick() },
                                onBackClick = { navController.popBackStack() },
                                onAccountDeleted = {
                                    snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.accountDeleted)
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
                        onOutsideClick = {
                            homeViewModel.onOutsideFloatingMenuClicked()
                        },
                    )
                } else {
                    systemUiController.isStatusBarVisible = currentRoute.isStatusBarVisible
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
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        actionOnNewLine = it.visuals.message.length >= 50,
                    )
                },
            )
        },
    )
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

        RootNavigationScreen.AwalaNotInstalled -> {
            navController.navigateWithPoppingAllBackStack(Route.AwalaNotInstalled)
        }

        RootNavigationScreen.AwalaInitializing -> {
            navController.navigateWithPoppingAllBackStack(Route.AwalaInitializing)
        }

        is RootNavigationScreen.AwalaInitializationError -> {
            navController.navigateWithPoppingAllBackStack(
                Route.AwalaInitializationError(
                    isFatal = firstNavigation.isFatal,
                ),
            )
        }
    }
}
