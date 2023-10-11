package tech.relaycorp.letro.account.registration.server

import android.util.Base64
import com.google.gson.Gson
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.registration.server.dto.PublicKeyImportData
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import tech.relaycorp.letro.utils.crypto.spkiEncode
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

interface ConnectionParamsProcessor : AwalaMessageProcessor

class ConnectionParamsProcessorImpl @Inject constructor(
    private val accountsRepository: AccountRepository,
): ConnectionParamsProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val publicThirdPartyEndpoint = PublicThirdPartyEndpoint.import(message.content)
        accountsRepository.allAccounts.value
            .filter { it.domain == publicThirdPartyEndpoint.internetAddress && it.token?.isNotEmptyOrBlank() == true }
            .forEach { account ->
                val encodedKey = account.veraidPrivateKey.deserialiseKeyPair().public.spkiEncode()
                val base64Encoding = Base64.encodeToString(encodedKey.encoded, Base64.NO_WRAP)
                val jsonContent = Gson().toJson(
                    PublicKeyImportData(
                        publicKeyImportToken = account.token!!,
                        publicKey = base64Encoding
                    )
                )
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.MemberPublicKeyImport,
                        content = jsonContent.toByteArray(),
                    ),
                    recipient = MessageRecipient.Server(
                        nodeId = publicThirdPartyEndpoint.nodeId,
                    )
                )
            }
    }

}