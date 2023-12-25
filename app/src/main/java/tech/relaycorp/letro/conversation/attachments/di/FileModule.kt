package tech.relaycorp.letro.conversation.attachments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManagerImpl
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverterImpl

@Module
@InstallIn(SingletonComponent::class)
interface FileModule {

    @Binds
    fun bindAttachmentInfoConverter(
        impl: AttachmentInfoConverterImpl,
    ): AttachmentInfoConverter

    @Binds
    fun bindFileSaver(
        impl: FileManagerImpl,
    ): FileManager
}
