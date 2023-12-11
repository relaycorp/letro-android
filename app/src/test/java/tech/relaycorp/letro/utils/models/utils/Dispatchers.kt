package tech.relaycorp.letro.utils.models.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.utils.coroutines.Dispatchers

@OptIn(ExperimentalCoroutinesApi::class)
fun dispatchers(
    Main: CoroutineDispatcher = UnconfinedTestDispatcher(),
    IO: CoroutineDispatcher = UnconfinedTestDispatcher(),
) = Dispatchers(
    Main = Main,
    IO = IO,
)
