package tech.relaycorp.letro.messages.viewing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.messages.model.ExtendedMessage
import tech.relaycorp.letro.ui.common.LetroButton
import tech.relaycorp.letro.utils.ext.applyIf

@Composable
fun ConversationScreen(
    onBackClicked: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val conversationState by viewModel.conversation.collectAsState()
    val conversation = conversationState

    if (conversation != null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 14.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBackClicked,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = stringResource(id = R.string.general_navigate_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = stringResource(id = R.string.content_description_delete_conversation),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                LetroButton(
                    text = stringResource(id = R.string.reply),
                    onClick = { /* TODO */ },
                    leadingIconResId = R.drawable.ic_reply,
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 24.dp,
                    ),
                )
                Spacer(
                    modifier = Modifier.width(16.dp),
                )
            }
            if (conversation.subject != null) {
                Text(
                    text = conversation.subject,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp,
                        ),
                )
            }
            LazyColumn {
                items(conversation.messages.size) { position ->
                    val message = conversation.messages[position]
                    val isLastMessage = position == conversation.messages.size - 1
                    Message(
                        message = message,
                        isCollapsable = conversation.messages.size > 1,
                        isLastMessage = isLastMessage,
                    )
                    if (!isLastMessage) {
                        Divider(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .fillMaxWidth()
                                .height(1.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(
    message: ExtendedMessage,
    isCollapsable: Boolean,
    isLastMessage: Boolean,
) {
    var isCollapsed: Boolean by remember { mutableStateOf(!isLastMessage) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .applyIf(isCollapsable && isCollapsed) {
                clickable { isCollapsed = !isCollapsed }
            }
            .padding(
                vertical = 10.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .applyIf(isCollapsable && !isCollapsed) {
                    clickable { isCollapsed = !isCollapsed }
                }
                .padding(
                    horizontal = 16.dp,
                ),
        ) {
            Text(
                text = if (message.isOutgoing) message.senderVeraId else message.contactDisplayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1F))
            Text(
                text = message.sentAtFormatted,
                style = if (isCollapsed) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (!isCollapsed) {
            Spacer(modifier = Modifier.height(26.dp))
        }
        Text(
            text = message.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isCollapsed) 1 else Int.MAX_VALUE,
            modifier = Modifier
                .padding(
                    vertical = if (isCollapsed) 0.dp else 10.dp,
                    horizontal = 16.dp,
                ),
        )
    }
}
