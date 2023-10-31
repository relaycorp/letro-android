package tech.relaycorp.letro.conversation.attachments.dto

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

@Parcelize
data class GsonAttachments(
    val files: List<AttachmentToShare.File> = emptyList(),
    val strings: List<AttachmentToShare.String> = emptyList(),
) : Parcelable {

    override fun toString(): String {
        return Uri.encode(Gson().toJson(this))
    }

    companion object {

        fun from(attachments: List<AttachmentToShare>): GsonAttachments {
            return GsonAttachments(
                files = attachments.filterIsInstance(AttachmentToShare.File::class.java),
                strings = attachments.filterIsInstance(AttachmentToShare.String::class.java),
            )
        }

        @JvmField
        val NavType: NavType<GsonAttachments> = object : NavType<GsonAttachments>(false) {
            override val name: String
                get() = "GsonAttachments"

            override fun put(bundle: Bundle, key: String, value: GsonAttachments) {
                bundle.putParcelable(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): GsonAttachments? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(key, GsonAttachments::class.java) as GsonAttachments
                } else {
                    bundle.getParcelable<GsonAttachments>(key)
                }
            }

            override fun parseValue(value: String): GsonAttachments {
                return Gson().fromJson(value, GsonAttachments::class.java)
            }
        }
    }
}
