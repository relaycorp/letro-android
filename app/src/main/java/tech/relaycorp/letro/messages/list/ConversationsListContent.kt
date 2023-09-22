package tech.relaycorp.letro.messages.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tech.relaycorp.letro.messages.model.ExtendedConversation

sealed interface ConversationsListContent {

    data class Conversations(
        val conversations: List<ExtendedConversation>,
    ) : ConversationsListContent

    data class Empty(
        @DrawableRes val image: Int,
        @StringRes val text: Int,
    ) : ConversationsListContent
}
