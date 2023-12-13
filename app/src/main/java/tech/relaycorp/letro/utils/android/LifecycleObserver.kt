package tech.relaycorp.letro.utils.android

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class LifecycleObserver(
    private val onStart: () -> Unit,
    private val onStop: () -> Unit,
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        onStart()
    }

    override fun onStop(owner: LifecycleOwner) {
        onStop()
    }
}
