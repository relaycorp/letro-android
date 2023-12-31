@file:Suppress("NAME_SHADOWING")
@file:OptIn(ExperimentalFoundationApi::class)

package tech.relaycorp.letro.conversation.list.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.conversation.list.ConversationUiModel
import tech.relaycorp.letro.conversation.list.ConversationsListViewModel
import tech.relaycorp.letro.conversation.list.section.ConversationSectionInfo
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.model.ExtendedMessage
import tech.relaycorp.letro.ui.common.LetroAvatar
import tech.relaycorp.letro.ui.common.LetroEmptyListStub
import tech.relaycorp.letro.ui.common.bottomsheet.BottomSheetAction
import tech.relaycorp.letro.ui.common.bottomsheet.LetroActionsBottomSheet
import tech.relaycorp.letro.ui.theme.BodyLargeProminent
import tech.relaycorp.letro.ui.theme.BodyMediumProminent
import tech.relaycorp.letro.ui.theme.LabelSmallProminent
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import tech.relaycorp.letro.utils.time.nowUTC
import java.util.UUID

@Composable
fun ConversationsListScreen(
    conversationsStringsProvider: ConversationsStringsProvider,
    viewModel: ConversationsListViewModel,
    openConversation: (ExtendedConversation) -> Unit,
    showSnackbar: (SnackbarString) -> Unit,
) {
    val conversations by viewModel.conversations.collectAsState()
    val isOnboardingVisible by viewModel.isOnboardingMessageVisible.collectAsState()
    val sectionSelectorState by viewModel.conversationSectionState.collectAsState()
    val isSectionSelectorVisible by viewModel.isSectionSelectorVisible.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.openConversation.collect {
            openConversation(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (sectionSelectorState.sectionSelector.isOpened) {
            LetroActionsBottomSheet(
                actions = sectionSelectorState.sectionSelector.sections
                    .map {
                        BottomSheetAction(
                            icon = it.icon,
                            title = it.title,
                            action = { viewModel.onSectionChosen(it) },
                            isChosen = it == sectionSelectorState.currentSection,
                            trailingText = if (it is ConversationSectionInfo.Inbox) it.unreadMessages.toString() else null,
                        )
                    },
                onDismissRequest = { viewModel.onConversationSectionDialogDismissed() },
            )
        }
        val conversations = conversations
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (conversations is ConversationsListContent.Empty) {
                LetroEmptyListStub(
                    image = conversations.image,
                    text = conversations.text,
                )
            }
            Column {
                AnimatedVisibility(visible = isOnboardingVisible) {
                    ConversationsOnboardingView(
                        onCloseClick = { viewModel.onCloseOnboardingButtonClick() },
                    )
                }
                LazyColumn {
                    if (isSectionSelectorVisible) {
                        item {
                            ConversationsSectionSelector(
                                text = stringResource(id = sectionSelectorState.currentSection.title),
                                icon = painterResource(id = sectionSelectorState.currentSection.icon),
                                onClick = {
                                    viewModel.onConversationSectionSelectorClick()
                                },
                            )
                        }
                    }

                    when (conversations) {
                        is ConversationsListContent.Conversations -> {
                            items(conversations.conversations) { conversation ->
                                Conversation(
                                    conversation = conversation,
                                    noSubjectText = conversationsStringsProvider.noSubject,
                                    onClick = {
                                        viewModel.onConversationClick(conversation)
                                    },
                                    onLongClick = {
                                        viewModel.onConversationLongClick(conversation)
                                    },
                                )
                            }
                        }
                        is ConversationsListContent.Empty -> {
                            // No more elements in lazy list
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Conversation(
    conversation: ConversationUiModel,
    noSubjectText: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isSelected = conversation.isSelected
    val conversation = conversation.conversation
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .background(if (!isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() },
            )
            .padding(
                horizontal = 16.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val avatarModifier = Modifier
                .clip(CircleShape)
                .size(40.dp)
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .then(avatarModifier),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_done_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                LetroAvatar(
                    modifier = avatarModifier,
                    filePath = conversation.contactAvatarPath,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .weight(1f),
                    ) {
                        Text(
                            text = conversation.contactDisplayName,
                            style = if (!conversation.isRead) MaterialTheme.typography.BodyLargeProminent else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (conversation.totalMessagesFormattedText != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = conversation.totalMessagesFormattedText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(
                                        top = 3.dp,
                                    ),
                            )
                        }
                    }
                    Text(
                        text = conversation.lastMessageFormattedTimestamp,
                        style = if (!conversation.isRead) MaterialTheme.typography.LabelSmallProminent else MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row {
                    Text(
                        text = conversation.subject ?: noSubjectText,
                        style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = " - ",
                        style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
                        color = if (!conversation.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val messageDisplayedText = if (conversation.messages.last().text.isNotEmptyOrBlank()) conversation.messages.last().text else conversation.messages.last().attachments.firstOrNull()?.name ?: ""
                    Text(
                        text = messageDisplayedText.replace("[\\r\\n]+".toRegex(), " "),
                        style = if (!conversation.isRead) MaterialTheme.typography.BodyMediumProminent else MaterialTheme.typography.bodyMedium,
                        color = if (!conversation.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationsSectionSelector(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 12.dp,
                )
                .clickable { onClick() },
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.BodyLargeProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
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
        senderAvatarPath = null,
        recipientDisplayName = "Recipient",
        isOutgoing = true,
        contactDisplayName = "Alias",
        text = "Hello man!",
        sentAtBriefFormatted = "15 Aug",
        sentAtDetailedFormatted = "15 Aug 2023, 10:06am",
        sentAtUtc = nowUTC(),
    )
    Column {
        Conversation(
            conversation = ConversationUiModel(
                ExtendedConversation(
                    conversationId = conversationId,
                    ownerVeraId = "ft@applepie.rocks",
                    contactVeraId = "contact@vera.id",
                    contactDisplayName = "Alias",
                    subject = "Subject Preview",
                    lastMessageSentAtUtc = message.sentAtUtc,
                    isRead = false,
                    lastMessageFormattedTimestamp = "01:03 PM",
                    lastMessage = message,
                    totalMessagesFormattedText = "(2)",
                    messages = listOf(
                        message,
                    ),
                    isArchived = false,
                    contactAvatarPath = null,
                ),
            ),
            noSubjectText = "(No subject)",
            onClick = {},
        ) {
        }
        Divider(
            modifier = Modifier.height(1.dp),
        )
        Conversation(
            conversation = ConversationUiModel(
                ExtendedConversation(
                    conversationId = conversationId,
                    ownerVeraId = "ft@applepie.rocks",
                    contactVeraId = "contact@vera.id",
                    contactDisplayName = "Alias",
                    subject = "Subject Preview",
                    lastMessageSentAtUtc = message.sentAtUtc,
                    isRead = true,
                    lastMessageFormattedTimestamp = "01:03 PM",
                    lastMessage = message,
                    totalMessagesFormattedText = "(2)",
                    messages = listOf(
                        message.copy(
                            text = "This is example of a very long text. So long, that it doesn't fit...",
                        ),
                    ),
                    isArchived = false,
                    contactAvatarPath = null,
                ),
            ),
            noSubjectText = "(No subject)",
            onClick = {},
        ) {
        }
        Divider(
            modifier = Modifier.height(1.dp),
        )
        Conversation(
            conversation = ConversationUiModel(
                ExtendedConversation(
                    conversationId = conversationId,
                    ownerVeraId = "ft@applepie.rocks",
                    contactVeraId = "contact@vera.id",
                    contactDisplayName = "Alias",
                    subject = "Very long subject. So long, that it doesn't fit on the screen at all",
                    lastMessageSentAtUtc = message.sentAtUtc,
                    isRead = true,
                    lastMessageFormattedTimestamp = "01:03 PM",
                    lastMessage = message,
                    totalMessagesFormattedText = "(2)",
                    messages = listOf(
                        message.copy(
                            text = "This is example of a very long text. So long, that it doesn't fit...",
                        ),
                    ),
                    isArchived = false,
                    contactAvatarPath = null,
                ),
            ),
            noSubjectText = "(No subject)",
            onClick = {},
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

@Preview(showBackground = true)
@Composable
private fun SectionSelector_Preview() {
    ConversationsSectionSelector(text = "inbox", icon = painterResource(id = R.drawable.inbox)) {
    }
}
