package pt.ipt.easynotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipt.easynotes.data.NotesRepository

/**
 * Factory que fornece o repositório necessário ao NotesViewModel.
 */
class NotesViewModelFactory(
    private val repository: NotesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
