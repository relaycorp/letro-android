package tech.relaycorp.letro.contacts.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.ui.common.text.BoldText
import tech.relaycorp.letro.ui.theme.LargeProminent
import tech.relaycorp.letro.ui.theme.LetroColor
import tech.relaycorp.letro.ui.theme.SmallProminent
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider

@OptIn(ExperimentalMaterial3Api::class)
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
            snackbarHostState.showSnackbar(
                message = snackbarStringsProvider.contactDeleted,
            )
        }
    }

    val editBottomSheet = editContactBottomSheetState
    val deleteContactDialog = deleteContactDialogState
    Box {
        if (editBottomSheet.isShown && editBottomSheet.contact != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEditBottomSheetDismissed() },
            ) {
                EditContactBottomSheet(
                    title = editBottomSheet.contact.alias ?: editBottomSheet.contact.contactVeraId,
                    onEditClick = {
                        viewModel.onEditContactClick()
                        onEditContactClick(editBottomSheet.contact)
                    },
                    onDeleteClick = {
                        viewModel.onDeleteContactClick(editBottomSheet.contact)
                    },
                )
            }
        } else if (deleteContactDialog.isShown && deleteContactDialog.contact != null) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.onDeleteContactDialogDismissed()
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.delete_contact_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
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
                            style = MaterialTheme.typography.LargeProminent,
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
                            style = MaterialTheme.typography.LargeProminent,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
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

@Composable
fun ContactView(
    contact: Contact,
    onActionsButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(
                vertical = if (contact.alias == null) 16.dp else 10.dp,
                horizontal = 16.dp,
            )
            .fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            if (contact.alias != null) {
                Text(
                    text = contact.alias,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = contact.contactVeraId,
                color = MaterialTheme.colorScheme.onSurface,
                style = if (contact.alias == null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_more),
            contentDescription = stringResource(id = R.string.icon_more_content_description),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onActionsButtonClick() },
        )
    }
}

@Composable
private fun EditContactBottomSheet(
    title: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            PaddingValues(
                bottom = 44.dp,
            ),
        ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.SmallProminent,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                ),
        )
        Spacer(
            modifier = Modifier.height(14.dp),
        )
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        EditContactAction(
            icon = R.drawable.edit,
            title = R.string.edit,
            onClick = onEditClick,
        )
        EditContactAction(
            icon = R.drawable.ic_delete,
            title = R.string.delete,
            onClick = onDeleteClick,
        )
    }
}

@Composable
private fun EditContactAction(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                vertical = 14.dp,
                horizontal = 16.dp,
            ),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = LetroColor.OnSurfaceContainer,
        )
        Spacer(
            modifier = Modifier.width(16.dp),
        )
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
