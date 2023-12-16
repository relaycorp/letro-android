package tech.relaycorp.letro.awala.ui.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.awala.AwalaManager
import javax.inject.Inject

@HiltViewModel
class AwalaInitializationErrorViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
) : ViewModel() {

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

    fun onTryAgainClick() {
        awalaManager.initializeGatewayAsync()
    }
}
