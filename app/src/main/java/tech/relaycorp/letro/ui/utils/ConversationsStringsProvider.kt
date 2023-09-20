package tech.relaycorp.letro.ui.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
import javax.inject.Inject

interface ConversationsStringsProvider {
    val noSubject: String
}

class ConversationsStringsProviderImpl @Inject constructor(
    @ActivityContext private val context: Context,
) : ConversationsStringsProvider {

    override val noSubject: String
        get() = context.getString(R.string.no_subject)
}
