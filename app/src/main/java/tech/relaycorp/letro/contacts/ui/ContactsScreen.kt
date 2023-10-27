package tech.relaycorp.letro.contacts.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.ContactActionsBottomSheet
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.ui.common.bottomsheet.LetroActionsBottomSheet
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.LabelLargeProminent
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.TitleMediumProminent
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.compose.showSnackbar

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    snackbarHostState: SnackbarHostState,
    snackbarStringsProvider: SnackbarStringsProvider,
    onEditContactClick: (Contact) -> Unit,
    onPairWithOthersClick: () -> Unit,
    onShareIdClick: () -> Unit,
    onStartConversationClick: (Contact) -> Unit,
) {
    val contactsState by viewModel.contacts.collectAsState()
    val contacts = contactsState
    val contactActionsBottomSheetState by viewModel.contactActionsBottomSheetState.collectAsState()
    val deleteContactDialogState by viewModel.deleteContactDialogState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.showContactDeletedSnackbarSignal.collect {
            snackbarHostState.showSnackbar(this, snackbarStringsProvider.contactDeleted)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showSnackbar.collect {
            snackbarHostState.showSnackbar(this, snackbarStringsProvider.get(it))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openConversationSignal.collect {
            onStartConversationClick(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.editContactSignal.collect {
            onEditContactClick(it)
        }
    }

    val actionsBottomSheet = contactActionsBottomSheetState
    val deleteContactDialog = deleteContactDialogState
    when (contacts) {
        is ContactsListContent.Contacts -> {
            Box {
                when {
                    actionsBottomSheet.isShown && actionsBottomSheet.data != null -> {
                        ContactActionsDialog(
                            data = actionsBottomSheet.data,
                            onDismissed = { viewModel.onActionsBottomSheetDismissed() },
                        )
                    }
                    deleteContactDialog.isShown && deleteContactDialog.contact != null -> {
                        DeleteContactDialog(
                            contact = deleteContactDialog.contact,
                            onDismissed = { viewModel.onDeleteContactDialogDismissed() },
                            onConfirm = { viewModel.onConfirmDeletingContactClick(deleteContactDialog.contact) },
                        )
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 8.dp,
                    ),
                ) {
                    items(contacts.contacts.size) { index ->
                        ContactView(
                            contact = contacts.contacts[index],
                            onClick = { viewModel.onActionsButtonClick(contacts.contacts[index]) },
                        )
                    }
                }
            }
        }
        is ContactsListContent.Empty -> {
            NoContactsScreen(
                onPairWithOthersClick = onPairWithOthersClick,
                onShareIdClick = onShareIdClick,
            )
        }
    }
}

@Composable
private fun ContactActionsDialog(
    data: ContactActionsBottomSheet,
    onDismissed: () -> Unit,
) {
    LetroActionsBottomSheet(
        title = data.title,
        actions = data.actions,
        onDismissRequest = onDismissed,
    )
}

@Composable
private fun DeleteContactDialog(
    contact: Contact,
    onDismissed: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(
                text = stringResource(id = R.string.delete_contact_dialog_title),
                style = MaterialTheme.typography.TitleMediumProminent,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            BoldText(
                fullText = stringResource(id = R.string.delete_contact_dialog_message, contact.contactVeraId),
                boldParts = listOf(contact.contactVeraId),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
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
                onClick = onDismissed,
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
