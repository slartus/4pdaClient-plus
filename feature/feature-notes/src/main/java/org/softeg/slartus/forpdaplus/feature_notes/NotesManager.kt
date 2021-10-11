package org.softeg.slartus.forpdaplus.feature_notes

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.OpenFileDialog
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NotesBackupManager @Inject constructor(
    private val appDatabase: AppDatabase,
    private val notesDao: NotesDao
) {
    fun restoreNotes(context: Context) {
        val errorJoinHandler = CoroutineExceptionHandler { _, ex ->
            Timber.e(ex)
        }
        val job = Job()
        val scope = CoroutineScope(job + Dispatchers.IO + errorJoinHandler)

        OpenFileDialog(context)
            .setFilter(".*\\.(?i:sqlite)")
            .setOpenDialogListener { fileName: String? ->
                try {
                    val sourceuri = Uri.parse(fileName)
                    if (sourceuri == null || fileName == null) {
                        Toast.makeText(context, "Файл не выбран!", Toast.LENGTH_SHORT).show()
                        return@setOpenDialogListener
                    }
                    val notes = getNotesFromFile(fileName)

                    AlertDialog.Builder(context)
                        .setTitle("Внимание!")
                        .setMessage(
                            """
    Заметок для восстановления: ${notes.size}

    Восстановление заметок приведёт к полной потере всех существующих заметок!
    """.trimIndent()
                        )
                        .setPositiveButton("Продолжить") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            scope.launch {
                                restoreFrom(notes)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Заметки восстановлены",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("Отмена", null)
                        .create().show()
                } catch (ex: Throwable) {
                    Timber.e(ex)
                }
            }
            .show()
    }

    fun backupNotes(context: Context) {
        showBackupNotesBackupDialog(context)
    }

    private fun showBackupNotesBackupDialog(context: Context) {
        val dbFile = appDatabase.getDatabasePath(context)
        if (!dbFile.exists()) {
            AlertDialog.Builder(context)
                .setTitle("Ошибка")
                .setMessage("Файл базы заметок не найден. Возможно, вы ещё не создали ни одной заметки")
                .setPositiveButton("ОК", null)
                .create().show()
            return
        }
        try {
            appDatabase.close()
            val externalDirPath: String
            val externalLocations = ExternalStorage.getAllStorageLocations()
            val sdCard = externalLocations[ExternalStorage.SD_CARD]
            val externalSdCard = externalLocations[ExternalStorage.EXTERNAL_SD_CARD]
            externalDirPath = externalSdCard?.toString()
                ?: (sdCard?.toString()
                    ?: Environment.getExternalStorageDirectory().toString())
            val toPath = "$externalDirPath/forpda_notes.sqlite"
            var newFile = File(toPath)
            var i = 0
            while (newFile.exists()) {
                newFile = File(
                    externalDirPath + String.format(
                        Locale.getDefault(),
                        "/forpda_notes_%d.sqlite",
                        i++
                    )
                )
            }
            val b = newFile.createNewFile()
            if (!b) {
                AlertDialog.Builder(context)
                    .setTitle("Ошибка").setMessage("Не удалось создать файл: $toPath")
                    .setPositiveButton("ОК", null)
                    .create().show()
                return
            }
            FileUtils.copy(dbFile, newFile)
            AlertDialog.Builder(context)
                .setTitle("Успех!")
                .setMessage("Резервная копия заметок сохранена в файл:\n$newFile")
                .setPositiveButton("ОК", null)
                .create().show()
        } catch (ex: Throwable) {
            Timber.e(ex)
        }
    }

    private fun getNotesFromFile(filePath: String): List<Note> {
        SQLiteDatabase.openOrCreateDatabase(File(filePath), null).use { backupDb ->
            return getNotes(
                backupDb,
                null
            )
        }
    }

    private fun getNotes(
        db: SQLiteDatabase,
        topicId: String?
    ): List<Note> {
        val notes: ArrayList<Note> = ArrayList<Note>()
        var c: Cursor? = null
        try {
            var selection: String? = null
            var selectionArgs: Array<String?>? = null
            if (!TextUtils.isEmpty(topicId)) {
                selection = "$COLUMN_TOPIC_ID=?"
                selectionArgs = arrayOf(topicId)
            }
            c = db.query(
                TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "$COLUMN_DATE DESC"
            )
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
            if (c.moveToFirst()) {
                val columnIdIndex = c.getColumnIndex(COLUMN_ID)
                val columnTitleIndex = c.getColumnIndex(COLUMN_TITLE)
                val columnBodyIndex = c.getColumnIndex(COLUMN_BODY)
                val columnUrlIndex = c.getColumnIndex(COLUMN_URL)
                val columnTopicIdIndex = c.getColumnIndex(COLUMN_TOPIC_ID)
                val columnPostIdIndex = c.getColumnIndex(COLUMN_POST_ID)
                val columnUserIdIndex = c.getColumnIndex(COLUMN_USER_ID)
                val columnUserIndex = c.getColumnIndex(COLUMN_USER)
                val columnTopicIndex = c.getColumnIndex(COLUMN_TOPIC)
                val columnDateIndex = c.getColumnIndex(COLUMN_DATE)
                do {
                    val note = Note(
                        id = c.getString(columnIdIndex).toIntOrNull(),
                        title = c.getString(columnTitleIndex),
                        body = c.getString(columnBodyIndex),
                        url = c.getString(columnUrlIndex),
                        topicId = c.getString(columnTopicIdIndex),
                        topicTitle = c.getString(columnTopicIndex),
                        postId = c.getString(columnPostIdIndex),
                        userId = c.getString(columnUserIdIndex),
                        userName = c.getString(columnUserIndex),
                        date = dateFormat.parse(c.getString(columnDateIndex)) ?: Date()
                    )

                    notes.add(note)
                } while (c.moveToNext())
            }
        } finally {
            c?.close()
        }
        return notes
    }

    private suspend fun restoreFrom(notes: List<Note>) {
        notesDao.merge(notes)
    }

    fun remoteUrl(context: Context) {

    }

    companion object {
        private const val TABLE_NAME = "Notes"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "Title"
        private const val COLUMN_BODY = "Body"
        private const val COLUMN_URL = "Url"
        private const val COLUMN_TOPIC_ID = "TopicId"
        private const val COLUMN_POST_ID = "PostId"
        private const val COLUMN_USER_ID = "UserId"
        private const val COLUMN_USER = "User"
        private const val COLUMN_TOPIC = "Topic"
        private const val COLUMN_DATE = "Date"
    }
}