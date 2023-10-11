package tech.relaycorp.letro.ui.utils

import android.content.Context
import androidx.annotation.IntDef
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.SEND_MESSAGE_ERROR
import javax.inject.Inject

interface SnackbarStringsProvider {
    val contactDeleted: String
    val contactEdited: String
    val messageSent: String
    val conversationDeleted: String
    val conversationArchived: String
    val conversationUnarchived: String
    val notificationPermissionDenied: String
    val goToSettings: String
    val accountDeleted: String
    val youNeedAtLeastOneContact: String
    val youNoLongerConnected: String
    val addContact: String

    fun get(@Type type: Int): String

    @IntDef(SEND_MESSAGE_ERROR)
    annotation class Type {
        companion object {
            const val SEND_MESSAGE_ERROR = 0
        }
    }
}

class SnackbarStringsProviderImpl @Inject constructor(
    @ActivityContext private val activity: Context,
) : SnackbarStringsProvider {
    override val contactEdited: String
        get() = activity.getString(R.string.snackbar_contact_edited)

    override val contactDeleted: String
        get() = activity.getString(R.string.snackbar_contact_deleted)

    override val messageSent: String
        get() = activity.getString(R.string.snackbar_message_sent)

    override val conversationDeleted: String
        get() = activity.getString(R.string.snackbar_conversation_deleted)

    override val conversationArchived: String
        get() = activity.getString(R.string.snackbar_conversation_archived)

    override val conversationUnarchived: String
        get() = activity.getString(R.string.snackbar_conversation_unarchived)

    override val notificationPermissionDenied: String
        get() = activity.getString(R.string.we_need_your_permission)

    override val goToSettings: String
        get() = activity.getString(R.string.go_to_settings)

    override val accountDeleted: String
        get() = activity.getString(R.string.account_deleted)

    override val youNeedAtLeastOneContact: String
        get() = activity.getString(R.string.you_need_at_least_one_contact)

    override val addContact: String
        get() = activity.getString(R.string.general_pair_with_others)

    override val youNoLongerConnected: String
        get() = activity.getString(R.string.you_cannot_reply_not_connected)

    override fun get(type: Int): String {
        return when (type) {
            SEND_MESSAGE_ERROR -> activity.getString(R.string.we_failed_to_send_this_via_awala)
            else -> throw IllegalStateException("Unknown type $type")
        }
    }
}
