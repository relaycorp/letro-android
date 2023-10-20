package tech.relaycorp.letro.conversation.viewing.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.conversation.attachments.ui.Attachment
import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import tech.relaycorp.letro.conversation.model.ExtendedMessage
import tech.relaycorp.letro.conversation.viewing.ConversationViewModel
import tech.relaycorp.letro.ui.common.LetroButton
import tech.relaycorp.letro.ui.theme.Elevation2
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.utils.compose.toDp
import tech.relaycorp.letro.utils.ext.applyIf
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    conversationsStringsProvider: ConversationsStringsProvider,
    onReplyClick: () -> Unit,
    onConversationDeleted: () -> Unit,
    onConversationArchived: (Boolean) -> Unit,
    onBackClicked: () -> Unit,
    onAttachmentClick: (UUID) -> Unit,
    showAddContactSnackbar: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val scrollState = rememberLazyListState()

    val conversationState by viewModel.conversation.collectAsState()
    val conversation = conversationState

    val deleteConversationDialogState by viewModel.deleteConversationDialogState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.showNoContactsSnackbarSignal.collect {
            showAddContactSnackbar()
        }
    }

    if (conversation != null) {
        LaunchedEffect(Unit) { // Scroll to the top of a conversation on screen opening
            scrollState.scrollToItem(conversation.messages.size - 1)
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            if (deleteConversationDialogState.isShown) {
                DeleteConversationDialog(
                    onDismissRequest = { viewModel.onDeleteConversationBottomSheetDismissed() },
                    onConfirmClick = {
                        viewModel.onConfirmConversationDeletionClick()
                        onConversationDeleted()
                    },
                )
            }
            LazyColumn(
                state = scrollState,
            ) {
                stickyHeader {
                    Surface(
                        shadowElevation = if (scrollState.canScrollBackward) Elevation2 else 0.dp,
                    ) {
                        ConversationToolbar(
                            isArchived = conversation.isArchived,
                            onReplyClick = {
                                if (viewModel.canReply()) {
                                    onReplyClick()
                                }
                            },
                            onBackClicked = onBackClicked,
                            onArchiveClick = {
                                val isArchived = viewModel.onArchiveConversationClicked()
                                onConversationArchived(isArchived)
                            },
                            onDeleteClick = { viewModel.onDeleteConversationClick() },
                        )
                    }
                }
                item {
                    SelectionContainer {
                        Text(
                            text = conversation.subject ?: conversationsStringsProvider.noSubject,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp,
                                ),
                        )
                    }
                }
                items(conversation.messages.size) { position ->
                    val message = conversation.messages[position]
                    val isLastMessage = position == conversation.messages.size - 1
                    Message(
                        message = message,
                        isCollapsable = conversation.messages.size > 1,
                        isLastMessage = isLastMessage,
                        onAttachmentClick = { onAttachmentClick(it.fileId) },
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
    onAttachmentClick: (AttachmentInfo) -> Unit,
) {
    var isCollapsed: Boolean by remember { mutableStateOf(!isLastMessage) }
    var isDetailsCollapsed: Boolean by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .applyIf(isCollapsed) {
                height(CollapsedMessageHeight)
            }
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
            )
            Text(
                text = message.sentAtBriefFormatted,
                style = if (isCollapsed) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (!isCollapsed) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = if (isDetailsCollapsed) R.string.show_more else R.string.show_less),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 2.dp,
                    )
                    .clickable { isDetailsCollapsed = !isDetailsCollapsed },
            )
            if (!isDetailsCollapsed) {
                Spacer(modifier = Modifier.height(12.dp))
                MessageInfoView(
                    message = message,
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                        )
                        .fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
        }
        SelectionContainer {
            Text(
                text = message.text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (isCollapsed) 1 else Int.MAX_VALUE,
                modifier = Modifier
                    .padding(
                        vertical = if (isCollapsed) 2.dp else 10.dp,
                        horizontal = 16.dp,
                    ),
            )
        }
        if (!isCollapsed) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                message.attachments.forEachIndexed { index, attachment ->
                    if (index != 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Attachment(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(fraction = 0.73F),
                        attachment = attachment,
                        onAttachmentClick = { onAttachmentClick(attachment) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageInfoView(
    modifier: Modifier = Modifier,
    message: ExtendedMessage,
) {
    Box(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                .padding(8.dp),
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.from),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (message.senderDisplayName != message.senderVeraId) {
                    Spacer(
                        modifier = Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.toDp().minus(2.dp)),
                    )
                }
                Text(
                    text = stringResource(id = R.string.to),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (message.recipientDisplayName != message.recipientVeraId) {
                    Spacer(
                        modifier = Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.toDp().minus(2.dp)),
                    )
                }
                Text(
                    text = stringResource(id = R.string.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = message.senderDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (message.senderDisplayName != message.senderVeraId) {
                    Text(
                        text = message.senderVeraId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.recipientDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (message.recipientDisplayName != message.recipientVeraId) {
                    Text(
                        text = message.recipientVeraId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.sentAtDetailedFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ConversationToolbar(
    isArchived: Boolean,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onBackClicked: () -> Unit,
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
            onClick = { onArchiveClick() },
        ) {
            Icon(
                painter = painterResource(id = if (isArchived) R.drawable.ic_unarchive_24 else R.drawable.ic_archive_24),
                contentDescription = stringResource(id = R.string.archive),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = { onDeleteClick() },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_trash),
                contentDescription = stringResource(id = R.string.delete_conversation),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        LetroButton(
            text = stringResource(id = R.string.reply),
            onClick = onReplyClick,
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
}

@Composable
private fun DeleteConversationDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(
                text = stringResource(id = R.string.delete_conversation),
                style = MaterialTheme.typography.TitleMediumProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.delete_conversation_dialog_message),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClick()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.delete),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Preview
@Composable
private fun MessageInfo_Preview() {
    Column {
        MessageInfoView(
            message = ExtendedMessage(
                conversationId = UUID.randomUUID(),
                senderVeraId = "sender@vera.id",
                recipientVeraId = "recipient@vera.id",
                senderDisplayName = "Sender",
                recipientDisplayName = "Recipient",
                isOutgoing = true,
                contactDisplayName = "Alias of contact",
                text = "Hi!",
                sentAtBriefFormatted = "15 Aug",
                sentAtDetailedFormatted = "15 Aug 2023, 10:06am",
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        MessageInfoView(
            message = ExtendedMessage(
                conversationId = UUID.randomUUID(),
                senderVeraId = "sender@vera.id",
                recipientVeraId = "recipient@vera.id",
                senderDisplayName = "Sender",
                recipientDisplayName = "Recipient",
                isOutgoing = false,
                contactDisplayName = "sender@vera.id",
                text = "Hi!",
                sentAtBriefFormatted = "15 Aug",
                sentAtDetailedFormatted = "15 Aug 2023, 10:06am",
            ),
        )
    }
}

private val CollapsedMessageHeight = 64.dp
