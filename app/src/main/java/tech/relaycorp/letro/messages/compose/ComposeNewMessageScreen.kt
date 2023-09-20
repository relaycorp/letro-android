package tech.relaycorp.letro.messages.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.ui.ContactView
import tech.relaycorp.letro.ui.common.LetroButton
import tech.relaycorp.letro.ui.common.LetroTextField
import tech.relaycorp.letro.ui.theme.HorizontalScreenPadding
import tech.relaycorp.letro.ui.theme.LetroColor

@Composable
fun CreateNewMessageScreen(
    onBackClicked: () -> Unit,
    onMessageSent: () -> Unit,
    viewModel: CreateNewMessageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var recipientTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(),
        )
    }

    var subjectTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(),
        )
    }

    var messageTextFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(),
        )
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.messageSentSignal.collect {
            onMessageSent()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        end = HorizontalScreenPadding,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
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
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.new_message_from),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = uiState.sender,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
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
                text = stringResource(id = R.string.new_message_to),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.showRecipientAsChip) {
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    contentAlignment = Alignment.CenterStart,
                ) {
                    RecipientChipView(
                        text = uiState.recipient,
                        onRemoveClick = {
                            viewModel.onRecipientRemoveClick()
                            recipientTextFieldValueState = TextFieldValue()
                        },
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
                )
            }
        }
        Divider()
        Box {
            val suggestedContacts = uiState.suggestedContacts
            when {
                !suggestedContacts.isNullOrEmpty() -> {
                    SuggestContactsList(
                        contacts = suggestedContacts,
                        onContactClick = {
                            recipientTextFieldValueState = TextFieldValue(it.contactVeraId, TextRange(it.contactVeraId.length))
                            viewModel.onSuggestClick(it)
                        },
                    )
                }
                uiState.showRecipientIsNotYourContactError -> {
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
                else -> {
                    Column {
                        LetroTextField(
                            value = subjectTextFieldValueState,
                            onValueChange = {
                                subjectTextFieldValueState = it
                                viewModel.onSubjectTextChanged(it.text)
                            },
                            placeHolderText = stringResource(id = R.string.new_message_subject_hint),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                            ),
                            placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .onFocusChanged { viewModel.onSubjectTextFieldFocused(it.isFocused) },
                        )
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
                            modifier = Modifier
                                .fillMaxSize()
                                .onFocusChanged { viewModel.onMessageTextFieldFocused(it.isFocused) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestContactsList(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        items(
            count = contacts.size,
            key = {
                contacts[it].id
            },
        ) {
            ContactView(
                contact = contacts[it],
                onContactClick = { onContactClick(contacts[it]) },
            )
        }
    }
}

@Composable
private fun RecipientChipView(
    text: String,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(80.dp),
            )
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_chip_delete),
            contentDescription = stringResource(id = R.string.content_description_recipient_clear),
            tint = LetroColor.OnSurfaceContainer,
            modifier = Modifier
                .clickable { onRemoveClick() },
        )
    }
}
