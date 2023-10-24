package tech.relaycorp.letro.awala.ui.notinstalled

import android.annotation.SuppressLint
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

@SuppressLint("WrongConstant")
@HiltViewModel
class AwalaNotInstalledViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
) : ViewModel() {

    private val _isAwalaInitializingShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAwalaInitializingShown: StateFlow<Boolean>
        get() = _isAwalaInitializingShown

    private var isFirstResuming = true

    init {
        viewModelScope.launch {
            awalaManager.awalaUnsuccessfulBindings.collect {
                _isAwalaInitializingShown.emit(false)
            }
        }
    }

    /**
     * The current logic works like this:
     * 1. We check whether Awala is installed or not on screen resuming.
     * 2. During this time we show the animation to show the user that something is going on.
     * 3. If binding is UNSUCCESSFUL (= Awala is not installed) : usually this checking is almost instant, so this animation will be interupted by @_isAwalaInitializingShown flow from AwalaManager, which is subscribed in constructor of AwalaNotInstalledViewModel
     * 4. If binding is SUCCESSFUL (= Awala is installed), then this screen will be automatically closed and futher navigation logic is performed by navigation controller (by now it's MainViewModel, which shows AwalaInitializationInProgress)
     *
     * NOTE: to display the same string while screens changing (step 4), the common strings flow is being used (see AwalaInitializationInProgressViewModel)
     */
    fun onScreenResumed() {
        if (!isFirstResuming) {
            viewModelScope.launch(Dispatchers.IO) {
                _isAwalaInitializingShown.emit(true)
                delay(3_000L)
                _isAwalaInitializingShown.emit(false)
            }
            awalaManager.initializeGatewayAsync()
        } else {
            isFirstResuming = false
        }
    }

    fun onScreenDestroyed() {
        isFirstResuming = true
    }
}
