package pt.ipt.easynotes.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class NotesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        val database =
            NotesDatabase.getDatabase(applicationContext)

        val repository =
            NotesRepository(database.noteDao())

        val sessionManager =
            SessionManager(applicationContext)

        val session =
            sessionManager.session.first()
                ?: return Result.success()

        return try {

            // 1. Envia notas criadas offline
            repository.syncPendingCreates(
                token = session.token,
                userId = session.userId
            )

            // 2. Envia alterações feitas offline
            repository.syncPendingUpdates(
                token = session.token,
                userId = session.userId
            )

            // 3. Processa eliminações feitas offline
            repository.syncPendingDeletes(
                token = session.token,
                userId = session.userId
            )

            // 4. Atualiza o Room com o estado da API
            repository.syncRemoteNotesToLocal(
                token = session.token,
                userId = session.userId
            )

            Result.success()

        } catch (e: Exception) {

            // Se a API estiver temporariamente indisponível,
            // o WorkManager volta a tentar mais tarde.
            Result.retry()
        }
    }
}