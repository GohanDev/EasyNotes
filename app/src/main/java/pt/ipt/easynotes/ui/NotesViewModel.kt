package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.data.SyncStatus
import pt.ipt.easynotes.network.NoteApiResponse

class NotesViewModel(
    private val repository: NotesRepository
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

                    // Há ligação à API:
                    // cria primeiro remotamente.
                    val remoteNote = repository.createRemoteNote(
                        token = token,
                        title = title,
                        content = content
                    )

                    // Depois guarda localmente já sincronizada.
                    val note = Note(
                        remoteId = remoteNote.id,
                        userId = userId,
                        title = title,
                        content = content,
                        photoPath = photoPath,
                        syncStatus = SyncStatus.SYNCED
                    )

                    repository.insertNote(note)

                    return@launch

                } catch (e: Exception) {
                    // Se não for possível contactar a API,
                    // continuamos abaixo e guardamos localmente.
                }
            }

            // Sem API: guarda no Room e marca como pendente.
            val offlineNote = Note(
                remoteId = null,
                userId = userId,
                title = title,
                content = content,
                photoPath = photoPath,
                syncStatus = SyncStatus.PENDING_CREATE
            )

            repository.insertNote(offlineNote)
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

            // Se a nota ainda nem existe na API,
            // continua como PENDING_CREATE.
            val newStatus =
                if (note.remoteId == null) {
                    SyncStatus.PENDING_CREATE
                } else {
                    SyncStatus.PENDING_UPDATE
                }

            // Guarda PRIMEIRO no Room.
            // Assim funciona mesmo sem Internet.
            val updatedNote = note.copy(
                title = title,
                content = content,
                photoPath = photoPath,
                syncStatus = newStatus
            )

            repository.updateNote(updatedNote)

            // Se não existir token, fica pendente.
            if (token == null) {
                return@launch
            }

            // Se já existe na API, tenta sincronizar.
            if (updatedNote.remoteId != null) {

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

                } catch (e: Exception) {
                    // Sem Internet:
                    // fica PENDING_UPDATE no Room.
                }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {

            // Se a nota nunca foi enviada para a API,
            // apaga apenas do Room.
            if (note.remoteId == null) {
                repository.deleteNote(note)
                return@launch
            }

            // Se existe na API, marcamos primeiro como pendente.
            val pendingDeleteNote = note.copy(
                syncStatus = SyncStatus.PENDING_DELETE
            )

            repository.updateNote(pendingDeleteNote)

            val token = currentToken ?: return@launch

            try {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )

                // Só removemos definitivamente do Room
                // se a API tiver apagado com sucesso.
                repository.deleteNote(pendingDeleteNote)

            } catch (e: Exception) {
                // Sem Internet / API desligada:
                // fica PENDING_DELETE e não crasha.
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

            // Nota criada offline e que nunca chegou à API:
            // pode ser apagada definitivamente do Room.
            if (note.remoteId == null) {
                repository.deleteNote(note)
                return@launch
            }

            // A nota existe na API.
            // Marcamos primeiro como pendente de eliminação.
            val pendingDeleteNote = note.copy(
                syncStatus = SyncStatus.PENDING_DELETE
            )

            repository.updateNote(pendingDeleteNote)

            val token = currentToken ?: return@launch

            try {

                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )

                // Se conseguiu apagar na API,
                // apagamos definitivamente do Room.
                repository.deleteNote(pendingDeleteNote)

            } catch (e: Exception) {

                // API desligada / sem Internet.
                // A nota continua no Room como PENDING_DELETE.
                // Como o DAO a esconde da lista,
                // para o utilizador ela já aparece como apagada.
            }
        }
    }

    fun loadRemoteNotes(
        token: String,
        userId: Int
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

                val result = repository.getRemoteNotes(token)

                _remoteNotes.value = result
                _remoteError.value = null

            } catch (e: Exception) {

                _remoteError.value =
                    "Não foi possível carregar as notas da API."
            }
        }
    }
}