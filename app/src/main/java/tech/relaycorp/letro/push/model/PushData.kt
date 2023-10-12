package tech.relaycorp.letro.push.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PushData(
    val title: String,
    val text: String,
    val action: PushAction,
    val notificationId: Int,
    val recipientAccountId: String,
    @PushChannel.ChannelId val channelId: String = PushChannel.ChannelId.ID_CONVERSATIONS,
)

sealed interface PushAction : Parcelable {

    val accountId: String

    @Parcelize
    data class OpenConversation(
        val conversationId: String,
        override val accountId: String,
    ) : PushAction

    @Parcelize
    data class OpenMainPage(
        override val accountId: String,
    ) : PushAction

    @Parcelize
    data class OpenContacts(
        override val accountId: String,
    ) : PushAction
}
