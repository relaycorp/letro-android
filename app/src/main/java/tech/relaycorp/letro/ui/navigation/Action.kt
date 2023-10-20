package tech.relaycorp.letro.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Action : Parcelable {

    val accountId: String?

    @Parcelize
    data class OpenConversation(
        val conversationId: String,
        override val accountId: String,
    ) : Action

    @Parcelize
    data class OpenMainPage(
        override val accountId: String,
    ) : Action

    @Parcelize
    data class OpenContacts(
        override val accountId: String,
    ) : Action

    @Parcelize
    data class OpenPairRequest(
        val contactAccountId: String,
        override val accountId: String? = null,
    ) : Action

    @Parcelize
    data class OpenAccountLinking(
        val domain: String = "",
        val awalaEndpoint: String = "",
        val token: String = "",
        override val accountId: String? = null,
    ) : Action
}
