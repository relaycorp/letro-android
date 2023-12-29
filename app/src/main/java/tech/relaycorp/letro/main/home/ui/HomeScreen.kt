package tech.relaycorp.letro.main.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.ui.ContactsScreen
import tech.relaycorp.letro.conversation.list.ConversationsListViewModel
import tech.relaycorp.letro.conversation.list.ui.ConversationsListScreen
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.main.home.HomeViewModel
import tech.relaycorp.letro.main.home.TABS_COUNT
import tech.relaycorp.letro.main.home.TAB_CHATS
import tech.relaycorp.letro.main.home.TAB_CONTACTS
import tech.relaycorp.letro.main.home.TAB_NOTIFICATIONS
import tech.relaycorp.letro.notification.NotificationsViewModel
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.notification.ui.NotificationsScreen
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.utils.compose.showSnackbar

@OptIn(ExperimentalFoundationApi::class)
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

    val pagerState = rememberPagerState(pageCount = { TABS_COUNT })

    LaunchedEffect(uiState.currentTab) {
        pagerState.animateScrollToPage(uiState.currentTab)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            homeViewModel.onTabClick(page)
        }
    }

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxSize(),
    ) { currentPage ->
        when (currentPage) {
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
