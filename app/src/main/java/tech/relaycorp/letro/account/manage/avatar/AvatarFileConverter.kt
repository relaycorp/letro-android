package tech.relaycorp.letro.account.manage.avatar

import android.content.ContentResolver
import tech.relaycorp.letro.account.di.AvatarSizeLimitBytes
import tech.relaycorp.letro.conversation.attachments.filepicker.DefaultFileConverter
import javax.inject.Inject

class AvatarFileConverter @Inject constructor(
    contentResolver: ContentResolver,
    @AvatarSizeLimitBytes avatarSizeLimitBytes: Int,
) : DefaultFileConverter(contentResolver, avatarSizeLimitBytes)
