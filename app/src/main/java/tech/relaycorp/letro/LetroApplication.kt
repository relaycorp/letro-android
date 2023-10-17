package tech.relaycorp.letro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.utils.crypto.MasterKeyProvider
import javax.inject.Inject

@HiltAndroidApp
open class LetroApplication : Application() {

    @Inject
    lateinit var awalaManager: AwalaManager

    override fun onCreate() {
        MasterKeyProvider.init(this)
        super.onCreate()
    }
}
