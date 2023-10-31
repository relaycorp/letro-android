@file:OptIn(DelicateCoroutinesApi::class)

package tech.relaycorp.letro.ui.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.StiffnessVeryLow
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.letro.account.SwitchAccountViewModel
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.registration.ui.RegistrationScreen
import tech.relaycorp.letro.account.registration.ui.UseExistingAccountScreen
import tech.relaycorp.letro.account.ui.SwitchAccountsBottomSheet
import tech.relaycorp.letro.awala.ui.error.AwalaInitializationError
import tech.relaycorp.letro.awala.ui.error.AwalaInitializationErrorViewModel.Companion.CONFIGURE_ENDPOINTS_ON_RESUME
import tech.relaycorp.letro.awala.ui.initialization.AwalaInitializationInProgress
import tech.relaycorp.letro.awala.ui.notinstalled.AwalaNotInstalledScreen
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.contacts.ManageContactViewModel
import tech.relaycorp.letro.contacts.ui.ContactsScreenOverlayFloatingMenu
import tech.relaycorp.letro.contacts.ui.ManageContactScreen
import tech.relaycorp.letro.contacts.ui.NoContactsScreen
import tech.relaycorp.letro.contacts.ui.WelcomeToLetroScreen
import tech.relaycorp.letro.conversation.attachments.dto.GsonAttachments
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel
import tech.relaycorp.letro.conversation.compose.ui.ComposeNewMessageScreen
import tech.relaycorp.letro.conversation.viewing.ui.ConversationScreen
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.main.home.HomeViewModel
import tech.relaycorp.letro.main.home.TAB_CONTACTS
import tech.relaycorp.letro.main.home.ui.HomeScreen
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.settings.SettingsScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreenUIStateModel
import tech.relaycorp.letro.ui.common.LetroTopBar
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.utils.compose.showSnackbar
import tech.relaycorp.letro.utils.navigation.navigateSingleTop
import tech.relaycorp.letro.utils.navigation.navigateWithPoppingAllBackStack
import tech.relaycorp.letro.utils.navigation.popBackStackSafe
import tech.relaycorp.letro.utils.navigation.popBackStackSafeUntil

