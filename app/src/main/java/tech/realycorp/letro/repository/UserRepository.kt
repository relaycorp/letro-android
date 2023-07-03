package tech.realycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.realycorp.letro.data.UserDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    val _allUsersDataFlow: MutableStateFlow<List<UserDataModel>> = MutableStateFlow(emptyList())
    val allUsersDataFlow: StateFlow<List<UserDataModel>> get() = _allUsersDataFlow

    val _currentUserDataFlow: MutableStateFlow<UserDataModel?> = MutableStateFlow(null)
    val currentUserDataFlow: StateFlow<UserDataModel?> get() = _currentUserDataFlow

    init {
        // TODO: Data is currently mocked, replace with real data
        databaseScope.launch {
            _allUsersDataFlow.emit(
                listOf(
                    UserDataModel("user1"),
                    UserDataModel("user2"),
                ),
            )
        }
        databaseScope.launch {
            _currentUserDataFlow.emit(
                _allUsersDataFlow.value.firstOrNull(),
            )
        }
    }
}
