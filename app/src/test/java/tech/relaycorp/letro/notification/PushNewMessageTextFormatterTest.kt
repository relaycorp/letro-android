package tech.relaycorp.letro.notification

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.push.PushNewMessageTextFormatterImpl

class PushNewMessageTextFormatterTest {

    private val formatter = PushNewMessageTextFormatterImpl()

    @Test
    fun `Test Text Without Subject And Attachments`() {
        val subject: String? = null
        val message: String = "This is text of the message"
        val attachments = emptyList<String>()
        val notificationText = formatter.getText(
            subject = subject,
            messageText = message,
            attachments = attachments,
        )
        notificationText shouldBe "This is text of the message"
    }

    @Test
    fun `Test Text With Subject, but Without Attachments`() {
        val subject: String? = "Subject"
        val message: String = "This is text of the message"
        val attachments = emptyList<String>()
        val notificationText = formatter.getText(
            subject = subject,
            messageText = message,
            attachments = attachments,
        )
        notificationText shouldBe "Subject - This is text of the message"
    }

    @Test
    fun `Test Text With Subject and Attachments`() {
        val subject: String? = "Subject"
        val message: String = "This is text of the message"
        val attachments = listOf<String>("attachment_1.mp3")
        val notificationText = formatter.getText(
            subject = subject,
            messageText = message,
            attachments = attachments,
        )
        notificationText shouldBe "Subject - This is text of the message attachment_1.mp3"
    }

    @Test
    fun `Test With Subject and Attachments, but without text`() {
        val subject: String? = "Subject"
        val message: String = ""
        val attachments = listOf<String>("attachment_1.mp3")
        val notificationText = formatter.getText(
            subject = subject,
            messageText = message,
            attachments = attachments,
        )
        notificationText shouldBe "Subject - attachment_1.mp3"
    }

    @Test
    fun `Test Without Subject and Text, but With Attachment`() {
        val subject: String? = null
        val message: String = ""
        val attachments = listOf<String>("attachment_1.mp3")
        val notificationText = formatter.getText(
            subject = subject,
            messageText = message,
            attachments = attachments,
        )
        notificationText shouldBe "attachment_1.mp3"
    }
}
