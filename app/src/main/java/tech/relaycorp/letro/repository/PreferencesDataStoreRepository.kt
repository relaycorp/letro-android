package tech.relaycorp.letro.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesDataStoreRepository @Inject constructor(
    @ApplicationContext val context: Context,
) : IPreferencesDataStoreRepository {
    private val preferencesScope = CoroutineScope(Dispatchers.IO)

    private val Context.dataStore by preferencesDataStore(name = "letro_preferences")

    private val preferencesDataStore: DataStore<Preferences> = context.dataStore

    private val serverFirstPartyEndpointKey = stringPreferencesKey("serverFirstPartyEndpointId")
    private val serverThirdPartyEndpointKey = stringPreferencesKey("serverThirdPartyEndpointId")
    private val authorizedReceivingMessagesFromServerKey =
        booleanPreferencesKey("authorizedReceivingMessagesFromServer")

    private val _serverFirstPartyEndpointNodeId: MutableStateFlow<String?> = MutableStateFlow(null)
    override val serverFirstPartyEndpointNodeId: StateFlow<String?> get() = _serverFirstPartyEndpointNodeId

    private val _serverThirdPartyEndpointNodeId: MutableStateFlow<String?> = MutableStateFlow(null)
    override val serverThirdPartyEndpointNodeId: StateFlow<String?> get() = _serverThirdPartyEndpointNodeId

    private val _isGatewayAuthorizedToReceiveMessagesFromServer: MutableStateFlow<Boolean?> =
        MutableStateFlow(null)
    override val isGatewayAuthorizedToReceiveMessagesFromServer: StateFlow<Boolean?> get() = _isGatewayAuthorizedToReceiveMessagesFromServer

    init {
        preferencesScope.launch {
            getServerFirstPartyEndpointNodeId().collect {
                _serverFirstPartyEndpointNodeId.emit(it)
            }
        }

        preferencesScope.launch {
            getServerThirdPartyEndpointNodeId().collect {
                _serverThirdPartyEndpointNodeId.emit(it)
            }
        }

        preferencesScope.launch {
            getAuthorizedReceivingMessagesFromServer().collect {
                _isGatewayAuthorizedToReceiveMessagesFromServer.emit(it)
            }
        }
    }

    override suspend fun saveServerFirstPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[serverFirstPartyEndpointKey] = value
        }
    }

    private fun getServerFirstPartyEndpointNodeId(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[serverFirstPartyEndpointKey]
        }
    }

    override suspend fun saveServerThirdPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[serverThirdPartyEndpointKey] = value
        }
    }

    private fun getServerThirdPartyEndpointNodeId(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[serverThirdPartyEndpointKey]
        }
    }

    override suspend fun saveAuthorizedReceivingMessagesFromServer(value: Boolean) {
        preferencesDataStore.edit { preferences ->
            preferences[authorizedReceivingMessagesFromServerKey] = value
        }
    }

    private fun getAuthorizedReceivingMessagesFromServer(): Flow<Boolean?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[authorizedReceivingMessagesFromServerKey]
        }
    }

    override suspend fun clear() {
        preferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
