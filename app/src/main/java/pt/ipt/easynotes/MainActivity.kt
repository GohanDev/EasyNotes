package pt.ipt.easynotes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import pt.ipt.easynotes.auth.BiometricAuthenticator
import pt.ipt.easynotes.data.AuthRepository
import pt.ipt.easynotes.data.NotesDatabase
import pt.ipt.easynotes.data.NotesRepository
import pt.ipt.easynotes.data.SessionManager
import pt.ipt.easynotes.databinding.ActivityMainBinding
import pt.ipt.easynotes.ui.AuthViewModel
import pt.ipt.easynotes.ui.AuthViewModelFactory
import pt.ipt.easynotes.ui.NotesViewModel
import pt.ipt.easynotes.ui.NotesViewModelFactory
import pt.ipt.easynotes.ui.fragments.AboutFragment
import pt.ipt.easynotes.ui.fragments.LoginFragment
import pt.ipt.easynotes.ui.fragments.NoteEditorFragment
import pt.ipt.easynotes.ui.fragments.NotesFragment
import pt.ipt.easynotes.ui.fragments.RegisterFragment

class MainActivity : FragmentActivity() {

    lateinit var authViewModel: AuthViewModel
        private set

    lateinit var notesViewModel: NotesViewModel
        private set

    lateinit var biometricAuthenticator: BiometricAuthenticator
        private set

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createViewModels()

        biometricAuthenticator = BiometricAuthenticator(this)

        requestRequiredPermissions()

        if (savedInstanceState == null) {
            showLogin()
        }

        if (authViewModel.uiState.value.token == null) {
            authViewModel.restoreSession()
        }
    }

    private fun createViewModels() {

        val database = NotesDatabase.getDatabase(this)

        val notesRepository = NotesRepository(
            database.noteDao()
        )

        val notesFactory = NotesViewModelFactory(
            repository = notesRepository,
            context = applicationContext
        )

        notesViewModel = ViewModelProvider(
            this,
            notesFactory
        )[NotesViewModel::class.java]

        val sessionManager = SessionManager(
            applicationContext
        )

        val authRepository = AuthRepository(
            sessionManager
        )

        val authFactory = AuthViewModelFactory(
            authRepository
        )

        authViewModel = ViewModelProvider(
            this,
            authFactory
        )[AuthViewModel::class.java]
    }

    private fun requestRequiredPermissions() {

        val permissions = mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(
                Manifest.permission.CAMERA
            )
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(
                permissions.toTypedArray(),
                100
            )
        }
    }

    fun showLogin() {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                LoginFragment()
            )
            .commit()
    }

    fun showRegister() {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                RegisterFragment()
            )
            .addToBackStack(null)
            .commit()
    }

    fun showNotes() {

        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                NotesFragment()
            )
            .commit()
    }

    fun showEditor(noteId: Int? = null) {

        val fragment =
            NoteEditorFragment.newInstance(noteId)

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .addToBackStack(null)
            .commit()
    }

    fun showAbout() {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                AboutFragment()
            )
            .commit()
    }

    fun goBack() {

        if (supportFragmentManager.backStackEntryCount > 0) {

            supportFragmentManager.popBackStack()

        } else {

            showLogin()
        }
    }
}