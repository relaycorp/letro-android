package tech.relaycorp.letro.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.ui.ContactsScreen
import tech.relaycorp.letro.home.tabs.LetroTabs
import tech.relaycorp.letro.messages.list.ConversationsListScreen
import tech.relaycorp.letro.messages.list.ConversationsListViewModel
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.notification.NotificationsScreen
import tech.relaycorp.letro.notification.NotificationsViewModel
import tech.relaycorp.letro.ui.utils.StringsProvider

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    stringsProvider: StringsProvider,
    onConversationClick: (ExtendedConversation) -> Unit,
    onEditContactClick: (Contact) -> Unit,
    snackbarHostState: SnackbarHostState,
    conversationsListViewModel: ConversationsListViewModel = hiltViewModel(),
    contactsViewModel: ContactsViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Box {
        Column {
            LetroTabs(
                viewModel = homeViewModel,
            )
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                when (uiState.currentTab) {
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
                        onEditContactClick = { onEditContactClick(it) },
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
