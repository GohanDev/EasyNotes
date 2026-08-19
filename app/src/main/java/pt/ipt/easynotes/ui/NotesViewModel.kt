package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.data.NotesRepository

class NotesViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    val notes: StateFlow<List<Note>> =
        repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addNote(
        title: String,
        content: String,
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            val note = Note(
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

            val note = repository.getNoteById(id)

            if (note != null) {
                repository.updateNote(
                    note.copy(
                        title = title,
                        content = content,
                        photoPath = photoPath
                    )
                )
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {

            val note = repository.getNoteById(id)

            if (note != null) {
                repository.deleteNote(note)
            }
        }
    }

}