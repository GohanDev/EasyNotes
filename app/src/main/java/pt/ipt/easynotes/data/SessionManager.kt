package pt.ipt.easynotes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "session"
)

data class UserSession(
    val token: String,
    val userId: Int,
    val name: String,
    val email: String
)

class SessionManager(
    private val context: Context
) {

    companion object {
        private val TOKEN =
            stringPreferencesKey("token")

        private val USER_ID =
            intPreferencesKey("user_id")

        private val NAME =
            stringPreferencesKey("name")

        private val EMAIL =
            stringPreferencesKey("email")
    }

    val session: Flow<UserSession?> =
        context.dataStore.data.map { preferences ->

            val token = preferences[TOKEN]
            val userId = preferences[USER_ID]
            val name = preferences[NAME]
            val email = preferences[EMAIL]

            if (
                token != null &&
                userId != null &&
                name != null &&
                email != null
            ) {
                UserSession(
                    token = token,
                    userId = userId,
                    name = name,
                    email = email
                )
            } else {
                null
            }
        }

    suspend fun saveSession(
        token: String,
        userId: Int,
        name: String,
        email: String
    ) {
        context.dataStore.edit { preferences ->

            preferences[TOKEN] = token
            preferences[USER_ID] = userId
            preferences[NAME] = name
            preferences[EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}