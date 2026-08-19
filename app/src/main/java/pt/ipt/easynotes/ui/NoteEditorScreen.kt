package pt.ipt.easynotes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ipt.easynotes.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory

@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: Int? = null,
    onBack: () -> Unit
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

    var photoBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var photoPath by remember {
        mutableStateOf<String?>(null)
    }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        if (bitmap != null) {

            photoBitmap = bitmap

            val file = File(
                context.filesDir,
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    var titleError by remember {
        mutableStateOf(false)
    }

    var contentError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            val note = viewModel.getNoteById(noteId)

            if (note != null) {
                title = note.title
                content = note.content
                photoPath = note.photoPath

                if (note.photoPath != null) {
                    photoBitmap = BitmapFactory.decodeFile(note.photoPath)
                }
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
                titleError = false
            },
            isError = titleError,

            label = {
                Text(
                    text = stringResource(R.string.title)
                )
            },

            supportingText = {
                if (titleError) {
                    Text(
                        text = stringResource(R.string.title_required)
                    )
                }
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
                contentError = false
            },
            isError = contentError,

            label = {
                Text(
                    text = stringResource(R.string.content)
                )
            },

            supportingText = {
                if (contentError) {
                    Text(
                        text = stringResource(R.string.content_required)
                    )
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                val hasCameraPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                if (hasCameraPermission) {
                    cameraLauncher.launch(null)
                } else {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.take_photo)
            )
        }

        photoBitmap?.let { bitmap ->

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.take_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.back)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Button(
                onClick = {

                    titleError = title.isBlank()
                    contentError = content.isBlank()

                    if (titleError || contentError) {
                        return@Button
                    }

                    if (noteId == null) {

                        viewModel.addNote(
                            title = title,
                            content = content,
                            photoPath = photoPath
                        )

                    } else {

                        viewModel.updateNote(
                            id = noteId,
                            title = title,
                            content = content,
                            photoPath = photoPath
                        )
                    }

                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.save)
                )
            }
        }

        if (noteId != null) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(R.string.delete)
                )
            }
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
                        onBack()
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