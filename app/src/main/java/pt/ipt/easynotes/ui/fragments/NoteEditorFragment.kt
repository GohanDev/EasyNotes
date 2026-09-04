package pt.ipt.easynotes.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.databinding.FragmentNoteEditorBinding
import java.io.File
import java.io.FileOutputStream

/**
 * Fragment utilizado para criar, editar e eliminar notas e para associar
 * fotografias tiradas com a câmara do dispositivo.
 */
class NoteEditorFragment : Fragment() {

    companion object {
        private const val ARG_NOTE_ID = "noteId"
        private const val CAMERA_REQUEST_CODE = 200
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101

        fun newInstance(noteId: Int?): NoteEditorFragment {
            val fragment = NoteEditorFragment()
            val arguments = Bundle()

            if (noteId != null) {
                arguments.putInt(ARG_NOTE_ID, noteId)
            }

            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var binding: FragmentNoteEditorBinding
    private lateinit var activity: MainActivity
    private var noteId: Int? = null
    private var photoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        if (arguments?.containsKey(ARG_NOTE_ID) == true) {
            noteId = arguments?.getInt(ARG_NOTE_ID)
        }

        configureScreen()

        binding.buttonBack.setOnClickListener {
            activity.goBack()
        }

        binding.buttonSave.setOnClickListener {
            saveNote()
        }

        binding.buttonDelete.setOnClickListener {
            confirmDelete()
        }

        binding.buttonCamera.setOnClickListener {
            openCamera()
        }
    }

    /**
     * Configura o ecrã de acordo com a operação: criação ou edição.
     */
    private fun configureScreen() {
        if (noteId == null) {
            binding.textHeading.text = getString(R.string.new_note)
            binding.textSubtitle.text = getString(R.string.new_note_subtitle)
            binding.buttonDelete.visibility = View.GONE
        } else {
            binding.textHeading.text = getString(R.string.edit_note)
            binding.textSubtitle.text = getString(R.string.edit_note_subtitle)
            binding.buttonDelete.visibility = View.VISIBLE
            loadNote()
        }
    }

    // Carrega do Internal Storage os dados da nota que está a ser editada.
    private fun loadNote() {
        val id = noteId ?: return
        val note = activity.notesViewModel.getNoteById(id) ?: return

        binding.editTitle.setText(note.title)
        binding.editContent.setText(note.content)
        photoPath = note.photoPath

        if (!photoPath.isNullOrBlank()) {
            val bitmap = BitmapFactory.decodeFile(photoPath)

            if (bitmap != null) {
                binding.imagePhoto.setImageBitmap(bitmap)
                binding.imagePhoto.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Valida título e conteúdo antes de criar ou atualizar a nota.
     */
    private fun saveNote() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        val titleInvalid = title.isBlank()
        val contentInvalid = content.isBlank()

        binding.textTitleError.visibility = if (titleInvalid) View.VISIBLE else View.GONE
        binding.textContentError.visibility = if (contentInvalid) View.VISIBLE else View.GONE

        if (titleInvalid || contentInvalid) {
            return
        }

        // Evita vários pedidos enquanto a operação de gravação está em curso.
        binding.buttonSave.isEnabled = false

        val id = noteId

        if (id == null) {
            activity.notesViewModel.addNote(
                title = title,
                content = content,
                photoPath = photoPath
            ) {
                activity.goBack()
            }
        } else {
            activity.notesViewModel.updateNote(
                id = id,
                title = title,
                content = content,
                photoPath = photoPath
            ) {
                activity.goBack()
            }
        }
    }

    /**
     * Pede confirmação antes de eliminar uma nota, conforme o requisito de
     * validação das operações de eliminação.
     */
    private fun confirmDelete() {
        val id = noteId ?: return

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_note)
            .setMessage(R.string.delete_note_confirmation)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                activity.notesViewModel.deleteNoteById(id) {
                    activity.goBack()
                }
            }
            .show()
    }

    /**
     * Verifica a permissão e abre a aplicação de câmara disponível no dispositivo.
     */
    private fun openCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            binding.textCameraError.visibility = View.VISIBLE
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return
        }

        binding.textCameraError.visibility = View.GONE

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    /**
     * Recebe a fotografia capturada pela câmara e guarda-a no Internal Storage.
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != CAMERA_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }

        val bitmap = data?.extras?.get("data") as? Bitmap ?: return
        savePhoto(bitmap)
    }

    private fun savePhoto(bitmap: Bitmap) {
        binding.imagePhoto.setImageBitmap(bitmap)
        binding.imagePhoto.visibility = View.VISIBLE

        val file = File(
            requireContext().filesDir,
            "note_photo_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                outputStream
            )
        }

        photoPath = file.absolutePath
    }
}
