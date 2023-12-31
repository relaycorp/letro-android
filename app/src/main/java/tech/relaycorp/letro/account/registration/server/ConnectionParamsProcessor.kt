package tech.relaycorp.letro.account.registration.server

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.registration.server.dto.PublicKeyImportData
import tech.relaycorp.letro.account.storage.repository.AccountRepository
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
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

class ConnectionParamsProcessor @Inject constructor(
    private val accountsRepository: AccountRepository,
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
    parser: ConnectionParamsParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.ConnectionParams>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.ConnectionParams,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        val publicThirdPartyEndpoint = awalaManager.importPublicThirdPartyEndpoint(content.connectionParams, recipientNodeId)

        val accountsToUpdate = accountsRepository.allAccounts.value
            .filter { (it.domain == publicThirdPartyEndpoint.internetAddress || it.awalaEndpointId == publicThirdPartyEndpoint.internetAddress) && it.token?.isNotEmptyOrBlank() == true && it.status != AccountStatus.CREATED }
        val contactsToUpdate = contactsDao.getContactsWithNoEndpoint(
            contactVeraId = publicThirdPartyEndpoint.internetAddress,
            pairingStatus = ContactPairingStatus.REQUEST_SENT,
        )

        try {
            accountsToUpdate.forEach {
                awalaManager.authorizePublicThirdPartyEndpoint(
                    account = it,
                    thirdPartyEndpoint = publicThirdPartyEndpoint,
                )
            }
        } catch (e: AwaladroidException) {
            Log.w(TAG, e)
            contactsToUpdate.forEach {
                contactsDao.deleteContact(it)
                contactPairingNotificationManager.showFailedPairingNotification(it)
            }
            accountsToUpdate.forEach {
                accountsRepository.updateAccount(
                    it.copy(
                        status = AccountStatus.ERROR_LINKING,
                    ),
                )
            }
            return
        }

        contactsToUpdate.forEach { contact ->
            contactsDao.update(
                contact.copy(
                    status = ContactPairingStatus.COMPLETED,
                    contactEndpointId = publicThirdPartyEndpoint.nodeId,
                    isPrivateEndpoint = false,
                ),
            )
            contactPairingNotificationManager.showSuccessPairingNotification(contact)
        }

        accountsToUpdate.forEach { account ->
            val encodedKey = account.veraidPrivateKey.deserialiseKeyPair().public.spkiEncode()
            val base64Encoding = Base64.encodeToString(encodedKey.encoded, Base64.NO_WRAP)
            val jsonContent = Gson().toJson(
                PublicKeyImportData(
                    publicKeyImportToken = account.token!!,
                    publicKey = base64Encoding,
                ),
            )
            try {
                accountsRepository.updateAccount(
                    account.copy(
                        veraidAuthEndpointId = publicThirdPartyEndpoint.nodeId,
                        token = null,
                    ),
                )
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.MemberPublicKeyImport,
                        content = jsonContent.toByteArray(),
                    ),
                    recipient = AwalaEndpoint.Public(
                        nodeId = publicThirdPartyEndpoint.nodeId,
                    ),
                    senderAccount = account,
                )
            } catch (e: AwaladroidException) {
                Log.w(TAG, e)
                accountsRepository.updateAccount(account.copy(status = AccountStatus.ERROR_LINKING))
            }
        }
    }
}

private const val TAG = "ConnectionParamsProcessor"
