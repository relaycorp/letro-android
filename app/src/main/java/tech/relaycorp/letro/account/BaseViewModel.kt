package tech.relaycorp.letro.account

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel : ViewModel() {
    protected val _showSnackbar: MutableSharedFlow<Int> = MutableSharedFlow()
    val showSnackbar: SharedFlow<Int>
        get() = _showSnackbar
}
