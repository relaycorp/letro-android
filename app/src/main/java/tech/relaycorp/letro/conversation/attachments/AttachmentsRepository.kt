package tech.relaycorp.letro.conversation.attachments

import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.di.ConversationFileConverterAnnotation
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.storage.dao.AttachmentsDao
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import java.util.UUID
import javax.inject.Inject

interface AttachmentsRepository {
    val attachments: Flow<List<Attachment>>
    suspend fun saveAttachments(messageId: Long, attachments: List<File.FileWithContent>)
    suspend fun saveMessageAttachments(messageId: Long, attachments: List<AttachmentAwalaWrapper>)
    suspend fun getById(id: UUID): Attachment?
}

class AttachmentsRepositoryImpl @Inject constructor(
    private val attachmentsDao: AttachmentsDao,
    private val fileManager: FileManager,
    @ConversationFileConverterAnnotation private val fileConverter: FileConverter,
) : AttachmentsRepository {

    override val attachments: Flow<List<Attachment>>
        get() = attachmentsDao.getAll()

    override suspend fun saveMessageAttachments(
        messageId: Long,
        attachments: List<AttachmentAwalaWrapper>,
    ) {
        saveAttachments(
            messageId = messageId,
            attachments = attachments.mapNotNull { fileConverter.getFile(it) },
        )
    }

    override suspend fun saveAttachments(messageId: Long, attachments: List<File.FileWithContent>) {
        attachmentsDao.insert(
            attachments
                .map { file ->
                    val path = fileManager.save(file)
                    Attachment(
                        fileId = file.id,
                        path = path,
                        messageId = messageId,
                    )
                },
        )
    }

    override suspend fun getById(id: UUID): Attachment? {
        return attachmentsDao.getById(id)
    }
}
