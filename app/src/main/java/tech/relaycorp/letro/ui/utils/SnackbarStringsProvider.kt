package tech.relaycorp.letro.ui.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
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
}
