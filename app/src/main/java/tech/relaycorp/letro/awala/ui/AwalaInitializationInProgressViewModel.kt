package tech.relaycorp.letro.awala.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.di.MainModule
import javax.inject.Inject

@HiltViewModel
class AwalaInitializationInProgressViewModel @Inject constructor(
    @MainModule.AwalaInitializationStringsIndexPointer private val _stringsIndexPointer: MutableStateFlow<Int>,
) : ViewModel() {

    val stringsIndexPointer: StateFlow<Int> = _stringsIndexPointer

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1_000L)
                _stringsIndexPointer.emit(_stringsIndexPointer.value + 1)
            }
        }
    }
}
