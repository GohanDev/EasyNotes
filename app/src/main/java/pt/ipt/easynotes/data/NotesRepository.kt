package pt.ipt.easynotes.data

import pt.ipt.easynotes.network.NotesApiService

/**
 * Repositório responsável pelo acesso às notas.
 *
 * Centraliza as operações realizadas no Internal Storage e na API REST,
 * incluindo a sincronização entre os dois locais.
 */
class NotesRepository(
    private val localStorage: NotesLocalStorage
) {

    // Obtém do armazenamento local as notas de um utilizador.
    fun getNotesByUser(userId: Int): List<Note> {
        return localStorage.getNotesByUser(userId)
    }

    // Obtém uma nota local através do seu identificador.
    fun getNoteById(id: Int): Note? {
        return localStorage.getNoteById(id)
    }

    // Insere uma nota no Internal Storage.
    fun insertNote(note: Note) {
        localStorage.insertNote(note)
    }

    // Atualiza uma nota existente no Internal Storage.
    fun updateNote(note: Note) {
        localStorage.updateNote(note)
    }

    // Remove uma nota do Internal Storage.
    fun deleteNote(note: Note) {
        localStorage.deleteNote(note)
    }

    // Obtém através da API as notas do utilizador autenticado.
    suspend fun getRemoteNotes(token: String) =
        NotesApiService.getNotes(token)

    // Cria uma nota através da API REST.
    suspend fun createRemoteNote(
        token: String,
        title: String,
        content: String
    ) = NotesApiService.createNote(
        token = token,
        title = title,
        content = content
    )

    // Atualiza uma nota existente através da API REST.
    suspend fun updateRemoteNote(
        token: String,
        id: Int,
        title: String,
        content: String
    ) = NotesApiService.updateNote(
        token = token,
        id = id,
        title = title,
        content = content
    )

    // Remove uma nota através da API REST.
    suspend fun deleteRemoteNote(
        token: String,
        id: Int
    ) {
        NotesApiService.deleteNote(
            token = token,
            id = id
        )
    }

    /**
     * Atualiza o Internal Storage com o estado atual das notas existentes na API.
     */
    suspend fun syncRemoteNotesToLocal(
        token: String,
        userId: Int
    ) {
        val remoteNotes = getRemoteNotes(token)
        val remoteIds = remoteNotes.map { remoteNote -> remoteNote.id }

        if (remoteIds.isEmpty()) {
            // A API já não possui notas sincronizadas para este utilizador.
            localStorage.deleteAllRemoteNotesForUser(userId)
        } else {
            // Remove localmente notas sincronizadas que deixaram de existir na API.
            localStorage.deleteMissingRemoteNotes(
                userId = userId,
                remoteIds = remoteIds
            )
        }

        remoteNotes.forEach { remoteNote ->
            val localNote = localStorage.getNoteByRemoteId(
                remoteId = remoteNote.id,
                userId = userId
            )

            if (localNote == null) {
                // A nota ainda não existe localmente e é criada no dispositivo.
                localStorage.insertNote(
                    Note(
                        remoteId = remoteNote.id,
                        userId = userId,
                        title = remoteNote.title,
                        content = remoteNote.content,
                        photoPath = null
                    )
                )
            } else {
                // A nota já existe e recebe os dados mais recentes da API.
                localStorage.updateNote(
                    localNote.copy(
                        title = remoteNote.title,
                        content = remoteNote.content,
                        syncStatus = SyncStatus.SYNCED
                    )
                )
            }
        }
    }

    /**
     * Envia para a API as notas criadas enquanto não existia ligação ao servidor.
     */
    suspend fun syncPendingCreates(
        token: String,
        userId: Int
    ) {
        val pendingNotes = localStorage.getNotesBySyncStatus(
            userId = userId,
            status = SyncStatus.PENDING_CREATE
        )

        pendingNotes.forEach { note ->
            val remoteNote = createRemoteNote(
                token = token,
                title = note.title,
                content = note.content
            )

            // Guarda o identificador atribuído pela API e marca a nota como sincronizada.
            localStorage.updateNote(
                note.copy(
                    remoteId = remoteNote.id,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }

    /**
     * Envia para a API as alterações locais que ficaram pendentes.
     */
    suspend fun syncPendingUpdates(
        token: String,
        userId: Int
    ) {
        val pendingNotes = localStorage.getNotesBySyncStatus(
            userId = userId,
            status = SyncStatus.PENDING_UPDATE
        )

        pendingNotes.forEach { note ->
            val remoteId = note.remoteId ?: return@forEach

            updateRemoteNote(
                token = token,
                id = remoteId,
                title = note.title,
                content = note.content
            )

            // A API confirmou a atualização.
            localStorage.updateNote(
                note.copy(syncStatus = SyncStatus.SYNCED)
            )
        }
    }

    /**
     * Envia para a API as eliminações que ficaram pendentes.
     */
    suspend fun syncPendingDeletes(
        token: String,
        userId: Int
    ) {
        val pendingNotes = localStorage.getNotesBySyncStatus(
            userId = userId,
            status = SyncStatus.PENDING_DELETE
        )

        pendingNotes.forEach { note ->
            val remoteId = note.remoteId

            if (remoteId == null) {
                // Nunca chegou à API, por isso basta removê-la localmente.
                localStorage.deleteNote(note)
                return@forEach
            }

            deleteRemoteNote(
                token = token,
                id = remoteId
            )

            // Só é removida localmente depois da confirmação da API.
            localStorage.deleteNote(note)
        }
    }
}
