package tech.relaycorp.letro.main.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.ui.ContactsScreen
import tech.relaycorp.letro.conversation.list.ConversationsListViewModel
import tech.relaycorp.letro.conversation.list.ui.ConversationsListScreen
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.main.home.HomeViewModel
import tech.relaycorp.letro.main.home.TAB_CHATS
import tech.relaycorp.letro.main.home.TAB_CONTACTS
import tech.relaycorp.letro.main.home.TAB_NOTIFICATIONS
import tech.relaycorp.letro.notification.NotificationsViewModel
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.notification.ui.NotificationsScreen
import tech.relaycorp.letro.ui.common.animation.swipeAnimation
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.utils.compose.showSnackbar

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    stringsProvider: StringsProvider,
    onConversationClick: (ExtendedConversation) -> Unit,
    onEditContactClick: (Contact) -> Unit,
    onNotificationsAction: (NotificationClickAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onPairWithOthersClick: () -> Unit,
    onShareIdClick: () -> Unit,
    onStartConversationClick: (Contact) -> Unit,
    conversationsListViewModel: ConversationsListViewModel,
    contactsViewModel: ContactsViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        notificationsViewModel.actions.collect {
            onNotificationsAction(it)
        }
    }

    AnimatedContent(
        targetState = uiState.currentTab,
        transitionSpec = { swipeAnimation() },
        label = "HomeScreenContent",
        modifier = Modifier.fillMaxSize(),
    ) { currentTab ->
        when (currentTab) {
            TAB_CHATS -> Column {
                ConversationsListScreen(
                    conversationsStringsProvider = stringsProvider.conversations,
                    openConversation = onConversationClick,
                    viewModel = conversationsListViewModel,
                    showSnackbar = { snackbarHostState.showSnackbar(scope, stringsProvider.snackbar, it) },
                )
            }
            TAB_CONTACTS -> ContactsScreen(
                viewModel = contactsViewModel,
                snackbarHostState = snackbarHostState,
                snackbarStringsProvider = stringsProvider.snackbar,
                onEditContactClick = onEditContactClick,
                onStartConversationClick = onStartConversationClick,
                onPairWithOthersClick = onPairWithOthersClick,
                onShareIdClick = onShareIdClick,
            )
            TAB_NOTIFICATIONS -> NotificationsScreen(
                viewModel = notificationsViewModel,
            )
            else -> throw IllegalStateException("Unsupported tab with index ${uiState.currentTab}")
        }
    }
}
