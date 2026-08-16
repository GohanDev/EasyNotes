package pt.ipt.easynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ipt.easynotes.data.NotesDatabase
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.ui.NoteEditorScreen
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

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "notes"
                ) {

                    composable("notes") {
                        NotesScreen(
                            viewModel = viewModel,
                            onAddNote = {
                                navController.navigate("editor")
                            }
                        )
                    }
                    composable("editor") {
                        NoteEditorScreen(
                            viewModel = viewModel,
                            onNoteSaved = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}