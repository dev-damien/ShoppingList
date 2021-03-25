package de.codingkeks.shoppinglist.utility

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreUtility {
    companion object {
        /**
         * call to save an Int-value
         * @param key the key under which the data should be stored
         * @param value the Int-value you want to store
         * @param context the context
         */
        suspend fun saveInt(key: String, value: Int, context: Context) {
            val dataStoreKey = intPreferencesKey(key)
            context.dataStore.edit { settings ->
                settings[dataStoreKey] = value
            }
        }

        /**
         * method to save a String-value
         * @param key the key under which the data should be stored
         * @param value the String-value you want to store
         * @param context the context
         */
        suspend fun saveString(key: String, value: String, context: Context) {
            val dataStoreKey = stringPreferencesKey(key)
            context.dataStore.edit { settings ->
                settings[dataStoreKey] = value
            }
        }

        /**
         * method to get an Int-value from DataStore
         * @param key the key under which the desired value is stored
         * @param context the context
         * @return returns the Int-value saved under the key-string or 0 if key does not exist
         */
        suspend fun readInt(key: String, context: Context): Int {
            val dataStoreKey = intPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[dataStoreKey] ?: 0
        }

        /**
         * method to get an String-value from DataStore
         * @param key the key under which the desired value is stored
         * @param context the context
         * @return returns the String-value saved under the key-string or an empty String if key does not exist
         */
        suspend fun readString(key: String, context: Context): String {
            val dataStoreKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            return preferences[dataStoreKey] ?: ""
        }
    }
}