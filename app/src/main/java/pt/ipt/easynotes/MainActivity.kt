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
import pt.ipt.easynotes.data.NotesLocalStorage
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

/**
 * Activity principal da aplicação.
 *
 * É responsável por criar os ViewModels, pedir a permissão da câmara e trocar
 * os Fragments apresentados no contentor principal.
 */
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

        // O login é o ecrã inicial e contém também o acesso ao ecrã Sobre.
        if (savedInstanceState == null) {
            showLogin()
        }
    }

    /**
     * Cria os ViewModels e fornece as respetivas dependências.
     */
    private fun createViewModels() {
        val localStorage = NotesLocalStorage.getInstance(applicationContext)
        val notesRepository = NotesRepository(localStorage)

        val notesFactory = NotesViewModelFactory(
            repository = notesRepository
        )

        notesViewModel = ViewModelProvider(
            this,
            notesFactory
        )[NotesViewModel::class.java]

        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(sessionManager)
        val authFactory = AuthViewModelFactory(authRepository)

        authViewModel = ViewModelProvider(
            this,
            authFactory
        )[AuthViewModel::class.java]
    }

    /**
     * Pede a permissão de câmara necessária para associar fotografias às notas.
     */
    private fun requestRequiredPermissions() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Apresenta o ecrã inicial de autenticação.
    fun showLogin() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()
    }

    // Apresenta o ecrã de criação de conta.
    fun showRegister() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    /**
     * Apresenta a lista de notas e limpa o histórico dos ecrãs de autenticação.
     */
    fun showNotes() {
        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, NotesFragment())
            .commit()
    }

    /**
     * Abre o editor. Um noteId nulo indica criação; caso contrário é edição.
     */
    fun showEditor(noteId: Int? = null) {
        val fragment = NoteEditorFragment.newInstance(noteId)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Apresenta a informação académica e técnica exigida pelo enunciado.
    fun showAbout() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, AboutFragment())
            .commit()
    }

    /**
     * Regressa ao Fragment anterior; sem histórico, volta ao login.
     */
    fun goBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            showLogin()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
