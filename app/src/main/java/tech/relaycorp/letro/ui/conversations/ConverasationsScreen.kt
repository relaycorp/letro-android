package tech.relaycorp.letro.ui.conversations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.theme.Grey90
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding
import tech.relaycorp.letro.ui.theme.LetroTheme

@Composable
fun ConversationsRoute(
    onChangeConversationsTypeClicked: () -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val conversations by viewModel.conversationsUIFlow.collectAsState()

    ConversationsScreen(
        onChangeConversationsTypeClicked = onChangeConversationsTypeClicked,
        conversations = conversations,
    )
}

@Composable
private fun ConversationsScreen(
    onChangeConversationsTypeClicked: () -> Unit,
    conversations: List<ConversationUIModel>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = HorizontalScreenPadding,
                    vertical = ItemPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.settings), // TODO Replace with inbox
                contentDescription = null, // TODO Add inbox content description
            )
            Text(
                text = stringResource(id = R.string.conversations_inbox),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onChangeConversationsTypeClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_down),
                    contentDescription = stringResource(id = R.string.conversations_change_conversation_type),
                )
            }
        }
        LazyColumn {
            items(conversations) { conversation ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = HorizontalScreenPadding,
                            vertical = ItemPadding,
                        ),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = conversation.contact + conversation.numberOfMessages,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.width(HorizontalScreenPadding))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = conversation.lastMessageTime,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = conversation.subject,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = " - " + conversation.lastMessageText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Grey90,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationsScreenPreview() {
    LetroTheme {
        ConversationsScreen(
            onChangeConversationsTypeClicked = {},
            conversations = listOf(
                ConversationUIModel(
                    contact = "James Bond",
                    numberOfMessages = 1,
                    subject = "Latest AI design resources",
                    lastMessageTime = "10:05am",
                    lastMessageText = "Hey, I thought you were going to do the designs by 1pm.",
                    isRead = false,
                ),
                ConversationUIModel(
                    contact = "Ana Garcia",
                    numberOfMessages = 1,
                    subject = "Web accessibility (article review)",
                    lastMessageTime = "08:16am",
                    lastMessageText = "Hi, just wanted to start the convo about this",
                    isRead = true,
                ),
                ConversationUIModel(
                    contact = "richardosisondahouse@applepie.fans",
                    numberOfMessages = 1,
                    subject = "First email from letro",
                    lastMessageTime = "10:05am",
                    lastMessageText = "Hey, have you come seen the way we encrypt our messages?",
                    isRead = false,
                ),
                ConversationUIModel(
                    contact = "Ana Garcia",
                    numberOfMessages = 3,
                    subject = "Artsy community meetup",
                    lastMessageTime = "08:16am",
                    lastMessageText = "Hey, would you like to join us this Saturday?",
                    isRead = true,
                ),
            ),
        )
    }
}
