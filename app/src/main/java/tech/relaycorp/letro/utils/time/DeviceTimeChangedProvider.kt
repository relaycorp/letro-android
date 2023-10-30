package tech.relaycorp.letro.utils.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface DeviceTimeChangedProvider {
    fun addListener(listener: OnDeviceTimeChangedListener)
}

class DeviceTimeChangedProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceTimeChangedProvider {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return
            when (intent.action) {
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> listeners.forEach { it.onChanged() }
            }
        }
    }

    private val listeners: MutableSet<OnDeviceTimeChangedListener> = hashSetOf()

    init {
        context.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            },
        )
    }

    override fun addListener(listener: OnDeviceTimeChangedListener) {
        listeners.add(listener)
    }
}

interface OnDeviceTimeChangedListener {
    fun onChanged()
}
