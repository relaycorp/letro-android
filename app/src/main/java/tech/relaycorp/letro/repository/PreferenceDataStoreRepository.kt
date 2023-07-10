package tech.relaycorp.letro.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PreferencesDataStoreRepository @Inject constructor(@ApplicationContext val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "letro_preferences")

    private val preferencesDataStore: DataStore<Preferences> = context.dataStore

    private val firstPartyEndpointKey = stringPreferencesKey("firstPartyEndpointNodeId")
    private val thirdPartyEndpointKey = stringPreferencesKey("thirdPartyEndpointNodeId")

    suspend fun saveFirstPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[firstPartyEndpointKey] = value
        }
    }

    fun getFirstPartyEndpoint(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[firstPartyEndpointKey]
        }
    }

    suspend fun saveThirdPartyEndpointNodeId(value: String) {
        preferencesDataStore.edit { preferences ->
            preferences[thirdPartyEndpointKey] = value
        }
    }

    fun getThirdPartyEndpoint(): Flow<String?> {
        return preferencesDataStore.data.map { preferences ->
            preferences[thirdPartyEndpointKey]
        }
    }
}
