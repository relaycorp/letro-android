package tech.relaycorp.letro.contacts.ui

import androidx.compose.runtime.Composable
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreen
import tech.relaycorp.letro.ui.actionTaking.ActionTakingScreenUIStateModel

@Composable
fun NoContactsScreen(
    onPairWithOthersClick: () -> Unit,
    onShareIdClick: () -> Unit,
) {
    ActionTakingScreen(
        actionTakingScreenUIStateModel = ActionTakingScreenUIStateModel.NoContacts(
            title = null,
            message = R.string.no_contacts_text,
            image = R.drawable.no_contacts_image,
            onPairWithOthersClick = onPairWithOthersClick,
            onShareIdClick = onShareIdClick,
        ),
    )
}
