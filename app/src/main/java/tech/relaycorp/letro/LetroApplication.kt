package tech.relaycorp.letro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.utils.crypto.MasterKeyProvider
import javax.inject.Inject

@HiltAndroidApp
open class LetroApplication : Application() {

    @Inject
    lateinit var awalaManager: AwalaManager

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var masterKeyProvider: MasterKeyProvider
}
