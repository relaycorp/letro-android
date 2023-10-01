package tech.relaycorp.letro.conversation.list.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.conversation.model.ExtendedConversation

sealed interface ConversationsListContent {

    data class Conversations(
        val conversations: List<ExtendedConversation>,
    ) : ConversationsListContent

    data class Empty(
        @DrawableRes val image: Int,
        @StringRes val text: Int,
    ) : ConversationsListContent
}
