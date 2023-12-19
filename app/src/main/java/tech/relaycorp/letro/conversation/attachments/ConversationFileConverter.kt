package tech.relaycorp.letro.conversation.attachments

import android.content.ContentResolver
import tech.relaycorp.letro.conversation.attachments.filepicker.DefaultFileConverter
import tech.relaycorp.letro.conversation.di.MessageSizeLimitBytes
import javax.inject.Inject

class ConversationFileConverter @Inject constructor(
    contentResolver: ContentResolver,
    @MessageSizeLimitBytes messageSizeLimitBytes: Int,
) : DefaultFileConverter(contentResolver, messageSizeLimitBytes)
