package tech.relaycorp.letro.push

import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

interface PushNewMessageTextFormatter {
    fun getText(
        subject: String?,
        messageText: String,
        attachments: List<String>,
    ): String
}

class PushNewMessageTextFormatterImpl @Inject constructor() : PushNewMessageTextFormatter {

    override fun getText(
        subject: String?,
        messageText: String,
        attachments: List<String>,
    ): String {
        return """
            ${if (subject != null) "$subject - " else ""}${if (messageText.isNotEmptyOrBlank()) messageText else ""}${if (attachments.isNotEmpty()) " ${attachments.first()}" else ""}
        """.trimIndent()
    }
}
