package tech.relaycorp.letro.awala.ui.error

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.relaycorp.letro.awala.AwalaManager
import javax.inject.Inject

@HiltViewModel
class AwalaInitializationErrorViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
) : ViewModel() {

    fun onTryAgainClick() {
        awalaManager.initializeGatewayAsync()
    }
}
