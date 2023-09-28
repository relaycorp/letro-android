package tech.relaycorp.letro.messages.list.section

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.R

sealed class ConversationSectionInfo(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    data class Inbox(
        val unreadMessages: Int,
    ) : ConversationSectionInfo(
        title = R.string.inbox,
        icon = R.drawable.inbox,
    )

    object Sent : ConversationSectionInfo(
        title = R.string.sent,
        icon = R.drawable.sent,
    )

    object Archived : ConversationSectionInfo(
        title = R.string.archive,
        icon = R.drawable.ic_archive_24,
    )

    companion object {
        fun allSections(
            unreadMessages: Int,
        ) = listOf(
            Inbox(unreadMessages),
            Sent,
            Archived,
        )
    }
}
