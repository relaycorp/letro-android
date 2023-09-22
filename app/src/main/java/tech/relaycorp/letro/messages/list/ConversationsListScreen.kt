package tech.relaycorp.letro.messages.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.model.ExtendedMessage
import tech.relaycorp.letro.ui.theme.BodyLargeProminent
import tech.relaycorp.letro.ui.theme.BodyMediumProminent
import tech.relaycorp.letro.ui.theme.LabelSmallProminent
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import java.util.UUID

@Composable
fun ConversationsListScreen(
    conversationsStringsProvider: ConversationsStringsProvider,
    onConversationClick: (ExtendedConversation) -> Unit,
    viewModel: ConversationsListViewModel,
) {
    val conversations by viewModel.conversations.collectAsState()
    val isOnboardingVisible by viewModel.isOnboardingMessageVisible.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isOnboardingVisible) {
            ConversationsOnboardingView(
                onCloseClick = { viewModel.onCloseOnboardingButtonClick() },
            )
        }
        Box {
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
                    style = if (!conversation.isRead) MaterialTheme.typography.BodyLargeProminent else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (conversation.totalMessagesFormattedText != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = conversation.totalMessagesFormattedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(
                                top = if (conversation.isRead) 3.dp else 1.dp,
                            ),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = conversation.lastMessageFormattedTimestamp,
                    style = if (!conversation.isRead) MaterialTheme.typography.LabelSmallProminent else MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            Row {
                Text(
                    text = conversation.subject ?: noSubjectText,
                    style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = " - ",
                    style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
                    color = if (!conversation.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = conversation.messages.last().text,
                    style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
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

@Composable
private fun ConversationsOnboardingView(
    onCloseClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.conversations_onboarding_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(24.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_14),
                contentDescription = stringResource(id = R.string.close_onboarding_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Conversation_Preview() {
    val conversationId = UUID.randomUUID()
    val message = ExtendedMessage(
        conversationId = conversationId,
        senderVeraId = "ft@applepie.rocks",
        recipientVeraId = "contact@vera.id",
        senderDisplayName = "Sender",
        recipientDisplayName = "Recipient",
        isOutgoing = true,
        contactDisplayName = "Alias",
        text = "Hello man!",
        sentAtBriefFormatted = "15 Aug",
        sentAtDetailedFormatted = "15 Aug 2023, 10:06am",
    )
    Column {
        Conversation(
            conversation = ExtendedConversation(
                conversationId = conversationId,
                ownerVeraId = "ft@applepie.rocks",
                contactVeraId = "contact@vera.id",
                contactDisplayName = "Alias",
                subject = "Subject Preview",
                lastMessageTimestamp = System.currentTimeMillis(),
                isRead = false,
                lastMessageFormattedTimestamp = "01:03 PM",
                lastMessage = message,
                totalMessagesFormattedText = "(2)",
                messages = listOf(
                    message,
                ),
            ),
            noSubjectText = "(No subject)",
        ) {
        }
        Divider(
            modifier = Modifier.height(1.dp),
        )
        Conversation(
            conversation = ExtendedConversation(
                conversationId = conversationId,
                ownerVeraId = "ft@applepie.rocks",
                contactVeraId = "contact@vera.id",
                contactDisplayName = "Alias",
                subject = "Subject Preview",
                lastMessageTimestamp = System.currentTimeMillis(),
                isRead = true,
                lastMessageFormattedTimestamp = "01:03 PM",
                lastMessage = message,
                totalMessagesFormattedText = "(2)",
                messages = listOf(
                    message,
                ),
            ),
            noSubjectText = "(No subject)",
        ) {
        }
    }
}

@Preview
@Composable
private fun Onboarding_Preview() {
    ConversationsOnboardingView {
    }
}
