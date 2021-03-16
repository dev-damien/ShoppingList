package de.codingkeks.shoppinglist.utility

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "overall_settings")

class DataStoreUtility {
    companion object {
        suspend fun saveInt(key: String, value: Int, context: Context) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.edit { settings ->
                settings[dataStoreKey] = value
            }
        }

        suspend fun saveString(key: String, value: String, context: Context) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.edit { settings ->
                settings[dataStoreKey] = value
            }
        }

        suspend fun readInt(key: String, context: Context): Int {
            val dataStoreKey = intPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[dataStoreKey] ?: 0
        }

        suspend fun readString(key: String, context: Context): String {
            val dataStoreKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[dataStoreKey] ?: ""
        }
    }
}