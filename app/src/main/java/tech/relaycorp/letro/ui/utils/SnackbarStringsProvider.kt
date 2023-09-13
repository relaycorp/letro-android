package tech.relaycorp.letro.ui.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
import javax.inject.Inject

interface SnackbarStringsProvider {
    val contactDeleted: String
    val contactEdited: String
}

class SnackbarStringsProviderImpl @Inject constructor(
    @ActivityContext private val activity: Context,
) : SnackbarStringsProvider {
    override val contactEdited: String
        get() = activity.getString(R.string.snackbar_contact_edited)

    override val contactDeleted: String
        get() = activity.getString(R.string.snackbar_contact_deleted)
}
