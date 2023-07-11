package tech.relaycorp.letro.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PreferencesDataStoreRepository @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val Context.dataStore by preferencesDataStore(name = "letro_preferences")

    private val preferencesDataStore: DataStore<Preferences> = context.dataStore

    private val serverFirstPartyEndpointKey = stringPreferencesKey("serverFirstPartyEndpointId")
    private val serverThirdPartyEndpointKey = stringPreferencesKey("serverThirdPartyEndpointId")
    private val authorizedReceivingMessagesFromServer =
        booleanPreferencesKey("authorizedReceivingMessagesFromServer")

    suspend fun saveServerFirstPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[serverFirstPartyEndpointKey] = value
        }
    }

    fun getServerFirstPartyEndpointNodeId(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[serverFirstPartyEndpointKey]
        }
    }

    suspend fun saveServerThirdPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[serverThirdPartyEndpointKey] = value
        }
    }

    fun getServerThirdPartyEndpointNodeId(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[serverThirdPartyEndpointKey]
        }
    }

    suspend fun saveAuthorizedReceivingMessagesFromServer(value: Boolean) {
        preferencesDataStore.edit { preferences ->
            preferences[authorizedReceivingMessagesFromServer] = value
        }
    }

    fun getAuthorizedReceivingMessagesFromServer(): Flow<Boolean?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[authorizedReceivingMessagesFromServer]
        }
    }
}
