package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.messages.filepicker.FileConverter
import tech.relaycorp.letro.messages.filepicker.FileConverterImpl
import tech.relaycorp.letro.messages.filepicker.FileSaver
import tech.relaycorp.letro.messages.filepicker.FileSaverImpl
import tech.relaycorp.letro.messages.ui.utils.AttachmentInfoConverter
import tech.relaycorp.letro.messages.ui.utils.AttachmentInfoConverterImpl

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
}
