package tech.relaycorp.letro.conversation.list.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.conversation.list.ConversationUiModel

sealed interface ConversationsListContent {

    data class Conversations(
        val conversations: List<ConversationUiModel>,
    ) : ConversationsListContent

    data class Empty(
        @DrawableRes val image: Int,
        @StringRes val text: Int,
    ) : ConversationsListContent
}
