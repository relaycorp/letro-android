package tech.relaycorp.letro.push.model

import tech.relaycorp.letro.ui.navigation.Action

data class PushData(
    val title: String,
    val text: String,
    val action: Action,
    val notificationId: Int,
    val recipientAccountId: String,
    val largeIcon: LargeIcon? = null,
    @PushChannel.ChannelId val channelId: String = PushChannel.ChannelId.ID_CONVERSATIONS,
)

sealed class LargeIcon {

    class File(
        val path: String,
    ) : LargeIcon()

    class DefaultAvatar : LargeIcon()
}
