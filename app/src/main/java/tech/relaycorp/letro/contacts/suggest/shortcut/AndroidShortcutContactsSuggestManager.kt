package tech.relaycorp.letro.contacts.suggest.shortcut

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.pm.ShortcutManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.contacts.suggest.ContactSuggestsManager
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.main.ui.MainActivity
import tech.relaycorp.letro.utils.di.IODispatcher
import tech.relaycorp.letro.utils.ext.isLessThanWeeksAgo
import tech.relaycorp.letro.utils.shortcut.toShortcutInfo
import javax.inject.Inject

interface AndroidShortcutContactsSuggestManager

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidShortcutContactsSuggestManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val suggestsManager: ContactSuggestsManager,
    private val contactsRepository: ContactsRepository,
    private val conversationsRepository: ConversationsRepository,
    private val accountRepository: AccountRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : AndroidShortcutContactsSuggestManager {

    private val scope = CoroutineScope(ioDispatcher)

    init {
        scope.launch {
            combine(
                accountRepository.currentAccount,
                conversationsRepository.conversations,
            ) { account, conversations ->
                Pair(account, conversations)
            }.flatMapLatest { accountAndConversations ->
                val accountId = accountAndConversations.first?.accountId
                    ?: return@flatMapLatest flowOf<Pair<List<Contact>, List<ExtendedConversation>>>(Pair(emptyList(), emptyList()))
                return@flatMapLatest flowOf(accountAndConversations.second).combine(contactsRepository.getContacts(accountId)) { conversations, contacts -> Pair(contacts, conversations) }
            }.collect { pair ->
                setShortcuts(pair.first, pair.second)
            }
        }
        scope.launch {
            contactsRepository.contactDeleteEvents.collect { contactId ->
                ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(contactId.toString()))
            }
        }
    }

    private suspend fun setShortcuts(
        contacts: List<Contact>,
        conversations: List<ExtendedConversation>,
    ) {
        withContext(ioDispatcher) {
            val pairedContacts = contacts
                .filter { it.status == ContactPairingStatus.COMPLETED }
            val sortedPairedContacts =
                suggestsManager.orderByRelevance(pairedContacts, conversations)
                    .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
                    .filter { contact ->
                        conversations
                            .filter { it.contactVeraId == contact.contactVeraId }
                            .any { conversation ->
                                conversation.lastMessage.sentAt.isLessThanWeeksAgo(4L)
                            }
                    }
            Log.i(TAG, "Push ${sortedPairedContacts.size} shortcuts")
            ShortcutManagerCompat.setDynamicShortcuts(
                context,
                sortedPairedContacts.toShortcutInfo(
                    context = context,
                    intentBuilder = { contact ->
                        Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Intent.EXTRA_SHORTCUT_ID else EXTRA_SHORTCUT_ID,
                                contact.id.toString(),
                            )
                        }
                    },
                ),
            )
        }
    }
}

private const val TAG = "ShortcutSuggestManager"
const val EXTRA_SHORTCUT_ID = "android.intent.extra.shortcut.ID" // I did not find any documentation, where to get this constant on API versions before Q
