package pt.ipt.easynotes.network

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String
)

object HealthService {

    suspend fun checkHealth(): HealthResponse {

        return ApiClient.client
            .get("${ApiClient.BASE_URL}/health")
            .body()
    }
}