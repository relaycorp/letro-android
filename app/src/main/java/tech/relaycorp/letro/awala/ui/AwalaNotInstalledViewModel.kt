package tech.relaycorp.letro.awala.ui

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.awala.AwalaInitializationState
import tech.relaycorp.letro.awala.AwalaManager
import javax.inject.Inject

@SuppressLint("WrongConstant")
@HiltViewModel
class AwalaNotInstalledViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
) : ViewModel() {

    private val _changeTextSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val changeTextSignal: SharedFlow<Unit> = _changeTextSignal
    private var changeTextJob: Job? = null

    private val _awalaInstallationProgressUiState: MutableStateFlow<Float?> = MutableStateFlow(null)
    val awalaInstallationProgressUiState: StateFlow<Float?>
        get() = _awalaInstallationProgressUiState

    init {
        viewModelScope.launch {
            awalaManager.awalaInitializationState.collect {
                if (it >= AwalaInitializationState.GATEWAY_BINDING) {
                    _awalaInstallationProgressUiState.emit(it / AwalaInitializationState.STEPS_COUNT.toFloat())
                    startChangingTexts()
                    if (it == AwalaInitializationState.INITIALIZED) {
                        stopUpdateProgress()
                    }
                } else {
                    stopUpdateProgress()
                }
            }
        }
    }

    private fun stopUpdateProgress() {
        viewModelScope.launch {
            _awalaInstallationProgressUiState.emit(null)
        }
        changeTextJob?.cancel()
        changeTextJob = null
    }

    private fun startChangingTexts() {
        if (changeTextJob != null) {
            return
        }
        changeTextJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1_000L)
                _changeTextSignal.emit(Unit)
            }
        }
    }
}
