package tech.relaycorp.letro.push.model

import tech.relaycorp.letro.ui.navigation.Action

data class PushData(
    val title: String,
    val text: String,
    val action: Action,
    val notificationId: Int,
    val recipientAccountId: String,
    @PushChannel.ChannelId val channelId: String = PushChannel.ChannelId.ID_CONVERSATIONS,
)
