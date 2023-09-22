@file:Suppress("NAME_SHADOWING")

package tech.relaycorp.letro.messages.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.messages.list.section.ConversationSectionInfo
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.model.ExtendedMessage
import tech.relaycorp.letro.ui.common.BottomSheetAction
import tech.relaycorp.letro.ui.common.LetroActionsBottomSheet
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
    val sectionSelectorState by viewModel.conversationSectionState.collectAsState()

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
        Column {
            if (isOnboardingVisible) {
                ConversationsOnboardingView(
                    onCloseClick = { viewModel.onCloseOnboardingButtonClick() },
                )
            }
            Box {
                LazyColumn {
                    items(1) {
                        ConversationsSectionSelector(
                            text = stringResource(id = sectionSelectorState.currentSection.title),
                            icon = painterResource(id = sectionSelectorState.currentSection.icon),
                            onClick = {
                                viewModel.onConversationSectionSelectorClick()
                            },
                        )
                    }

                    when (val conversations = conversations) {
                        is ConversationsListContent.Conversations -> {
                            items(conversations.conversations) { conversation ->
                                Conversation(
                                    conversation = conversation,
                                    noSubjectText = conversationsStringsProvider.noSubject,
                                    onConversationClick = {
                                        onConversationClick(conversation)
                                    },
                                )
                            }
                        }
                        is ConversationsListContent.Empty -> {
                            items(1) {
                                Column {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    EmptyConversationsView(
                                        image = conversations.image,
                                        text = conversations.text,
                                    )
                                }
                            }
                        }
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
                    horizontal = 16.dp,
                    vertical = 16.dp,
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
private fun EmptyConversationsView(
    @DrawableRes image: Int,
    @StringRes text: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter = painterResource(id = image), contentDescription = null)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = text),
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

@Preview(showBackground = true)
@Composable
private fun SectionSelector_Preview() {
    ConversationsSectionSelector(text = "inbox", icon = painterResource(id = R.drawable.inbox)) {
    }
}
