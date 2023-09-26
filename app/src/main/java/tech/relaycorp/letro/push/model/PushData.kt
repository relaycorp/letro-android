package tech.relaycorp.letro.push.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PushData(
    val title: String,
    val text: String,
    val action: PushAction,
    val notificationId: Int,
    val recipientAccountId: String,
    @PushChannel.ChannelId val channelId: String = PushChannel.ChannelId.ID_DEFAULT,
)

sealed interface PushAction : Parcelable {

    @Parcelize
    data class OpenConversation(
        val conversationId: String,
    ) : PushAction
}