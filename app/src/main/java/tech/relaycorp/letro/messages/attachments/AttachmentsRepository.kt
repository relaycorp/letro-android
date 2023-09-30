package tech.relaycorp.letro.messages.attachments

import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.messages.filepicker.FileSaver
import tech.relaycorp.letro.messages.filepicker.model.File
import tech.relaycorp.letro.messages.storage.AttachmentsDao
import tech.relaycorp.letro.messages.storage.entity.Attachment
import java.util.UUID
import javax.inject.Inject

interface AttachmentsRepository {
    val attachments: Flow<List<Attachment>>
    suspend fun saveAttachments(messageId: Long, attachments: List<File.FileWithContent>)
    suspend fun getById(id: UUID): Attachment?
}

class AttachmentsRepositoryImpl @Inject constructor(
    private val attachmentsDao: AttachmentsDao,
    private val fileSaver: FileSaver,
) : AttachmentsRepository {

    override val attachments: Flow<List<Attachment>>
        get() = attachmentsDao.getAll()

    override suspend fun saveAttachments(messageId: Long, attachments: List<File.FileWithContent>) {
        attachmentsDao.insert(
            attachments
                .map { file ->
                    val path = fileSaver.save(file)
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
