package tech.relaycorp.letro.utils.compose

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider

fun SnackbarHostState.showSnackbar(scope: CoroutineScope, message: String) {
    scope.launch {
        showSnackbar(
            message = message,
        )
    }
}

fun SnackbarHostState.showSnackbar(scope: CoroutineScope, snackbarStringsProvider: SnackbarStringsProvider, message: SnackbarString) {
    scope.launch {
        showSnackbar(
            message = snackbarStringsProvider.get(message),
        )
    }
}
