package tech.relaycorp.letro.conversation.attachments

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    suspend fun saveAttachments(conversationId: UUID, messageId: Long, attachments: List<File.FileWithContent>)
    suspend fun saveMessageAttachments(conversationId: UUID, messageId: Long, attachments: List<AttachmentAwalaWrapper>)
    suspend fun deleteAttachments(conversationId: UUID)
    suspend fun getById(id: UUID): Attachment?
}

class AttachmentsRepositoryImpl @Inject constructor(
    private val attachmentsDao: AttachmentsDao,
    private val fileManager: FileManager,
    @ConversationFileConverterAnnotation private val fileConverter: FileConverter,
) : AttachmentsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _attachments: MutableStateFlow<List<Attachment>> = MutableStateFlow(emptyList())
    override val attachments: Flow<List<Attachment>>
        get() = _attachments

    init {
        scope.launch {
            attachmentsDao.getAll().collect {
                _attachments.value = it
            }
        }
    }

    override suspend fun saveMessageAttachments(
        conversationId: UUID,
        messageId: Long,
        attachments: List<AttachmentAwalaWrapper>,
    ) {
        saveAttachments(
            conversationId = conversationId,
            messageId = messageId,
            attachments = attachments.mapNotNull { fileConverter.getFile(it) },
        )
    }

    override suspend fun saveAttachments(conversationId: UUID, messageId: Long, attachments: List<File.FileWithContent>) {
        attachmentsDao.insert(
            attachments
                .map { file ->
                    val path = fileManager.save(file)
                    Attachment(
                        fileId = file.id,
                        path = path,
                        messageId = messageId,
                        conversationId = conversationId,
                    )
                },
        )
    }

    override suspend fun deleteAttachments(conversationId: UUID) {
        _attachments.value
            .filter { it.conversationId == conversationId }
            .forEach { fileManager.delete(it.path) }
    }

    override suspend fun getById(id: UUID): Attachment? {
        return attachmentsDao.getById(id)
    }
}
