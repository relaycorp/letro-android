package tech.realycorp.letro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient

@HiltAndroidApp
open class App : Application() {

    private val coroutineContext = Dispatchers.Default + SupervisorJob()

    // TODO
//    @Inject
//    lateinit var receivePong: GatewayRepository

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(coroutineContext).launch {
            setupGateway()
        }
    }

    protected open suspend fun setupGateway() {
        Awala.setUp(this)
        try {
            GatewayClient.bind()
        } catch (exp: GatewayBindingException) {
            // TODO logger.log(Level.WARNING, "Gateway binding exception", exp)
            openNoGateway()
            return
        }

        // TODO Collect ping flow from the repository (string, byte)
        //  GatewayClient.receiveMessages().collect()
    }

    private fun openNoGateway() {
        // TODO navigate to NoGatewayScreen
    }
}
