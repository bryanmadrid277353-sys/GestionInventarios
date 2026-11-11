package com.example.gestioninventarios.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class TokenStore(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val LEGACY_LAST_LOGIN_EMAIL_KEY = stringPreferencesKey("last_login_email")
    }

    val userRole: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_ROLE_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_NAME_KEY] }
    val userEmail: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_EMAIL_KEY] }
    val userId: Flow<Int?> = context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] == true
    }

    suspend fun saveSession(userId: Int, nombre: String, email: String, rol: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_KEY] = true
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = nombre
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_ROLE_KEY] = rol
            prefs.remove(LEGACY_LAST_LOGIN_EMAIL_KEY)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun cleanupLegacyData() {
        context.dataStore.edit { prefs ->
            prefs.remove(LEGACY_LAST_LOGIN_EMAIL_KEY)
        }
    }
}
