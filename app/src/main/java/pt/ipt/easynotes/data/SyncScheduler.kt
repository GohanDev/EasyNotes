package pt.ipt.easynotes.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object SyncScheduler {

    fun scheduleSync(context: Context) {

        // O Worker só pode executar quando existir ligação à rede.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<NotesSyncWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "notes_sync",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }
}