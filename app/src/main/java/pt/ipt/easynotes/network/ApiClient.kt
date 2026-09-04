package pt.ipt.easynotes.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Configura o cliente HTTP Ktor utilizado para comunicar com a API REST.
 */
object ApiClient {

    val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    const val BASE_URL = "https://easynotesapi.deployzy.app"
}