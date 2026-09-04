package pt.ipt.easynotes.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Responsável por guardar e ler as notas no Internal Storage da aplicação.
 *
 * As notas são guardadas no ficheiro notes.json, dentro da pasta privada
 * da aplicação. Por estar em Internal Storage, não são necessárias permissões
 * adicionais para ler ou escrever este ficheiro.
 */
class NotesLocalStorage private constructor(context: Context) {

    private val file = File(
        context.applicationContext.filesDir,
        FILE_NAME
    )

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Devolve as notas do utilizador indicado e esconde as notas que estão
     * marcadas para eliminação enquanto aguardam sincronização com a API.
     */
    @Synchronized
    fun getNotesByUser(userId: Int): List<Note> {
        return loadNotes()
            .filter { note ->
                note.userId == userId &&
                        note.syncStatus != SyncStatus.PENDING_DELETE
            }
            .sortedByDescending { note -> note.id }
    }

    // Procura uma nota através do identificador local.
    @Synchronized
    fun getNoteById(id: Int): Note? {
        return loadNotes().firstOrNull { note ->
            note.id == id
        }
    }

    // Procura uma nota através do identificador atribuído pela API.
    @Synchronized
    fun getNoteByRemoteId(
        remoteId: Int,
        userId: Int
    ): Note? {
        return loadNotes().firstOrNull { note ->
            note.remoteId == remoteId && note.userId == userId
        }
    }

    // Obtém as notas que têm um determinado estado de sincronização.
    @Synchronized
    fun getNotesBySyncStatus(
        userId: Int,
        status: SyncStatus
    ): List<Note> {
        return loadNotes().filter { note ->
            note.userId == userId && note.syncStatus == status
        }
    }

    /**
     * Insere uma nova nota. Quando ainda não existe identificador local,
     * é gerado o próximo identificador disponível.
     */
    @Synchronized
    fun insertNote(note: Note) {
        val notes = loadNotes().toMutableList()

        val noteToInsert = if (note.id == 0) {
            note.copy(id = nextId(notes))
        } else {
            note
        }

        notes.add(noteToInsert)
        saveNotes(notes)
    }

    // Atualiza uma nota existente através do seu identificador local.
    @Synchronized
    fun updateNote(note: Note) {
        val notes = loadNotes().toMutableList()
        val index = notes.indexOfFirst { currentNote ->
            currentNote.id == note.id
        }

        if (index == -1) {
            return
        }

        notes[index] = note
        saveNotes(notes)
    }

    // Remove definitivamente uma nota do ficheiro local.
    @Synchronized
    fun deleteNote(note: Note) {
        val notes = loadNotes().filterNot { currentNote ->
            currentNote.id == note.id
        }

        saveNotes(notes)
    }

    /**
     * Remove notas sincronizadas que já não existem na API.
     */
    @Synchronized
    fun deleteMissingRemoteNotes(
        userId: Int,
        remoteIds: List<Int>
    ) {
        val notes = loadNotes().filterNot { note ->
            note.userId == userId &&
                    note.remoteId != null &&
                    note.remoteId !in remoteIds
        }

        saveNotes(notes)
    }

    /**
     * Remove todas as notas desse utilizador que anteriormente vieram da API.
     */
    @Synchronized
    fun deleteAllRemoteNotesForUser(userId: Int) {
        val notes = loadNotes().filterNot { note ->
            note.userId == userId && note.remoteId != null
        }

        saveNotes(notes)
    }

    /**
     * Lê e converte o conteúdo de notes.json para uma lista de notas.
     * Se o ficheiro não existir, estiver vazio ou não puder ser lido,
     * devolve uma lista vazia para evitar que a aplicação termine com erro.
     */
    private fun loadNotes(): List<Note> {
        if (!file.exists()) {
            return emptyList()
        }

        return try {
            val content = file.readText()

            if (content.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<Note>>(content)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Converte a lista de notas para JSON e grava-a no Internal Storage.
     */
    private fun saveNotes(notes: List<Note>) {
        val content = json.encodeToString(notes)
        file.writeText(content)
    }

    // Gera o próximo identificador local disponível.
    private fun nextId(notes: List<Note>): Int {
        return (notes.maxOfOrNull { note -> note.id } ?: 0) + 1
    }

    companion object {
        private const val FILE_NAME = "notes.json"

        @Volatile
        private var INSTANCE: NotesLocalStorage? = null

        /**
         * Mantém uma única instância do armazenamento local durante
         * a execução da aplicação.
         */
        fun getInstance(context: Context): NotesLocalStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = NotesLocalStorage(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
