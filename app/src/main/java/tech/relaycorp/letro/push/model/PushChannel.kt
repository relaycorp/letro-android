package tech.relaycorp.letro.push.model

import androidx.annotation.StringDef
import androidx.annotation.StringRes
import tech.relaycorp.letro.push.model.PushChannel.ChannelId.Companion.ID_DEFAULT

data class PushChannel(
    @ChannelId val id: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
) {

    @StringDef(ID_DEFAULT)
    annotation class ChannelId {
        companion object {
            const val ID_DEFAULT = "default"
        }
    }
}
