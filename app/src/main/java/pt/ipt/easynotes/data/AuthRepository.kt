package pt.ipt.easynotes.data

import pt.ipt.easynotes.network.AuthService
import pt.ipt.easynotes.network.LoginResponse
import pt.ipt.easynotes.network.UserResponse

/**
 * Centraliza as operações de autenticação e a gestão da sessão local.
 */
class AuthRepository(
    private val sessionManager: SessionManager
) {

    /**
     * Autentica o utilizador na API e guarda localmente a sessão recebida.
     */
    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        val response = AuthService.login(
            email = email,
            password = password
        )

        sessionManager.saveSession(
            token = response.token,
            userId = response.user.id,
            name = response.user.name,
            email = response.user.email
        )

        return response
    }

    // Envia para a API os dados necessários para criar uma conta.
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): UserResponse {
        return AuthService.register(
            name = name,
            email = email,
            password = password
        )
    }

    // Recupera a sessão guardada em SharedPreferences.
    fun getSession(): UserSession? {
        return sessionManager.getSession()
    }

    // Limpa a sessão guardada no dispositivo.
    fun logout() {
        sessionManager.clearSession()
    }

    // Confirma junto da API que o token JWT continua válido.
    suspend fun validateSession(token: String): UserResponse {
        return AuthService.getCurrentUser(token)
    }
}
