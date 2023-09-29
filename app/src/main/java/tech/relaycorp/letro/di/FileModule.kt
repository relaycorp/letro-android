package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import tech.relaycorp.letro.messages.filepicker.FileConverter
import tech.relaycorp.letro.messages.filepicker.FileConverterImpl
import tech.relaycorp.letro.messages.ui.utils.AttachmentInfoConverter
import tech.relaycorp.letro.messages.ui.utils.AttachmentInfoConverterImpl

@Module
@InstallIn(ViewModelComponent::class)
interface FileModule {

    @Binds
    @ViewModelScoped
    fun bindFileConverter(
        impl: FileConverterImpl,
    ): FileConverter

    @Binds
    fun bindAttachmentInfoConverter(
        impl: AttachmentInfoConverterImpl,
    ): AttachmentInfoConverter
}
