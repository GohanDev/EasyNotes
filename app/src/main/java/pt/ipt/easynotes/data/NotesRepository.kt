package pt.ipt.easynotes.data

import kotlinx.coroutines.flow.Flow
import pt.ipt.easynotes.network.NotesApiService

class NotesRepository(
    private val noteDao: NoteDao
) {

    fun getNotesByUser(
        userId: Int
    ): Flow<List<Note>> {
        return noteDao.getNotesByUser(userId)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun getRemoteNotes(
        token: String
    ) = NotesApiService.getNotes(token)

    suspend fun createRemoteNote(
        token: String,
        title: String,
        content: String
    ) = NotesApiService.createNote(
        token = token,
        title = title,
        content = content
    )

    suspend fun updateRemoteNote(
        token: String,
        id: Int,
        title: String,
        content: String
    ) = NotesApiService.updateNote(
        token = token,
        id = id,
        title = title,
        content = content
    )

    suspend fun deleteRemoteNote(
        token: String,
        id: Int
    ) {
        NotesApiService.deleteNote(
            token = token,
            id = id
        )
    }

    suspend fun syncRemoteNotesToLocal(
        token: String,
        userId: Int
    ) {
        val remoteNotes = getRemoteNotes(token)

        val remoteIds = remoteNotes.map { it.id }

        if (remoteIds.isEmpty()) {

            noteDao.deleteAllRemoteNotesForUser(
                userId = userId
            )

        } else {

            noteDao.deleteMissingRemoteNotes(
                userId = userId,
                remoteIds = remoteIds
            )
        }

        remoteNotes.forEach { remoteNote ->

            val localNote = noteDao.getNoteByRemoteId(
                remoteId = remoteNote.id,
                userId = userId
            )

            if (localNote == null) {

                noteDao.insertNote(
                    Note(
                        remoteId = remoteNote.id,
                        userId = userId,
                        title = remoteNote.title,
                        content = remoteNote.content,
                        photoPath = null
                    )
                )

            } else {

                noteDao.updateNote(
                    localNote.copy(
                        title = remoteNote.title,
                        content = remoteNote.content
                    )
                )
            }
        }
    }
}