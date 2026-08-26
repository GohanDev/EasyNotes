package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.data.NotesRepository
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
            val token = currentToken ?: return@launch

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
                photoPath = photoPath
            )

            repository.insertNote(note)
        }
    }

    fun updateNote(
        id: Int,
        title: String,
        content: String,
        photoPath: String? = null
    ) {
        viewModelScope.launch {

            val token = currentToken ?: return@launch
            val note = repository.getNoteById(id) ?: return@launch

            val remoteId = note.remoteId

            if (remoteId != null) {
                repository.updateRemoteNote(
                    token = token,
                    id = remoteId,
                    title = title,
                    content = content
                )
            }

            repository.updateNote(
                note.copy(
                    title = title,
                    content = content,
                    photoPath = photoPath
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {

            val token = currentToken ?: return@launch

            if (note.remoteId != null) {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )
            }

            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {

            val token = currentToken ?: return@launch
            val note = repository.getNoteById(id) ?: return@launch

            if (note.remoteId != null) {
                repository.deleteRemoteNote(
                    token = token,
                    id = note.remoteId
                )
            }

            repository.deleteNote(note)
        }
    }

    fun loadRemoteNotes(
        token: String,
        userId: Int
    ) {
        viewModelScope.launch {

            try {

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