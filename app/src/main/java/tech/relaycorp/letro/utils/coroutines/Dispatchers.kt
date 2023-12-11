package tech.relaycorp.letro.utils.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Singleton
data class Dispatchers(
    val Main: CoroutineDispatcher,
    val IO: CoroutineDispatcher,
)
