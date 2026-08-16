package pt.ipt.easynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import pt.ipt.easynotes.data.NotesDatabase
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.ui.NotesScreen
import pt.ipt.easynotes.ui.NotesViewModel
import pt.ipt.easynotes.ui.NotesViewModelFactory
import pt.ipt.easynotes.ui.theme.EasyNotesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = NotesDatabase.getDatabase(this)

        val repository = NotesRepository(
            database.noteDao()
        )

        val viewModelFactory = NotesViewModelFactory(
            repository
        )

        val viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[NotesViewModel::class.java]

        setContent {
            EasyNotesTheme {
                NotesScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}