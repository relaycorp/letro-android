package tech.relaycorp.letro.push.model

import androidx.annotation.StringDef
import androidx.annotation.StringRes
import tech.relaycorp.letro.push.model.PushChannel.ChannelId.Companion.ID_CONVERSATIONS

data class PushChannel(
    @ChannelId val id: String,
    @StringRes val name: Int,
) {

    @StringDef(ID_CONVERSATIONS)
    annotation class ChannelId {
        companion object {
            const val ID_CONVERSATIONS = "default"
        }
    }
}
