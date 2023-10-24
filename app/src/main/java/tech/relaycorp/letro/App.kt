package tech.relaycorp.letro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.suggest.shortcut.AndroidShortcutContactsSuggestManager
import javax.inject.Inject

@HiltAndroidApp
open class App : Application() {

    @Inject
    lateinit var awalaManager: AwalaManager

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var shortcutContactsSuggestManager: AndroidShortcutContactsSuggestManager
}
