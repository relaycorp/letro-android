package tech.relaycorp.letro.utils.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> MutableSharedFlow<T>.emitOn(
    value: T,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) {
    scope.launch(coroutineContext) { emit(value) }
}

fun <T> MutableStateFlow<T>.emitOn(
    value: T,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) {
    scope.launch(coroutineContext) { emit(value) }
}

fun <T> MutableStateFlow<T>.emitOnDelayed(
    value: T,
    scope: CoroutineScope,
    delay: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) {
    scope.launch(coroutineContext) {
        delay(delay)
        emit(value)
    }
}

fun <T> Channel<T>.sendOn(
    value: T,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) {
    scope.launch(coroutineContext) { send(value) }
}
