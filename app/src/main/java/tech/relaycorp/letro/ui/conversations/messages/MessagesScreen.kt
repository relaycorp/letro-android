package tech.relaycorp.letro.ui.conversations.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.data.MessageDataModel
import tech.relaycorp.letro.ui.custom.LetroButton
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.ItemPadding

@Composable
fun MessagesRoute(
    onBackClicked: () -> Unit,
    onReplyClicked: () -> Unit,
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    val messages by viewModel.messagesDataFlow.collectAsState()
    MessagesScreen(
        messages = messages,
        onBackClicked = onBackClicked,
        onReplyClicked = onReplyClicked,
    )
}

@Composable
private fun MessagesScreen(
    messages: List<MessageDataModel>,
    onBackClicked: () -> Unit,
    onReplyClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.general_navigate_back),
                )
            }
            LetroButton(
                modifier = Modifier.fillMaxWidth(0f),
                leadingIconResId = R.drawable.reply,
                text = stringResource(id = R.string.messages_reply),
                onClick = onReplyClicked,
            )
        }
        LazyColumn {
            items(messages) { message ->
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
                            text = message.sender,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.width(HorizontalScreenPadding))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = message.timestamp.toString(), // TODO Beautify
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                        )
                    }
                    Text(
                        text = message.body,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagesScreenPreview() {
    MessagesScreen(emptyList(), {}, {})
}
