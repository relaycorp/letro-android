package tech.relaycorp.letro.messages.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.ui.theme.LargeProminent
import tech.relaycorp.letro.ui.theme.MediumProminent
import tech.relaycorp.letro.ui.theme.SmallProminent
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider

@Composable
fun ConversationsListScreen(
    conversationsStringsProvider: ConversationsStringsProvider,
    onConversationClick: (ExtendedConversation) -> Unit,
    viewModel: ConversationsViewModel,
) {
    val conversations by viewModel.conversations.collectAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        if (conversations.isEmpty()) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                EmptyConversationsView()
            }
        } else {
            LazyColumn {
                items(conversations) { conversation ->
                    Conversation(
                        conversation = conversation,
                        noSubjectText = conversationsStringsProvider.noSubject,
                        onConversationClick = {
                            onConversationClick(conversation)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Conversation(
    conversation: ExtendedConversation,
    noSubjectText: String,
    onConversationClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConversationClick() }
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = conversation.contactDisplayName,
                    style = if (!conversation.isRead) MaterialTheme.typography.LargeProminent else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = conversation.lastMessageFormattedTimestamp,
                    style = if (!conversation.isRead) MaterialTheme.typography.SmallProminent else MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            Row {
                Text(
                    text = conversation.subject ?: noSubjectText,
                    style = if (!conversation.isRead) MaterialTheme.typography.MediumProminent else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = " - ",
                    style = if (!conversation.isRead) MaterialTheme.typography.MediumProminent else MaterialTheme.typography.bodyMedium,
                    color = if (!conversation.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = conversation.messages.last().text,
                    style = if (!conversation.isRead) MaterialTheme.typography.MediumProminent else MaterialTheme.typography.bodyMedium,
                    color = if (!conversation.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EmptyConversationsView() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter = painterResource(id = R.drawable.empty_inbox_image), contentDescription = null)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.conversations_empty_stub),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
