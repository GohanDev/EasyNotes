package pt.ipt.easynotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        "SELECT * FROM notes " +
                "WHERE userId = :userId " +
                "AND syncStatus != 'PENDING_DELETE' " +
                "ORDER BY id DESC"
    )
    fun getNotesByUser(userId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?

    @Query(
        "SELECT * FROM notes " +
                "WHERE remoteId = :remoteId AND userId = :userId " +
                "LIMIT 1"
    )
    suspend fun getNoteByRemoteId(
        remoteId: Int,
        userId: Int
    ): Note?

    @Query(
        "DELETE FROM notes " +
                "WHERE userId = :userId " +
                "AND remoteId IS NOT NULL " +
                "AND remoteId NOT IN (:remoteIds)"
    )
    suspend fun deleteMissingRemoteNotes(
        userId: Int,
        remoteIds: List<Int>
    )

    @Query(
        "DELETE FROM notes " +
                "WHERE userId = :userId " +
                "AND remoteId IS NOT NULL"
    )
    suspend fun deleteAllRemoteNotesForUser(
        userId: Int
    )

    @Query(
        "SELECT * FROM notes " +
                "WHERE userId = :userId AND syncStatus = :status"
    )
    suspend fun getNotesBySyncStatus(
        userId: Int,
        status: SyncStatus
    ): List<Note>

    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}