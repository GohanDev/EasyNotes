package pt.ipt.easynotes.data

import pt.ipt.easynotes.network.AuthService
import pt.ipt.easynotes.network.LoginResponse
import pt.ipt.easynotes.network.UserResponse

class AuthRepository(
    private val sessionManager: SessionManager
) {
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

    fun getSession() = sessionManager.session

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun validateSession(
        token: String
    ): UserResponse {
        return AuthService.getCurrentUser(token)
    }
}