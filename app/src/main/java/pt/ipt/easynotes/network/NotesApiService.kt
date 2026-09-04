package pt.ipt.easynotes.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/** Dados enviados para criar ou atualizar uma nota na API. */
@Serializable
data class NoteApiRequest(
    val title: String,
    val content: String
)

/** Nota devolvida pela API REST. */
@Serializable
data class NoteApiResponse(
    val id: Int,
    val title: String,
    val content: String
)

/**
 * Serviço responsável pelos pedidos REST relacionados com as notas.
 */
object NotesApiService {

    suspend fun getNotes(
        token: String
    ): List<NoteApiResponse> {

        return ApiClient.client
            .get("${ApiClient.BASE_URL}/notes") {

                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )
            }
            .body()
    }

    suspend fun createNote(
        token: String,
        title: String,
        content: String
    ): NoteApiResponse {

        return ApiClient.client
            .post("${ApiClient.BASE_URL}/notes") {

                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )

                contentType(ContentType.Application.Json)

                setBody(
                    NoteApiRequest(
                        title = title,
                        content = content
                    )
                )
            }
            .body()
    }

    suspend fun updateNote(
        token: String,
        id: Int,
        title: String,
        content: String
    ): NoteApiResponse {

        return ApiClient.client
            .put("${ApiClient.BASE_URL}/notes/$id") {

                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )

                contentType(ContentType.Application.Json)

                setBody(
                    NoteApiRequest(
                        title = title,
                        content = content
                    )
                )
            }
            .body()
    }

    suspend fun deleteNote(
        token: String,
        id: Int
    ) {

        ApiClient.client
            .delete("${ApiClient.BASE_URL}/notes/$id") {

                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )
            }
    }
}