@OptIn(FlowPreview::class)
@Composable
fun LetroNavHost(
    stringsProvider: StringsProvider,
    onGoToNotificationsSettingsClick: () -> Unit,
    onOpenAwalaClick: () -> Unit,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    switchAccountViewModel: SwitchAccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val systemUiController: SystemUiController = rememberSystemUiController()

    var currentRoute: Route by remember { mutableStateOf(Route.AwalaInitializing) }

    val uiState by mainViewModel.uiState.collectAsState()

    val switchAccountsBottomSheetState by switchAccountViewModel.switchAccountBottomSheetState.collectAsState()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val floatingActionButtonConfig = homeUiState.floatingActionButtonConfig

    val snackbarHostState = remember { SnackbarHostState() }

    var isAwalaInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route.toRoute()
            Log.d(TAG, "New route: $currentRoute")
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.rootNavigationScreen.collect { firstNavigation ->
            val rootNavigationRoute = firstNavigation.toRoute()
            val isAlreadyInBackstack = navController.currentBackStack.value.any { it.destination.route == rootNavigationRoute.name }
            Log.d(TAG, "RootNavigationCollector: isAlreadyInBackstack: $isAlreadyInBackstack; needToNavigateWithClearingBackstack=${mainViewModel.rootNavigationScreenAlreadyHandled}")
            if (mainViewModel.rootNavigationScreenAlreadyHandled || !isAlreadyInBackstack) {
                navController.navigateWithPoppingAllBackStack(
                    route = firstNavigation.toRoute(),
                )
                mainViewModel.onRootNavigationScreenHandled(firstNavigation)
            }
            if (firstNavigation != RootNavigationScreen.AwalaNotInstalled && firstNavigation != RootNavigationScreen.AwalaInitializing && firstNavigation !is RootNavigationScreen.AwalaInitializationError) {
                isAwalaInitialized = true
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.clearBackstackSignal.collect {
            Log.d(TAG, "clearing backstack until: $it")
            navController.popBackStackSafeUntil(it.toRoute())
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.createNewConversationSignal.collect {
            navController.navigate(Route.CreateNewMessage.getRouteName(ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION))
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.showNoContactsSnackbarSignal.collect {
            showSnackbar(
                scope = this,
                snackbarHostState = snackbarHostState,
                message = stringsProvider.snackbar.youNeedAtLeastOneContact,
                actionLabel = stringsProvider.snackbar.addContact,
                onActionPerformed = {
                    navController.navigate(Route.ManageContact.getRouteName(ManageContactViewModel.Type.NEW_CONTACT))
                },
            )
        }
    }

    LaunchedEffect(isAwalaInitialized) {
        if (!isAwalaInitialized) {
            return@LaunchedEffect
        }
        mainViewModel.actions.collect { action ->
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    Log.i(TAG, "New action to process: $action")
                    when (action) {
                        is Action.OpenConversation -> {
                            navController.navigate(
                                Route.Conversation.getRouteName(
                                    conversationId = action.conversationId,
                                ),
                            )
                        }
                        is Action.OpenContacts -> {
                            homeViewModel.onTabClick(TAB_CONTACTS)
                        }
                        is Action.OpenPairRequest -> {
                            if (uiState.currentAccount == null || uiState.accountStatus != AccountStatus.CREATED) {
                                Log.w(TAG, "Account is not created, so pair request screen can't be opened")
                                return@withContext
                            }
                            navController.navigate(
                                Route.ManageContact.getRouteName(
                                    screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                    prefilledContactAccountId = action.contactAccountId,
                                ),
                            )
                        }
                        is Action.OpenAccountLinking -> {
                            navController.navigateSingleTop(
                                route = Route.UseExistingAccount.getRouteName(
                                    domain = action.domain,
                                    awalaEndpoint = action.awalaEndpoint,
                                    token = action.token,
                                ),
                            )
                        }
                        is Action.OpenComposeNewMessage -> {
                            if (uiState.currentAccount == null || uiState.accountStatus != AccountStatus.CREATED) {
                                Log.w(TAG, "Account is not created, so files can't be shared")
                                return@withContext
                            }
                            if (!uiState.canSendMessages) { // TODO: move this check to the caller, and remove dependency on repository here!
                                Log.w(TAG, "User cannot send messages, so files can't be shared")
                                return@withContext
                            }
                            navController.navigate(
                                Route.CreateNewMessage.getRouteName(
                                    screenType = ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION,
                                    attachments = action.attachments,
                                    contactId = action.contactId ?: Route.CreateNewMessage.NO_ID,
                                ),
                            )
                        }
                        is Action.OpenMainPage -> {}
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
                    AnimatedVisibility(visible = currentRoute.showTopBar && currentAccount != null) {
                        if (currentAccount != null) {
                            LetroTopBar(
                                accountVeraId = currentAccount,
                                accountStatus = uiState.accountStatus,
                                domain = uiState.domain ?: "",
                                showAccountIdAsShimmer = uiState.showTopBarAccountIdAsShimmer,
                                onChangeAccountClicked = { switchAccountViewModel.onSwitchAccountsClick() },
                                onSettingsClicked = { navController.navigateSingleTop(Route.Settings) },
                            )
                        }
                    }
                    NavHost(
                        navController = navController,
                        startDestination = Route.AwalaInitializing.name,
                    ) {
                        composable(Route.AwalaNotInstalled.name) {
                            AwalaNotInstalledScreen(
                                onInstallAwalaClick = {
                                    mainViewModel.onInstallAwalaClick()
                                },
                            )
                        }
                        composable(
                            route = Route.AwalaInitializing.name,
                            exitTransition = {
                                fadeOut(
                                    animationSpec = spring(
                                        stiffness = StiffnessVeryLow,
                                    ),
                                )
                            },
                            popExitTransition = {
                                fadeOut(
                                    animationSpec = spring(
                                        stiffness = StiffnessVeryLow,
                                    ),
                                )
                            },
                        ) {
                            AwalaInitializationInProgress()
                        }
                        composable(
                            route = "${Route.AwalaInitializationError.NAME_PREFIX}${Route.AwalaInitializationError.TYPE_FATAL_ERROR}",
                        ) {
                            AwalaInitializationError(
                                type = Route.AwalaInitializationError.TYPE_FATAL_ERROR,
                            )
                        }
                        composable(
                            route = "${Route.AwalaInitializationError.NAME_PREFIX}${Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR}",
                        ) {
                            AwalaInitializationError(
                                type = Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR,
                            )
                        }
                        composable(
                            route = "${Route.AwalaInitializationError.NAME_PREFIX}${Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA}",
                            arguments = listOf(
                                navArgument(CONFIGURE_ENDPOINTS_ON_RESUME) {
                                    this.type = NavType.BoolType
                                    defaultValue = true
                                    nullable = false
                                },
                            ),
                        ) {
                            AwalaInitializationError(
                                type = Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA,
                                onOpenAwalaClick = onOpenAwalaClick,
                            )
                        }
                        composable(
                            route = Route.Registration.name +
                                "?${Route.Registration.WITH_BACK_BUTTON}={${Route.Registration.WITH_BACK_BUTTON}}",
                            arguments = listOf(
                                navArgument(
                                    name = Route.Registration.WITH_BACK_BUTTON,
                                ) {
                                    type = NavType.BoolType
                                    defaultValue = false
                                },
                            ),
                        ) {
                            val withBackButton = it.arguments?.getBoolean(Route.Registration.WITH_BACK_BUTTON, false) ?: false
                            RegistrationScreen(
                                onUseExistingAccountClick = { navController.navigateSingleTop(Route.UseExistingAccount) },
                                showSnackbar = {
                                    showSnackbar(
                                        string = it,
                                        snackbarHostState = snackbarHostState,
                                        scope = scope,
                                        stringsProvider = stringsProvider.snackbar,
                                    )
                                },
                                onBackClick = if (withBackButton) {
                                    { navController.popBackStackSafe() }
                                } else {
                                    null
                                },
                            )
                        }
                        composable(Route.AccountCreationFailed.name) {
                            ActionTakingScreen(
                                model = ActionTakingScreenUIStateModel.AccountCreationFailed(
                                    domain = uiState.domain ?: "",
                                ),
                            )
                        }
                        composable("${Route.WelcomeToLetro.ROUTE_NAME_PREFIX}${true}") {
                            WelcomeToLetroScreen(
                                withConfettiAnimation = true,
                                onPairWithOthersClick = {
                                    navController.navigate(
                                        Route.ManageContact.getRouteName(
                                            screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                        ),
                                    )
                                },
                                onShareIdClick = {
                                    mainViewModel.onShareIdClick()
                                },
                            )
                        }
                        composable("${Route.WelcomeToLetro.ROUTE_NAME_PREFIX}${false}") {
                            WelcomeToLetroScreen(
                                withConfettiAnimation = false,
                                onPairWithOthersClick = {
                                    navController.navigate(
                                        Route.ManageContact.getRouteName(
                                            screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                        ),
                                    )
                                },
                                onShareIdClick = {
                                    mainViewModel.onShareIdClick()
                                },
                            )
                        }
                        composable(Route.NoContacts.name) {
                            NoContactsScreen(
                                onPairWithOthersClick = {
                                    navController.navigate(
                                        Route.ManageContact.getRouteName(
                                            screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                        ),
                                    )
                                },
                                onShareIdClick = {
                                    mainViewModel.onShareIdClick()
                                },
                            )
                        }
                        composable(Route.AccountCreationWaiting.name) {
                            ActionTakingScreen(
                                model = ActionTakingScreenUIStateModel.AccountCreation,
                            )
                        }
                        composable(Route.AccountLinkingWaiting.name) {
                            ActionTakingScreen(
                                model = ActionTakingScreenUIStateModel.AccountLinking,
                            )
                        }
                        composable(
                            route = Route.UseExistingAccount.name +
                                "?${Route.UseExistingAccount.DOMAIN_ENCODED}={${Route.UseExistingAccount.DOMAIN_ENCODED}}" +
                                "&${Route.UseExistingAccount.AWALA_ENDPOINT_ENCODED}={${Route.UseExistingAccount.AWALA_ENDPOINT_ENCODED}}" +
                                "&${Route.UseExistingAccount.TOKEN_ENCODED}={${Route.UseExistingAccount.TOKEN_ENCODED}}",
                        ) {
                            UseExistingAccountScreen(
                                onBackClick = { navController.popBackStackSafe() },
                                showSnackbar = {
                                    showSnackbar(
                                        string = it,
                                        snackbarHostState = snackbarHostState,
                                        scope = scope,
                                        stringsProvider = stringsProvider.snackbar,
                                    )
                                },
                            )
                        }
                        composable(
                            route = Route.ManageContact.name +
                                "?${Route.ManageContact.KEY_SCREEN_TYPE}={${Route.ManageContact.KEY_SCREEN_TYPE}}" +
                                "&${Route.ManageContact.KEY_CONTACT_ID_TO_EDIT}={${Route.ManageContact.KEY_CONTACT_ID_TO_EDIT}}" +
                                "&${Route.ManageContact.KEY_PREFILLED_ACCOUNT_ID_ENCODED}={${Route.ManageContact.KEY_PREFILLED_ACCOUNT_ID_ENCODED}}",
                            arguments = listOf(
                                navArgument(Route.ManageContact.KEY_SCREEN_TYPE) {
                                    type = NavType.IntType
                                    nullable = false
                                },
                                navArgument(Route.ManageContact.KEY_CONTACT_ID_TO_EDIT) {
                                    type = NavType.LongType
                                    nullable = false
                                    defaultValue = Route.ManageContact.NO_ID
                                },
                                navArgument(Route.ManageContact.KEY_PREFILLED_ACCOUNT_ID_ENCODED) {
                                    type = NavType.StringType
                                    nullable = true
                                },
                            ),
                        ) { entry ->
                            ManageContactScreen(
                                onBackClick = {
                                    navController.popBackStackSafe()
                                },
                                onEditContactCompleted = {
                                    when (val type = entry.arguments?.getInt(Route.ManageContact.KEY_SCREEN_TYPE)) {
                                        ManageContactViewModel.Type.EDIT_CONTACT -> {
                                            navController.popBackStackSafe()
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
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            onGoToNotificationsSettingsClick()
                                        }
                                    }
                                },
                                showSnackbar = {
                                    showSnackbar(
                                        string = it,
                                        snackbarHostState = snackbarHostState,
                                        scope = scope,
                                        stringsProvider = stringsProvider.snackbar,
                                    )
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
                                onStartConversationClick = {
                                    navController.navigate(
                                        Route.CreateNewMessage.getRouteName(
                                            screenType = ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION,
                                            contactId = it.id,
                                        ),
                                    )
                                },
                                onPairWithOthersClick = {
                                    navController.navigate(
                                        Route.ManageContact.getRouteName(
                                            screenType = ManageContactViewModel.Type.NEW_CONTACT,
                                        ),
                                    )
                                },
                                onShareIdClick = {
                                    mainViewModel.onShareIdClick()
                                },
                            )
                        }
                        composable(
                            route = Route.CreateNewMessage.name +
                                "?${Route.CreateNewMessage.KEY_SCREEN_TYPE}={${Route.CreateNewMessage.KEY_SCREEN_TYPE}}" +
                                "&${Route.CreateNewMessage.KEY_ATTACHMENTS}={${Route.CreateNewMessage.KEY_ATTACHMENTS}}" +
                                "&${Route.CreateNewMessage.KEY_CONTACT_ID}={${Route.CreateNewMessage.KEY_CONTACT_ID}}" +
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
                                navArgument(Route.CreateNewMessage.KEY_CONTACT_ID) {
                                    type = NavType.LongType
                                    nullable = false
                                    defaultValue = Route.CreateNewMessage.NO_ID
                                },
                                navArgument(Route.CreateNewMessage.KEY_ATTACHMENTS) {
                                    type = GsonAttachments.NavType
                                    nullable = false
                                    defaultValue = GsonAttachments(emptyList())
                                },
                            ),
                        ) {
                            val screenType = it.arguments?.getInt(Route.CreateNewMessage.KEY_SCREEN_TYPE)
                            ComposeNewMessageScreen(
                                conversationsStringsProvider = stringsProvider.conversations,
                                goBack = { navController.popBackStackSafe() },
                                onMessageSent = {
                                    when (screenType) {
                                        ComposeNewMessageViewModel.ScreenType.REPLY_TO_EXISTING_CONVERSATION -> {
                                            navController.popBackStack(
                                                route = Route.Home.name,
                                                inclusive = false,
                                            )
                                        }
                                        ComposeNewMessageViewModel.ScreenType.NEW_CONVERSATION -> {
                                            navController.popBackStackSafe()
                                        }
                                    }
                                    snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.messageSent)
                                },
                                showSnackbar = {
                                    showSnackbar(
                                        string = it,
                                        snackbarHostState = snackbarHostState,
                                        scope = scope,
                                        stringsProvider = stringsProvider.snackbar,
                                    )
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
                                    navController.popBackStackSafe()
                                    snackbarHostState.showSnackbar(scope, stringsProvider.snackbar.conversationDeleted)
                                },
                                onConversationArchived = { isArchived ->
                                    navController.popBackStackSafe()
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
                                    navController.popBackStackSafe()
                                },
                                onAttachmentClick = { fileId ->
                                    mainViewModel.onAttachmentClick(fileId)
                                },
                                showAddContactSnackbar = {
                                    showSnackbar(
                                        scope = scope,
                                        snackbarHostState = snackbarHostState,
                                        message = stringsProvider.snackbar.youNoLongerConnected,
                                        actionLabel = stringsProvider.snackbar.addContact,
                                        onActionPerformed = {
                                            navController.navigate(Route.ManageContact.getRouteName(ManageContactViewModel.Type.NEW_CONTACT))
                                        },
                                    )
                                },
                            )
                        }
                        composable(Route.Settings.name) {
                            SettingsScreen(
                                onAddAccountClick = { navController.navigate(Route.Registration.getRouteName(withBackButton = true)) },
                                onNotificationsClick = onGoToNotificationsSettingsClick,
                                onTermsAndConditionsClick = { mainViewModel.onTermsAndConditionsClick() },
                                onBackClick = { navController.popBackStackSafe() },
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

private fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String,
    onActionPerformed: () -> Unit,
) {
    scope.launch {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            onActionPerformed()
        }
    }
}

private fun showSnackbar(
    string: SnackbarString,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    stringsProvider: SnackbarStringsProvider,
) {
    snackbarHostState.showSnackbar(scope, stringsProvider.get(string))
}

private const val TAG = "LetroNavHost"
