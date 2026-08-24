package pt.ipt.easynotes.network

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

object AuthService {

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {

        return ApiClient.client
            .post("${ApiClient.BASE_URL}/auth/login") {

                contentType(ContentType.Application.Json)

                setBody(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )
            }
            .body()
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): UserResponse {

        return ApiClient.client
            .post("${ApiClient.BASE_URL}/auth/register") {

                contentType(ContentType.Application.Json)

                setBody(
                    RegisterRequest(
                        name = name,
                        email = email,
                        password = password
                    )
                )
            }
            .body()
    }
}