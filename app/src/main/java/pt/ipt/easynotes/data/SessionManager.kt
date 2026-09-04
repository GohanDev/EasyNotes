package pt.ipt.easynotes.data

import android.content.Context

/**
 * Dados mínimos necessários para recuperar uma sessão iniciada anteriormente.
 */
data class UserSession(
    val token: String,
    val userId: Int,
    val name: String,
    val email: String
)

/**
 * Guarda e recupera a sessão do utilizador com SharedPreferences.
 *
 * SharedPreferences é adequado neste caso porque a sessão é composta por
 * poucos valores simples (token, identificador, nome e email).
 */
class SessionManager(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Guarda os dados da sessão no armazenamento privado da aplicação.
     */
    fun saveSession(
        token: String,
        userId: Int,
        name: String,
        email: String
    ) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .commit()
    }

    /**
     * Recupera a sessão guardada. Se faltar algum valor obrigatório,
     * considera-se que não existe uma sessão válida no dispositivo.
     */
    fun getSession(): UserSession? {
        val token = preferences.getString(KEY_TOKEN, null)
        val userId = preferences.getInt(KEY_USER_ID, -1)
        val name = preferences.getString(KEY_NAME, null)
        val email = preferences.getString(KEY_EMAIL, null)

        if (
            token == null ||
            userId == -1 ||
            name == null ||
            email == null
        ) {
            return null
        }

        return UserSession(
            token = token,
            userId = userId,
            name = name,
            email = email
        )
    }

    /**
     * Remove todos os dados da sessão quando o utilizador termina sessão.
     */
    fun clearSession() {
        preferences.edit()
            .clear()
            .commit()
    }

    companion object {
        private const val PREFERENCES_NAME = "session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
    }
}
