package tech.relaycorp.letro.awala.processor

import android.util.Log
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManagerImpl
import javax.inject.Inject

class UnknownMessageProcessor @Inject constructor() : AwalaMessageProcessor {

    override suspend fun process(message: IncomingMessage) {
        Log.w(AwalaManagerImpl.TAG, "Unknown message processor for type: ${message.type}")
    }
}
