package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.databinding.FragmentNotesBinding
import pt.ipt.easynotes.ui.adapters.NoteAdapter

/**
 * Fragment principal que apresenta as notas do utilizador autenticado.
 *
 * As notas locais são mostradas imediatamente. Quando o Fragment fica visível,
 * a aplicação tenta sincronizar as operações pendentes com a API e atualiza a
 * lista novamente no fim da sincronização.
 */
class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var activity: MainActivity
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        adapter = NoteAdapter(activity)
        binding.recyclerNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotes.adapter = adapter

        binding.buttonAdd.setOnClickListener {
            activity.showEditor()
        }

        binding.emptyState.setOnClickListener {
            activity.showEditor()
        }

        binding.buttonAbout.setOnClickListener {
            activity.showAbout()
        }

        binding.buttonLogout.setOnClickListener {
            activity.authViewModel.logout()
            activity.showLogin()
        }

        configureCurrentUser()
    }

    /**
     * Sempre que o utilizador regressa a este Fragment, apresenta primeiro as
     * notas locais e tenta depois sincronizá-las com a API.
     */
    override fun onResume() {
        super.onResume()

        if (::activity.isInitialized && ::adapter.isInitialized) {
            refreshNotes()
            synchronizeNotes()
        }
    }

    /**
     * Obtém a sessão atual e configura o ViewModel para o utilizador autenticado.
     */
    private fun configureCurrentUser() {
        val state = activity.authViewModel.uiState
        val token = state.token
        val user = state.user

        if (token == null || user == null) {
            activity.showLogin()
            return
        }

        activity.notesViewModel.setCurrentUser(
            userId = user.id,
            token = token
        )

        // Mostra imediatamente o conteúdo que já se encontra no dispositivo.
        refreshNotes()
    }

    /**
     * Tenta enviar as operações pendentes e obter o estado atual da API.
     * Se não existir Internet, as notas locais continuam disponíveis.
     */
    private fun synchronizeNotes() {
        val state = activity.authViewModel.uiState
        val token = state.token
        val user = state.user

        if (token == null || user == null) {
            return
        }

        activity.notesViewModel.loadRemoteNotes(
            token = token,
            userId = user.id
        ) { errorMessage ->
            refreshNotes()
            showSyncError(errorMessage)
        }
    }

    /**
     * Lê as notas atuais do Internal Storage e atualiza o RecyclerView.
     */
    private fun refreshNotes() {
        val notes = activity.notesViewModel.getCurrentNotes()
        adapter.setNotes(notes)

        binding.textNoteCount.text = if (notes.size == 1) {
            "1 nota"
        } else {
            "${notes.size} notas"
        }

        if (notes.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerNotes.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerNotes.visibility = View.VISIBLE
        }
    }

    // Apresenta ou esconde a mensagem relacionada com a sincronização.
    private fun showSyncError(message: String?) {
        if (message.isNullOrBlank()) {
            binding.textSyncError.visibility = View.GONE
        } else {
            binding.textSyncError.text = message
            binding.textSyncError.visibility = View.VISIBLE
        }
    }
}
