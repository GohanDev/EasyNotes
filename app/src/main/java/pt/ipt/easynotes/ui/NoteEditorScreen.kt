package pt.ipt.easynotes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ipt.easynotes.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: Int? = null,
    onNoteSaved: () -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var content by remember {
        mutableStateOf("")
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            val note = viewModel.getNoteById(noteId)

            if (note != null) {
                title = note.title
                content = note.content
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = stringResource(
                if (noteId == null) {
                    R.string.new_note
                } else {
                    R.string.edit_note
                }
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            label = {
                Text(
                    text = stringResource(R.string.title)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = content,
            onValueChange = {
                content = it
            },
            label = {
                Text(
                    text = stringResource(R.string.content)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (noteId != null) {

            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.delete)
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        Button(
            onClick = {
                if (noteId == null) {

                    viewModel.addNote(
                        title = title,
                        content = content
                    )

                } else {

                    viewModel.updateNote(
                        id = noteId,
                        title = title,
                        content = content
                    )
                }

                onNoteSaved()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.save)
            )
        }
    }
    if (showDeleteDialog && noteId != null) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text = stringResource(R.string.delete_note)
                )
            },

            text = {
                Text(
                    text = stringResource(R.string.delete_note_confirmation)
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNoteById(noteId)
                        showDeleteDialog = false
                        onNoteSaved()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete)
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cancel)
                    )
                }
            }
        )
    }
}