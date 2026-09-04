package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.data.SyncStatus

/**
 * ViewModel responsável pelas operações sobre as notas.
 *
 * Mantém a identificação do utilizador autenticado, coordena o acesso ao
 * Internal Storage e comunica com a API REST através do repositório.
 */
class NotesViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private var currentUserId: Int? = null
    private var currentToken: String? = null

    /**
     * Define o utilizador cujas notas devem ser utilizadas nesta sessão.
     */
    fun setCurrentUser(
        userId: Int,
        token: String
    ) {
        currentUserId = userId
        currentToken = token
    }

    /**
     * Devolve diretamente do Internal Storage as notas do utilizador atual.
     */
    fun getCurrentNotes(): List<Note> {
        val userId = currentUserId ?: return emptyList()
        return repository.getNotesByUser(userId)
    }

    // Obtém uma nota local através do seu identificador.
    fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    /**
     * Cria uma nota.
     *
     * Se a API estiver disponível, a nota fica imediatamente sincronizada.
     * Se a comunicação falhar, fica guardada no Internal Storage como
     * PENDING_CREATE. A aplicação tentará sincronizá-la quando o utilizador
     * voltar ao ecrã das notas com ligação à Internet.
     */
    fun addNote(
        title: String,
        content: String,
        photoPath: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            val token = currentToken

            if (token != null) {
                try {
                    val remoteNote = repository.createRemoteNote(
                        token = token,
                        title = title,
                        content = content
                    )

                    repository.insertNote(
                        Note(
                            remoteId = remoteNote.id,
                            userId = userId,
                            title = title,
                            content = content,
                            photoPath = photoPath,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )

                    onComplete()
                    return@launch
                } catch (e: Exception) {
                    // Se a API estiver indisponível, continua em modo offline.
                }
            }

            repository.insertNote(
                Note(
                    remoteId = null,
                    userId = userId,
                    title = title,
                    content = content,
                    photoPath = photoPath,
                    syncStatus = SyncStatus.PENDING_CREATE
                )
            )

            onComplete()
        }
    }

    /**
     * Atualiza primeiro a nota no Internal Storage para que a alteração não se
     * perca quando não existe ligação à Internet.
     */
    fun updateNote(
        id: Int,
        title: String,
        content: String,
        photoPath: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val note = repository.getNoteById(id) ?: return@launch

            val newStatus = if (note.remoteId == null) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }

            val updatedNote = note.copy(
                title = title,
                content = content,
                photoPath = photoPath,
                syncStatus = newStatus
            )

            // A alteração local fica concluída antes de regressar à lista.
            repository.updateNote(updatedNote)
            onComplete()

            val token = currentToken

            // Se ainda não existir no servidor, será criada na próxima sincronização.
            if (token == null || updatedNote.remoteId == null) {
                return@launch
            }

            try {
                repository.updateRemoteNote(
                    token = token,
                    id = updatedNote.remoteId,
                    title = title,
                    content = content
                )

                repository.updateNote(
                    updatedNote.copy(syncStatus = SyncStatus.SYNCED)
                )
            } catch (e: Exception) {
                // Mantém PENDING_UPDATE para a próxima sincronização.
            }
        }
    }

    /**
     * Elimina uma nota através do identificador local.
     *
     * Se a nota já existir na API, fica marcada como PENDING_DELETE e desaparece
     * da lista. A eliminação no servidor será tentada imediatamente e, em caso
     * de falha, novamente quando o utilizador regressar ao ecrã das notas.
     */
    fun deleteNoteById(
        id: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val note = repository.getNoteById(id) ?: return@launch

            if (note.remoteId == null) {
                // Nunca foi criada na API, portanto basta removê-la localmente.
                repository.deleteNote(note)
                onComplete()
                return@launch
            }

            val pendingDeleteNote = note.copy(
                syncStatus = SyncStatus.PENDING_DELETE
            )

            repository.updateNote(pendingDeleteNote)
            onComplete()

            val token = currentToken ?: return@launch

            try {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )

                // Só desaparece definitivamente do ficheiro após confirmação da API.
                repository.deleteNote(pendingDeleteNote)
            } catch (e: Exception) {
                // Mantém PENDING_DELETE para a próxima sincronização.
            }
        }
    }

    /**
     * Sincroniza as operações pendentes e atualiza o Internal Storage com o
     * estado atual das notas existentes na API.
     *
     * Esta função é chamada quando o utilizador entra ou regressa ao ecrã das
     * notas. O callback recebe null em caso de sucesso ou uma mensagem de erro.
     */
    fun loadRemoteNotes(
        token: String,
        userId: Int,
        onComplete: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.syncPendingCreates(
                    token = token,
                    userId = userId
                )

                repository.syncPendingUpdates(
                    token = token,
                    userId = userId
                )

                repository.syncPendingDeletes(
                    token = token,
                    userId = userId
                )

                repository.syncRemoteNotesToLocal(
                    token = token,
                    userId = userId
                )

                onComplete(null)
            } catch (e: Exception) {
                onComplete("Não foi possível sincronizar com a API.")
            }
        }
    }
}
