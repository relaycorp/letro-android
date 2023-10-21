package tech.relaycorp.letro.conversation.attachments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverterImpl
import tech.relaycorp.letro.conversation.attachments.filepicker.FileSaver
import tech.relaycorp.letro.conversation.attachments.filepicker.FileSaverImpl
import tech.relaycorp.letro.conversation.attachments.sharing.ShareAttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.sharing.ShareAttachmentsRepositoryImpl
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FileModule {
    @Binds
    fun bindFileConverter(
        impl: FileConverterImpl,
    ): FileConverter

    @Binds
    fun bindAttachmentInfoConverter(
        impl: AttachmentInfoConverterImpl,
    ): AttachmentInfoConverter

    @Binds
    fun bindFileSaver(
        impl: FileSaverImpl,
    ): FileSaver

    @Binds
    @Singleton
    fun bindShareAttachmentsRepository(
        impl: ShareAttachmentsRepositoryImpl,
    ): ShareAttachmentsRepository
}
