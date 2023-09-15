package tech.relaycorp.letro.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class HomeFloatingActionButtonConfig(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
) {

    object ChatListFloatingActionButtonConfig : HomeFloatingActionButtonConfig(
        icon = R.drawable.pencil,
        contentDescription = R.string.floating_action_button_write_new_message_content_description,
    )

    object ContactsFloatingActionButtonConfig : HomeFloatingActionButtonConfig(
        icon = R.drawable.ic_plus,
        contentDescription = R.string.floating_action_button_add_contact_content_description,
    )
}
