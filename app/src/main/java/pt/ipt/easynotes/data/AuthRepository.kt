package pt.ipt.easynotes.data

import pt.ipt.easynotes.network.AuthService
import pt.ipt.easynotes.network.LoginResponse
import pt.ipt.easynotes.network.UserResponse

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        return AuthService.login(
            email = email,
            password = password
        )
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
}