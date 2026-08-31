package pt.ipt.easynotes.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipt.easynotes.data.NotesRepository

class NotesViewModelFactory(
    private val repository: NotesRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(
                repository = repository,
                context = context.applicationContext
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}