package tech.relaycorp.letro.ui.utils

import android.content.Context
import androidx.annotation.IntDef
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.ACCOUNT_CREATION_ID_ALREADY_EXISTS
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.ACCOUNT_LINKING_ID_ALREADY_EXISTS
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.AVATAR_TOO_BIG_ERROR
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.AVATAR_UNSUPPORTED_FORMAT
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.CONVERSATIONS_ARCHIVED
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.CONVERSATIONS_DELETED
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.CONVERSATIONS_UNARCHIVED
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider.Type.Companion.FILE_TOO_BIG_ERROR
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

    fun get(string: SnackbarString): String

    @IntDef(
        SEND_MESSAGE_ERROR,
        FILE_TOO_BIG_ERROR,
        ACCOUNT_LINKING_ID_ALREADY_EXISTS,
        ACCOUNT_CREATION_ID_ALREADY_EXISTS,
        AVATAR_TOO_BIG_ERROR,
        CONVERSATIONS_DELETED,
        CONVERSATIONS_ARCHIVED,
        CONVERSATIONS_UNARCHIVED,
        AVATAR_UNSUPPORTED_FORMAT,
    )
    annotation class Type {
        companion object {
            const val SEND_MESSAGE_ERROR = 0
            const val FILE_TOO_BIG_ERROR = 1
            const val ACCOUNT_LINKING_ID_ALREADY_EXISTS = 2
            const val ACCOUNT_CREATION_ID_ALREADY_EXISTS = 3
            const val AVATAR_TOO_BIG_ERROR = 4
            const val CONVERSATIONS_DELETED = 5
            const val CONVERSATIONS_ARCHIVED = 6
            const val CONVERSATIONS_UNARCHIVED = 7
            const val AVATAR_UNSUPPORTED_FORMAT = 8
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

    override fun get(string: SnackbarString): String {
        return when (val type = string.type) {
            SEND_MESSAGE_ERROR -> activity.getString(R.string.we_failed_to_send_this_via_awala)
            FILE_TOO_BIG_ERROR -> activity.getString(R.string.file_too_big_error_message)
            ACCOUNT_LINKING_ID_ALREADY_EXISTS -> activity.getString(R.string.you_already_waiting_for_id, *string.args)
            ACCOUNT_CREATION_ID_ALREADY_EXISTS -> activity.getString(R.string.you_already_have_account_with_this_id)
            AVATAR_TOO_BIG_ERROR -> activity.getString(R.string.avatar_too_big_error_message)
            CONVERSATIONS_DELETED -> activity.getString(R.string.snackbar_conversations_deleted)
            CONVERSATIONS_ARCHIVED -> activity.getString(R.string.snackbar_conversations_archived)
            CONVERSATIONS_UNARCHIVED -> activity.getString(R.string.snackbar_conversations_unarchived)
            AVATAR_UNSUPPORTED_FORMAT -> activity.getString(R.string.avatar_unsupported_type)
            else -> throw IllegalStateException("Unknown type $type")
        }
    }
}
