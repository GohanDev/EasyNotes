package pt.ipt.easynotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: Int? = null,
    val userId: Int,
    val title: String,
    val content: String,
    val photoPath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)