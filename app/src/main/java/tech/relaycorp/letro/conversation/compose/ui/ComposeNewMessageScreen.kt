package tech.relaycorp.letro.conversation.compose.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.ui.ContactView
import tech.relaycorp.letro.conversation.attachments.ui.Attachment
import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel
import tech.relaycorp.letro.ui.common.LetroAvatar
import tech.relaycorp.letro.ui.common.LetroButton
import tech.relaycorp.letro.ui.common.LetroTextField
import tech.relaycorp.letro.ui.theme.Elevation2
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.utils.ext.applyIf

@Composable
fun ComposeNewMessageScreen(
    conversationsStringsProvider: ConversationsStringsProvider,
    goBack: () -> Unit,
    onMessageSent: () -> Unit,
    showSnackbar: (SnackbarString) -> Unit,
    viewModel: ComposeNewMessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageExceedsLimitError = uiState.messageExceedsLimitTextError
    val suggestedContacts = uiState.suggestedContacts
    val attachments by viewModel.attachments.collectAsState()

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            viewModel.onFilePickerResult(it?.toString())
        },
    )

    var recipientTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.recipientAccountId,
            ),
        )
    }

    var subjectTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.subject,
            ),
        )
    }

    var messageTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.messageText,
            ),
        )
    }

    val scrollState = rememberLazyListState()

    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.goBackSignal.collect {
            goBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messageSentSignal.collect {
            onMessageSent()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isConfirmDiscardingDialogVisible) {
            ConfirmGoBackDialog(
                onConfirm = { viewModel.onConfirmDiscardingClick() },
                onDismiss = { viewModel.onConfirmDiscardingDialogDismissed() },
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val shadowElevation by animateDpAsState(targetValue = if (scrollState.canScrollBackward) Elevation2 else 0.dp)
            Surface(
                shadowElevation = shadowElevation,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            PaddingValues(
                                end = HorizontalScreenPadding,
                                top = 14.dp,
                                bottom = 14.dp,
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { viewModel.onBackPressed() },
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
                        onClick = { documentPickerLauncher.launch("*/*") },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.attachment),
                            contentDescription = stringResource(id = R.string.new_message_attach),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    LetroButton(
                        text = stringResource(id = R.string.new_message_send),
                        onClick = { viewModel.onSendMessageClick() },
                        leadingIconResId = R.drawable.ic_send,
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 24.dp,
                        ),
                        enabled = uiState.isSendButtonEnabled,
                        progressIndicatorModifier = Modifier
                            .size(18.dp, 18.dp),
                        isProgressIndicatorVisible = uiState.isSendingMessage,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                state = scrollState,
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.from),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                .applyIf(uiState.isOnlyTextEditale) {
                                    copy(alpha = 0.38f)
                                },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = uiState.sender,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.38F),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.to),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                .applyIf(uiState.isOnlyTextEditale) {
                                    copy(alpha = 0.38f)
                                },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (uiState.showRecipientAsChip) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                RecipientChipView(
                                    text = uiState.recipientDisplayedText,
                                    avatarPath = uiState.recipientAvatarPath,
                                    onRemoveClick = {
                                        viewModel.onRecipientRemoveClick()
                                        recipientTextFieldValueState = TextFieldValue()
                                    },
                                    isEditable = !uiState.isOnlyTextEditale,
                                )
                                Spacer(modifier = Modifier.height(TextFieldDefaults.MinHeight))
                            }
                        } else {
                            LetroTextField(
                                value = recipientTextFieldValueState,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                onValueChange = {
                                    if (uiState.showRecipientAsChip) {
                                        return@LetroTextField
                                    }
                                    recipientTextFieldValueState = it
                                    viewModel.onRecipientTextChanged(it.text)
                                },
                                modifier = Modifier
                                    .onFocusChanged {
                                        viewModel.onRecipientTextFieldFocused(it.isFocused)
                                    },
                            )
                        }
                    }
                    Divider()
                }

                when {
                    !suggestedContacts.isNullOrEmpty() -> {
                        suggestContactsList(
                            lazyListScope = this@LazyColumn,
                            contacts = suggestedContacts,
                            onContactClick = {
                                recipientTextFieldValueState = TextFieldValue(
                                    it.contactVeraId,
                                    TextRange(it.contactVeraId.length),
                                )
                                viewModel.onSuggestClick(it)
                            },
                        )
                    }

                    uiState.showRecipientIsNotYourContactError -> {
                        items(1) {
                            Text(
                                text = stringResource(id = R.string.you_not_connected_to_this_contact_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                            )
                        }
                    }

                    else -> {
                        item {
                            Column {
                                if (!uiState.isOnlyTextEditale) {
                                    LetroTextField(
                                        value = subjectTextFieldValueState,
                                        onValueChange = {
                                            subjectTextFieldValueState = it
                                            viewModel.onSubjectTextChanged(it.text)
                                        },
                                        placeHolderText = stringResource(id = R.string.new_message_subject_hint),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Next,
                                            capitalization = KeyboardCapitalization.Sentences,
                                        ),
                                        placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .onFocusChanged { viewModel.onSubjectTextFieldFocused(it.isFocused) },
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(TextFieldDefaults.MinHeight)
                                            .padding(horizontal = 16.dp),
                                    ) {
                                        Text(
                                            text = if (uiState.showNoSubjectText) conversationsStringsProvider.noSubject else uiState.subject,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                                .copy(alpha = 0.38F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart),
                                        )
                                    }
                                }
                                Divider()
                                LetroTextField(
                                    value = messageTextFieldValueState,
                                    onValueChange = {
                                        messageTextFieldValueState = it
                                        viewModel.onMessageTextChanged(it.text)
                                    },
                                    placeHolderText = stringResource(id = R.string.new_message_body_hint),
                                    singleLine = false,
                                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        capitalization = KeyboardCapitalization.Sentences,
                                    ),
                                    modifier = Modifier
                                        .then(Modifier)
                                        .applyIf(attachments.isEmpty()) {
                                            defaultMinSize(
                                                minHeight = 250.dp,
                                            )
                                        }
                                        .onFocusChanged { viewModel.onMessageTextFieldFocused(it.isFocused) },
                                )
                            }
                        }
                        if (attachments.isNotEmpty()) {
                            attachments(
                                lazyListScope = this@LazyColumn,
                                attachments = attachments,
                                onAttachmentDeleteClick = { viewModel.onAttachmentDeleteClick(it) },
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = messageExceedsLimitError != null && suggestedContacts.isNullOrEmpty(),
                enter = slideInVertically(initialOffsetY = { it / 2 }),
                exit = slideOutVertically(targetOffsetY = { it / 2 }),
            ) {
                if (messageExceedsLimitError != null) {
                    Text(
                        text = stringResource(
                            id = messageExceedsLimitError.stringRes,
                            messageExceedsLimitError.value,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(vertical = 18.dp, horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

private fun suggestContactsList(
    lazyListScope: LazyListScope,
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
) {
    with(lazyListScope) {
        items(
            count = contacts.size,
            key = {
                contacts[it].id
            },
        ) {
            ContactView(
                contact = contacts[it],
                onClick = { onContactClick(contacts[it]) },
            )
        }
    }
}

@Composable
private fun RecipientChipView(
    text: String,
    avatarPath: String?,
    onRemoveClick: () -> Unit,
    isEditable: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(80.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(32.dp)
            .background(
                color = if (isEditable) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.06F,
                ),
                shape = shape,
            )
            .applyIf(isEditable) {
                border(1.dp, LetroColor.SurfaceContainer, shape)
            }
            .padding(
                start = 2.dp,
                end = 12.dp,
                top = 2.dp,
                bottom = 2.dp,
            ),
    ) {
        LetroAvatar(
            modifier = Modifier
                .clip(CircleShape)
                .size(28.dp),
            filePath = avatarPath,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
                .applyIf(!isEditable) {
                    copy(alpha = 0.3F)
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false),
        )
        if (isEditable) {
            Spacer(modifier = Modifier.width(11.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_cancel_16),
                contentDescription = stringResource(id = R.string.content_description_recipient_clear),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable { onRemoveClick() },
            )
        }
    }
}

private fun attachments(
    lazyListScope: LazyListScope,
    attachments: List<AttachmentInfo>,
    onAttachmentDeleteClick: (AttachmentInfo) -> Unit,
) {
    with(lazyListScope) {
        items(
            count = attachments.size,
            key = { attachments[it].fileId },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                if (it != 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Attachment(
                    attachment = attachments[it],
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(fraction = 0.87F),
                    onDeleteClick = { onAttachmentDeleteClick(attachments[it]) },
                )
                if (it == attachments.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ConfirmGoBackDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.discard_message_dialog_title),
                style = MaterialTheme.typography.TitleMediumProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.discard_message_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(id = R.string.discard_message),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.LabelLargeProminent,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        containerColor = LetroColor.SurfaceContainerLow,
    )
}
