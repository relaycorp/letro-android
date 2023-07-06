package tech.relaycorp.letro.ui.conversations.newMessage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.custom.LetroButton
import tech.relaycorp.letro.ui.custom.LetroTextField
import tech.relaycorp.letro.ui.theme.Grey90

@Composable
fun NewMessageRoute(
    onBackClicked: () -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel(),
) {
    val conversationDataModel by viewModel.currentConversationDataFlow.collectAsState()

    NewMessageScreen(
        sender = conversationDataModel.sender,
        recipient = conversationDataModel.recipient,
        subject = conversationDataModel.subject,
        body = conversationDataModel.messages.last().body,
        onRecipientInput = viewModel::onRecipientInput,
        onBodyInput = viewModel::onContentInput,
        onSubjectInput = viewModel::onSubjectInput,
        onBackClicked = onBackClicked,
        onAttachmentClicked = viewModel::onAttachmentClicked,
        onSendClicked = viewModel::onSendClicked,
    )
}

@Composable
private fun NewMessageScreen(
    sender: String,
    recipient: String,
    subject: String,
    body: String,
    onRecipientInput: (String) -> Unit,
    onBodyInput: (String) -> Unit,
    onSubjectInput: (String) -> Unit,
    onBackClicked: () -> Unit,
    onAttachmentClicked: () -> Unit,
    onSendClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.general_navigate_back),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onAttachmentClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.settings), // TODO Replace with attachment
                    contentDescription = stringResource(id = R.string.new_message_attach),
                )
            }
            LetroButton(
                modifier = Modifier.fillMaxWidth(0f),
                text = stringResource(id = R.string.new_message_send),
                onClick = onSendClicked,
                leadingIconResId = R.drawable.send,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.new_message_from),
                style = MaterialTheme.typography.bodyLarge,
                color = Grey90,
            )
            LetroTextField(
                value = sender,
                onValueChange = onRecipientInput,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.new_message_to),
                style = MaterialTheme.typography.bodyLarge,
                color = Grey90,
            )
            Text(
                text = recipient,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        LetroTextField(
            value = subject,
            onValueChange = onSubjectInput,
            placeHolderText = stringResource(id = R.string.new_message_subject_hint),
        )
        LetroTextField(
            value = body,
            onValueChange = onBodyInput,
            placeHolderText = stringResource(id = R.string.new_message_body_hint),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewMessageScreenPreview() {
    NewMessageScreen(
        sender = "sender",
        recipient = "recipient",
        subject = "subject",
        body = "body",
        onRecipientInput = {},
        onBodyInput = {},
        onSubjectInput = {},
        onBackClicked = {},
        onAttachmentClicked = {},
        onSendClicked = {},
    )
}
