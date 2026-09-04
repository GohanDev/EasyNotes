package pt.ipt.easynotes.data

import kotlinx.serialization.Serializable

/**
 * Estado de sincronização de uma nota entre o armazenamento local e a API.
 */
@Serializable
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

/**
 * Representa uma nota da aplicação.
 *
 * A classe é Serializable porque as notas são guardadas num ficheiro JSON
 * no Internal Storage da aplicação.
 */
@Serializable
data class Note(
    // Identificador local da nota.
    val id: Int = 0,

    // Identificador da mesma nota na API. É null enquanto não for sincronizada.
    val remoteId: Int? = null,

    // Identificador do utilizador a quem pertence a nota.
    val userId: Int,

    val title: String,
    val content: String,

    // Caminho local da fotografia associada à nota, quando existir.
    val photoPath: String? = null,

    // Indica se existe alguma operação pendente de sincronização.
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
