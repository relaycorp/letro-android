package tech.relaycorp.letro.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
abstract class BaseViewModel : ViewModel() {
    protected val showSnackbarDebounced: MutableSharedFlow<Int> = MutableSharedFlow()

    private val _showSnackbarMutable: MutableSharedFlow<Int> = MutableSharedFlow()
    val showSnackbar: SharedFlow<Int>
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
