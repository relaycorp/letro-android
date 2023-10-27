package tech.relaycorp.letro.main.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import tech.relaycorp.letro.main.home.ui.tabs.LetroTabs
import tech.relaycorp.letro.notification.NotificationsViewModel
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.notification.ui.NotificationsScreen
import tech.relaycorp.letro.ui.common.animation.swipeAnimation
import tech.relaycorp.letro.ui.utils.StringsProvider

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
    conversationsListViewModel: ConversationsListViewModel = hiltViewModel(),
    contactsViewModel: ContactsViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        notificationsViewModel.actions.collect {
            onNotificationsAction(it)
        }
    }

    Box {
        Column {
            LetroTabs(
                viewModel = homeViewModel,
            )
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = { swipeAnimation() },
                    label = "HomeScreenContent",
                ) { currentTab ->
                    when (currentTab) {
                        TAB_CHATS -> Column {
                            ConversationsListScreen(
                                conversationsStringsProvider = stringsProvider.conversations,
                                onConversationClick = onConversationClick,
                                viewModel = conversationsListViewModel,
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
        }
    }
}
