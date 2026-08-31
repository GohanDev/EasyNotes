package pt.ipt.easynotes.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.data.SyncScheduler
import pt.ipt.easynotes.data.SyncStatus
import pt.ipt.easynotes.network.NoteApiResponse

class NotesViewModel(
    private val repository: NotesRepository,
    private val context: Context
) : ViewModel() {

    private val _notes =
        MutableStateFlow<List<Note>>(emptyList())

    val notes: StateFlow<List<Note>> =
        _notes.asStateFlow()

    private val _remoteNotes =
        MutableStateFlow<List<NoteApiResponse>>(emptyList())

    val remoteNotes: StateFlow<List<NoteApiResponse>> =
        _remoteNotes.asStateFlow()

    private val _remoteError =
        MutableStateFlow<String?>(null)

    val remoteError: StateFlow<String?> =
        _remoteError.asStateFlow()

    private var currentUserId: Int? = null
    private var currentToken: String? = null

    fun setCurrentUser(
        userId: Int,
        token: String
    ) {
        currentUserId = userId
        currentToken = token

        viewModelScope.launch {
            repository
                .getNotesByUser(userId)
                .collect { userNotes ->
                    _notes.value = userNotes
                }
        }
    }

    fun addNote(
        title: String,
        content: String,
        photoPath: String? = null
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

                    val note = Note(
                        remoteId = remoteNote.id,
                        userId = userId,
                        title = title,
                        content = content,
                        photoPath = photoPath,
                        syncStatus = SyncStatus.SYNCED
                    )

                    repository.insertNote(note)

                    // A comunicação com a API teve sucesso.
                    // Remove uma eventual mensagem de erro antiga.
                    _remoteError.value = null

                    return@launch

                } catch (e: Exception) {
                    // API indisponível:
                    // continua e guarda localmente.
                }
            }

            val offlineNote = Note(
                remoteId = null,
                userId = userId,
                title = title,
                content = content,
                photoPath = photoPath,
                syncStatus = SyncStatus.PENDING_CREATE
            )

            repository.insertNote(offlineNote)

            SyncScheduler.scheduleSync(context)
        }
    }

    fun updateNote(
        id: Int,
        title: String,
        content: String,
        photoPath: String? = null
    ) {
        viewModelScope.launch {

            val token = currentToken

            val note = repository.getNoteById(id)
                ?: return@launch

            val newStatus =
                if (note.remoteId == null) {
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

            // Guarda primeiro no Room.
            repository.updateNote(updatedNote)

            if (token == null) {
                SyncScheduler.scheduleSync(context)
                return@launch
            }

            // Se ainda não existe na API,
            // fica como PENDING_CREATE.
            if (updatedNote.remoteId == null) {
                SyncScheduler.scheduleSync(context)
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
                    updatedNote.copy(
                        syncStatus = SyncStatus.SYNCED
                    )
                )

                // A API respondeu corretamente.
                _remoteError.value = null

            } catch (e: Exception) {

                // Fica PENDING_UPDATE no Room.
                SyncScheduler.scheduleSync(context)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {

            // A nota nunca chegou à API.
            if (note.remoteId == null) {
                repository.deleteNote(note)
                return@launch
            }

            val pendingDeleteNote = note.copy(
                syncStatus = SyncStatus.PENDING_DELETE
            )

            // Marca primeiro no Room.
            repository.updateNote(pendingDeleteNote)

            val token = currentToken

            if (token == null) {
                SyncScheduler.scheduleSync(context)
                return@launch
            }

            try {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )

                repository.deleteNote(pendingDeleteNote)

                // A API respondeu corretamente.
                _remoteError.value = null

            } catch (e: Exception) {

                // Fica PENDING_DELETE.
                SyncScheduler.scheduleSync(context)
            }
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {

            val note = repository.getNoteById(id)
                ?: return@launch

            // A nota nunca chegou à API.
            if (note.remoteId == null) {
                repository.deleteNote(note)
                return@launch
            }

            val pendingDeleteNote = note.copy(
                syncStatus = SyncStatus.PENDING_DELETE
            )

            // Marca primeiro no Room.
            repository.updateNote(pendingDeleteNote)

            val token = currentToken

            if (token == null) {
                SyncScheduler.scheduleSync(context)
                return@launch
            }

            try {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )

                repository.deleteNote(pendingDeleteNote)

                // A API respondeu corretamente.
                _remoteError.value = null

            } catch (e: Exception) {

                // Fica PENDING_DELETE.
                SyncScheduler.scheduleSync(context)
            }
        }
    }

    fun loadRemoteNotes(
        token: String,
        userId: Int
    ) {
        viewModelScope.launch {

            try {
                // Envia primeiro tudo o que ficou pendente.
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

                // Depois atualiza o Room a partir da API.
                repository.syncRemoteNotesToLocal(
                    token = token,
                    userId = userId
                )

                val result =
                    repository.getRemoteNotes(token)

                _remoteNotes.value = result

                // Sincronização concluída com sucesso.
                _remoteError.value = null

            } catch (e: Exception) {

                _remoteError.value =
                    "Não foi possível sincronizar com a API."

                // Agenda nova tentativa quando houver condições.
                SyncScheduler.scheduleSync(context)
            }
        }
    }
}