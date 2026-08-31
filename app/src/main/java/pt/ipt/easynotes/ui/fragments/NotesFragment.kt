package pt.ipt.easynotes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.databinding.FragmentNotesBinding
import pt.ipt.easynotes.ui.adapters.NoteAdapter

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
        observeNotes()
        observeSyncError()
    }

    private fun configureCurrentUser() {
        val state = activity.authViewModel.uiState.value
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

        activity.notesViewModel.loadRemoteNotes(
            token = token,
            userId = user.id
        )
    }

    private fun observeNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activity.notesViewModel.notes.collect { notes ->
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
            }
        }
    }

    private fun observeSyncError() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activity.notesViewModel.remoteError.collect { message ->
                    if (message.isNullOrBlank()) {
                        binding.textSyncError.visibility = View.GONE
                    } else {
                        binding.textSyncError.text = message
                        binding.textSyncError.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
