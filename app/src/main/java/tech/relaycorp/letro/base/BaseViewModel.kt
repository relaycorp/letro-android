package tech.relaycorp.letro.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import tech.relaycorp.letro.base.utils.SnackbarString

@OptIn(FlowPreview::class, DelicateCoroutinesApi::class)
abstract class BaseViewModel : ViewModel() {
    protected val showSnackbarDebounced: MutableSharedFlow<SnackbarString> = MutableSharedFlow()

    private val _showSnackbarMutable: MutableSharedFlow<SnackbarString> = MutableSharedFlow()
    val showSnackbar: SharedFlow<SnackbarString>
        get() = _showSnackbarMutable

    init {
        viewModelScope.launch {
            showSnackbarDebounced
                .debounce(500L)
                .collect {
                    _showSnackbarMutable.emit(it)
                }
        }
    }
}
