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
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.ui.common.bottomsheet.BottomSheetAction
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
) {
    val contacts by viewModel.contacts.collectAsState()
    val editContactBottomSheetState by viewModel.editContactBottomSheetState.collectAsState()
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

    val editBottomSheet = editContactBottomSheetState
    val deleteContactDialog = deleteContactDialogState
    Box {
        when {
            editBottomSheet.isShown && editBottomSheet.contact != null -> {
                EditContactDialog(
                    contact = editBottomSheet.contact,
                    onDismissed = { viewModel.onEditBottomSheetDismissed() },
                    onEditClick = {
                        viewModel.onEditContactClick()
                        onEditContactClick(editBottomSheet.contact)
                    },
                    onDeleteClick = {
                        viewModel.onDeleteContactClick(editBottomSheet.contact)
                    },
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
            items(contacts.size) { index ->
                ContactView(
                    contact = contacts[index],
                    onClick = { viewModel.onActionsButtonClick(contacts[index]) },
                )
            }
        }
    }
}

@Composable
private fun EditContactDialog(
    contact: Contact,
    onDismissed: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    LetroActionsBottomSheet(
        title = contact.alias ?: contact.contactVeraId,
        onDismissRequest = onDismissed,
        actions = listOf(
            BottomSheetAction(
                icon = R.drawable.edit,
                title = R.string.edit,
                action = onEditClick,
            ),
            BottomSheetAction(
                icon = R.drawable.ic_delete,
                title = R.string.delete,
                action = onDeleteClick,
            ),
        ),
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
