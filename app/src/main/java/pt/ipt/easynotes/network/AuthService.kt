package pt.ipt.easynotes.network

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

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

    suspend fun getCurrentUser(
        token: String
    ): UserResponse {

        val response = ApiClient.client
            .get("${ApiClient.BASE_URL}/me") {

                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )
            }

        return when (response.status) {

            HttpStatusCode.OK -> {
                response.body()
            }

            HttpStatusCode.Unauthorized -> {
                throw InvalidSessionException()
            }

            else -> {
                throw ApiException(
                    "Erro da API: ${response.status}"
                )
            }
        }
    }
}

class InvalidSessionException : Exception()

class ApiException(
    message: String
) : Exception(message)