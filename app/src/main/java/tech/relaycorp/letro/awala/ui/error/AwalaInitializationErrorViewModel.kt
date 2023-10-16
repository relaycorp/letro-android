package tech.relaycorp.letro.awala.ui.error

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.awala.AwalaManager
import javax.inject.Inject

@HiltViewModel
class AwalaInitializationErrorViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val isConfigureEndpointsOnResume = savedStateHandle.get<Boolean>(CONFIGURE_ENDPOINTS_ON_RESUME) ?: false

    private val _isAwalaInitializingShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAwalaInitializingShown: StateFlow<Boolean>
        get() = _isAwalaInitializingShown

    init {
        viewModelScope.launch {
            awalaManager.awalaUnsuccessfulConfigurations.collect {
                _isAwalaInitializingShown.emit(false)
            }
        }
    }

    fun onScreenResumed() {
        if (isConfigureEndpointsOnResume) {
            viewModelScope.launch(Dispatchers.IO) {
                _isAwalaInitializingShown.emit(true)
                delay(8_000L)
                _isAwalaInitializingShown.emit(false)
            }
            awalaManager.configureEndpointsAsync()
        }
    }

    fun onTryAgainClick() {
        awalaManager.initializeGatewayAsync()
    }

    companion object {
        const val CONFIGURE_ENDPOINTS_ON_RESUME = "register_endpoints_on_resume"
    }
}
