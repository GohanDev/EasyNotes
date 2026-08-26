package pt.ipt.easynotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.ipt.easynotes.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextOverflow
import pt.ipt.easynotes.data.Note
import androidx.compose.foundation.clickable
import androidx.compose.material3.TextButton
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.LaunchedEffect
import pt.ipt.easynotes.network.HealthService
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onAddNote: () -> Unit,
    onNoteClick: (Int) -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
){

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val remoteNotes by viewModel.remoteNotes.collectAsStateWithLifecycle()
    val remoteError by viewModel.remoteError.collectAsStateWithLifecycle()

    var apiStatus by remember {
        mutableStateOf("A verificar API...")
    }

    val context = LocalContext.current

    val localNetworkPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                apiStatus = "Permissão concedida. Reinicia a app."

            } else {

                apiStatus = "Acesso à rede local recusado."
            }
        }

    LaunchedEffect(Unit) {

        val hasLocalNetworkPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) == PackageManager.PERMISSION_GRANTED

        if (hasLocalNetworkPermission) {

            apiStatus = try {

                val response = HealthService.checkHealth()

                if (response.status == "ok") {
                    "API ligada"
                } else {
                    "API com problema"
                }

            } catch (e: Exception) {
                "Erro: ${e.message}"
            }

        } else {

            localNetworkPermissionLauncher.launch(
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name)
                        )

                        Text(
                            text = if (remoteError != null) {
                                remoteError!!
                            } else {
                                "Notas na API: ${remoteNotes.size}"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onAboutClick
                    ) {
                        Text(
                            text = stringResource(R.string.about)
                        )
                    }

                    TextButton(
                        onClick = onLogoutClick
                    ) {
                        Text(
                            text = stringResource(R.string.logout)
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->

        if (notes.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_notes)
                )

                Text(
                    text = stringResource(R.string.no_notes_description)
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {

                items(notes) { note ->

                    NoteCard(
                        note = note,
                        onClick = {
                            onNoteClick(note.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {

    var showPhoto by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (note.content.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (note.photoPath != null) {

                val bitmap = BitmapFactory.decodeFile(note.photoPath)

                if (bitmap != null) {

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.note_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clickable {
                                showPhoto = true
                            }
                    )
                }
            }
        }
    }
    if (showPhoto && note.photoPath != null) {

        val largeBitmap = BitmapFactory.decodeFile(note.photoPath)

        if (largeBitmap != null) {

            Dialog(
                onDismissRequest = {
                    showPhoto = false
                }
            ) {
                Image(
                    bitmap = largeBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.note_photo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            largeBitmap.width.toFloat() /
                                    largeBitmap.height.toFloat()
                        )
                )
            }
        }
    }
}