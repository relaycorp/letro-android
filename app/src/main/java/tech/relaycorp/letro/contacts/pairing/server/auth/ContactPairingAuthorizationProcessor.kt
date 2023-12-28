package tech.relaycorp.letro.contacts.pairing.server.auth

import android.util.Log
import tech.relaycorp.awaladroid.endpoint.InvalidAuthorizationException
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.utils.Logger
import java.io.File
import javax.inject.Inject

class ContactPairingAuthorizationProcessor @Inject constructor(
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
    private val accountsDao: AccountDao,
    parser: ContactPairingAuthorizationParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.ContactPairingAuthorization>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.ContactPairingAuthorization,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        val nodeId = try {
            awalaManager.importPrivateThirdPartyAuth(content.authData, recipientNodeId)
        } catch (e: InvalidAuthorizationException) {
            Log.w(TAG, e)
            return
        }

        Log.d(TAG, "Contact auth received ($nodeId).")

        val recipientAccount = accountsDao.getByFirstPartyEndpointNodeId(recipientNodeId) ?: run {
            Log.d(TAG, "Couldn't find account of recipient $recipientNodeId")
            return
        }

        val contacts = contactsDao.getContactsByContactEndpointId(
            contactEndpointId = nodeId,
        ).filter { it.ownerVeraId == recipientAccount.accountId }

        if (contacts.isEmpty()) {
            Log.d(TAG, "Couldn't find contacts with nodeId=$nodeId (${recipientAccount.accountId})")
            return
        }

        contacts.forEach { contact ->
            Log.d(TAG, "Update status for nodeId=$nodeId")
            contactsDao.update(
                contact.copy(
                    status = ContactPairingStatus.COMPLETED,
                ),
            )
            contactPairingNotificationManager.showSuccessPairingNotification(contact)

            if (contact.contactEndpointId == null) return@forEach
            recipientAccount.avatarPath?.let { avatarPath ->
                val avatarFile = File(avatarPath)
                if (avatarFile.exists()) {
                    awalaManager.sendMessage(
                        outgoingMessage = AwalaOutgoingMessage(
                            type = MessageType.ContactPhotoUpdated,
                            content = avatarFile.readBytes(),
                        ),
                        recipient = AwalaEndpoint.Private(
                            nodeId = contact.contactEndpointId,
                        ),
                        senderAccount = recipientAccount,
                    )
                }
            }
        }
    }

    private companion object {
        private const val TAG = "ContactPairingAuthorizationProcessorImpl"
    }
}
