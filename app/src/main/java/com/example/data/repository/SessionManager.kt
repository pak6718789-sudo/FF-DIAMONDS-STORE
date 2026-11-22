package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SessionStore {
    val currentUserIdFlow: Flow<String?>
    suspend fun setCurrentUserId(userId: String)
    suspend fun clearSession()
}

private val Context.sessionDataStore by preferencesDataStore(name = "ff_diamond_store_session")

class SessionManager(private val context: Context) : SessionStore {
    private val currentUserIdKey = stringPreferencesKey("current_user_id")

    override val currentUserIdFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[currentUserIdKey]?.takeIf { it.isNotBlank() }
    }

    override suspend fun setCurrentUserId(userId: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[currentUserIdKey] = userId
        }
    }

    override suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(currentUserIdKey)
        }
    }
}
