package tech.relaycorp.letro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import tech.relaycorp.letro.awala.AwalaManager
import javax.inject.Inject

@HiltAndroidApp
open class App : Application() {

    @Inject
    lateinit var awalaManager: AwalaManager
}
