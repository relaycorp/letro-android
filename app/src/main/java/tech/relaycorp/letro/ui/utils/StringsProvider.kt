package tech.relaycorp.letro.ui.utils

import javax.inject.Inject

interface StringsProvider {
    val snackbar: SnackbarStringsProvider
    val conversations: ConversationsStringsProvider
    val awalaInitializationStringsProvider: AwalaInitializationStringsProvider
}

class StringsProviderImpl @Inject constructor(
    override val snackbar: SnackbarStringsProvider,
    override val conversations: ConversationsStringsProvider,
    override val awalaInitializationStringsProvider: AwalaInitializationStringsProvider,
) : StringsProvider
