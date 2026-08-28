package pt.ipt.easynotes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ipt.easynotes.auth.BiometricAuthenticator
import pt.ipt.easynotes.data.AuthRepository
import pt.ipt.easynotes.data.NotesDatabase
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.data.SessionManager
import pt.ipt.easynotes.ui.AboutScreen
import pt.ipt.easynotes.ui.AuthViewModel
import pt.ipt.easynotes.ui.AuthViewModelFactory
import pt.ipt.easynotes.ui.LoginScreen
import pt.ipt.easynotes.ui.NoteEditorScreen
import pt.ipt.easynotes.ui.NotesScreen
import pt.ipt.easynotes.ui.NotesViewModel
import pt.ipt.easynotes.ui.NotesViewModelFactory
import pt.ipt.easynotes.ui.RegisterScreen
import pt.ipt.easynotes.ui.theme.EasyNotesTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = NotesDatabase.getDatabase(this)

        val notesRepository = NotesRepository(
            database.noteDao()
        )

        val notesViewModelFactory = NotesViewModelFactory(
            notesRepository
        )

        val notesViewModel = ViewModelProvider(
            this,
            notesViewModelFactory
        )[NotesViewModel::class.java]

        val sessionManager = SessionManager(
            applicationContext
        )

        val authRepository = AuthRepository(
            sessionManager
        )

        val authViewModelFactory = AuthViewModelFactory(
            authRepository
        )

        val authViewModel = ViewModelProvider(
            this,
            authViewModelFactory
        )[AuthViewModel::class.java]

        val biometricAuthenticator =
            BiometricAuthenticator(this)

        setContent {

            EasyNotesTheme {

                val context = LocalContext.current

                // Permissão de acesso à rede local
                LaunchedEffect(Unit) {

                    val hasPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_LOCAL_NETWORK
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_LOCAL_NETWORK
                            ),
                            100
                        )
                    }
                }

                val navController = rememberNavController()

                // Tenta recuperar a sessão guardada
                LaunchedEffect(Unit) {
                    authViewModel.restoreSession()
                }

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    // LOGIN
                    composable("login") {

                        val authState by authViewModel
                            .uiState
                            .collectAsStateWithLifecycle()

                        LaunchedEffect(
                            authState.token,
                            authState.restoredSession
                        ) {

                            if (authState.token != null) {

                                if (authState.restoredSession) {

                                    if (
                                        biometricAuthenticator
                                            .canAuthenticate()
                                    ) {

                                        biometricAuthenticator.authenticate(
                                            onSuccess = {

                                                navController.navigate(
                                                    "notes"
                                                ) {
                                                    popUpTo("login") {
                                                        inclusive = true
                                                    }
                                                }
                                            },
                                            onError = {
                                                // Fica no login
                                            }
                                        )
                                    }

                                } else {

                                    // Login normal com email/password
                                    navController.navigate("notes") {
                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }

                        LoginScreen(
                            isLoading = authState.isLoading,
                            errorMessage = authState.errorMessage,
                            onLogin = { email, password ->

                                authViewModel.login(
                                    email = email,
                                    password = password
                                )
                            },
                            onRegisterClick = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // LISTA DE NOTAS
                    composable("notes") {

                        val authState by authViewModel
                            .uiState
                            .collectAsStateWithLifecycle()

                        LaunchedEffect(
                            authState.token,
                            authState.user
                        ) {

                            val token = authState.token
                            val user = authState.user

                            if (
                                token != null &&
                                user != null
                            ) {

                                notesViewModel.setCurrentUser(
                                    userId = user.id,
                                    token = token
                                )

                                notesViewModel.loadRemoteNotes(
                                    token = token,
                                    userId = user.id
                                )
                            }
                        }

                        NotesScreen(
                            viewModel = notesViewModel,

                            onAddNote = {
                                navController.navigate("editor")
                            },

                            onNoteClick = { noteId ->
                                navController.navigate(
                                    "editor/$noteId"
                                )
                            },

                            onAboutClick = {
                                navController.navigate("about")
                            },

                            onLogoutClick = {

                                authViewModel.logout()

                                navController.navigate("login") {
                                    popUpTo("notes") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    // CRIAR NOTA
                    composable("editor") {

                        NoteEditorScreen(
                            viewModel = notesViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // EDITAR NOTA
                    composable(
                        "editor/{noteId}"
                    ) { backStackEntry ->

                        val noteId =
                            backStackEntry.arguments
                                ?.getString("noteId")
                                ?.toIntOrNull()

                        NoteEditorScreen(
                            viewModel = notesViewModel,
                            noteId = noteId,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // SOBRE
                    composable("about") {

                        AboutScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // REGISTO
                    composable("register") {

                        val authState by authViewModel
                            .uiState
                            .collectAsStateWithLifecycle()

                        LaunchedEffect(
                            authState.registrationSuccessful
                        ) {

                            if (
                                authState.registrationSuccessful
                            ) {

                                authViewModel
                                    .clearRegistrationSuccess()

                                navController.popBackStack()
                            }
                        }

                        RegisterScreen(
                            isLoading = authState.isLoading,
                            errorMessage = authState.errorMessage,

                            onRegister = {
                                    name,
                                    email,
                                    password ->

                                authViewModel.register(
                                    name = name,
                                    email = email,
                                    password = password
                                )
                            },

                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}