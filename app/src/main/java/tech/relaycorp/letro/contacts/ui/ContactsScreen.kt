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

    val editBottomSheet = editContactBottomSheetState
    val deleteContactDialog = deleteContactDialogState
    Box {
        if (editBottomSheet.isShown && editBottomSheet.contact != null) {
            LetroActionsBottomSheet(
                title = editBottomSheet.contact.alias ?: editBottomSheet.contact.contactVeraId,
                onDismissRequest = { viewModel.onEditBottomSheetDismissed() },
                actions = listOf(
                    BottomSheetAction(
                        icon = R.drawable.edit,
                        title = R.string.edit,
                        action = {
                            viewModel.onEditContactClick()
                            onEditContactClick(editBottomSheet.contact)
                        },
                    ),
                    BottomSheetAction(
                        icon = R.drawable.ic_delete,
                        title = R.string.delete,
                        action = {
                            viewModel.onDeleteContactClick(editBottomSheet.contact)
                        },
                    ),
                ),
            )
        } else if (deleteContactDialog.isShown && deleteContactDialog.contact != null) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.onDeleteContactDialogDismissed()
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.delete_contact_dialog_title),
                        style = MaterialTheme.typography.TitleMediumProminent,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                text = {
                    BoldText(
                        fullText = stringResource(id = R.string.delete_contact_dialog_message, deleteContactDialog.contact.contactVeraId),
                        boldParts = listOf(deleteContactDialog.contact.contactVeraId),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onConfirmDeletingContactClick(deleteContactDialog.contact)
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
                            viewModel.onDeleteContactDialogDismissed()
                        },
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
        LazyColumn(
            contentPadding = PaddingValues(
                top = 8.dp,
            ),
        ) {
            items(contacts.size) { index ->
                ContactView(
                    contact = contacts[index],
                    onActionsButtonClick = { viewModel.onActionsButtonClick(contacts[index]) },
                )
            }
        }
    }
}
