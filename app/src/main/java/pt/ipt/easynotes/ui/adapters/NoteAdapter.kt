package pt.ipt.easynotes.ui.adapters

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.easynotes.MainActivity
import pt.ipt.easynotes.R
import pt.ipt.easynotes.data.Note
import pt.ipt.easynotes.databinding.ItemNoteBinding

/**
 * Adapter responsável por apresentar as notas no RecyclerView.
 */
class NoteAdapter(
    private val activity: MainActivity
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()

    fun setNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.textTitle.text = note.title
            binding.textContent.text = note.content

            binding.noteContainer.setOnClickListener {
                activity.showEditor(note.id)
            }

            val path = note.photoPath
            if (path.isNullOrBlank()) {
                binding.imagePhoto.visibility = View.GONE
                binding.imagePhoto.setImageDrawable(null)
            } else {
                val bitmap = BitmapFactory.decodeFile(path)

                if (bitmap == null) {
                    binding.imagePhoto.visibility = View.GONE
                } else {
                    binding.imagePhoto.setImageBitmap(bitmap)
                    binding.imagePhoto.visibility = View.VISIBLE
                    binding.imagePhoto.setOnClickListener {
                        showPhoto(path)
                    }
                }
            }
        }

        private fun showPhoto(path: String) {
            val bitmap = BitmapFactory.decodeFile(path) ?: return
            val image = ImageView(activity)
            image.setImageBitmap(bitmap)
            image.adjustViewBounds = true

            AlertDialog.Builder(activity)
                .setView(image)
                .setPositiveButton(R.string.close, null)
                .show()
        }
    }
}
