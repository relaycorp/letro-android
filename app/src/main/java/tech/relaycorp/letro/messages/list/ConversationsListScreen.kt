package tech.relaycorp.letro.messages.list

import androidx.compose.foundation.Image
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

@Composable
fun ConversationsListScreen(
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
                items(conversations) {
                    Conversation(conversation = it)
                }
            }
        }
    }
}

@Composable
private fun Conversation(
    conversation: ExtendedConversation,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = conversation.recipientAlias ?: conversation.recipientVeraId,
                    style = MaterialTheme.typography.LargeProminent,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = conversation.lastMessageFormattedTimestamp,
                    style = MaterialTheme.typography.SmallProminent,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row {
                if (conversation.subject != null) {
                    Text(
                        text = conversation.subject,
                        style = MaterialTheme.typography.MediumProminent,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = " - ",
                        style = MaterialTheme.typography.MediumProminent,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = conversation.messages.last().text,
                    style = MaterialTheme.typography.MediumProminent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
