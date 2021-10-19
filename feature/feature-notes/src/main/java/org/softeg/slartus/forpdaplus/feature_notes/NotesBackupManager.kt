package org.softeg.slartus.forpdaplus.feature_notes

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
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
        OpenFileDialog(context)
            .setFilter(".*\\.(?i:sqlite)")
            .setOpenDialogListener { file: File? ->
                try {
                    val sourceUri = Uri.fromFile(file)
                    if (sourceUri == null || file == null) {
                        Toast.makeText(context, "Файл не выбран!", Toast.LENGTH_SHORT).show()
                        return@setOpenDialogListener
                    }
                    restoreNotes(context, file)
                } catch (ex: Throwable) {
                    Timber.e(ex)
                }
            }
            .show()
    }

    fun restoreNotes(context: Context, fileUri: Uri) {
        try {
            context.contentResolver.openInputStream(fileUri)?.let { inputStream ->
                val tempFile = FileUtils.createTempFile(context)
                FileUtils.copyInputStreamToFile(inputStream, tempFile)

                restoreNotes(context, tempFile)
                tempFile.delete()
            }
        } catch (ex: Throwable) {
            Timber.e(ex)
        }
    }

    private fun restoreNotes(context: Context, file: File) {
        val notes = getNotesFromFile(file)
        val errorJoinHandler = CoroutineExceptionHandler { _, ex ->
            Timber.e(ex)
        }
        val job = Job()
        val scope = CoroutineScope(job + Dispatchers.Default + errorJoinHandler)
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

    private fun getNotesFromFile(file: File): List<Note> {
        SQLiteDatabase.openOrCreateDatabase(file, null).use { backupDb ->
            return getNotes(backupDb)
        }
    }

    private fun isOldDb(db: SQLiteDatabase): Boolean {
        val query = "select DISTINCT tbl_name from sqlite_master where tbl_name = 'Setting'"
        db.rawQuery(query, null).use { cursor ->
            if (cursor != null) {
                if (cursor.count > 0) {
                    return false
                }
            }
            return true
        }
    }

    private fun getNotes(
        db: SQLiteDatabase
    ): List<Note> {
        val notes: ArrayList<Note> = ArrayList<Note>()
        val oldStruct = isOldDb(db)
        val tableStruct = if (oldStruct) NotesTableStruct.Old else NotesTableStruct.Actual
        var c: Cursor? = null
        try {
            c = db.query(
                tableStruct.tableName,
                null,
                null,
                null,
                null,
                null,
                "${tableStruct.dateColumn} DESC"
            )
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
            if (c.moveToFirst()) {
                val columnIdIndex = c.getColumnIndex(tableStruct.idColumn)
                val columnTitleIndex = c.getColumnIndex(tableStruct.titleColumn)
                val columnBodyIndex = c.getColumnIndex(tableStruct.bodyColumn)
                val columnUrlIndex = c.getColumnIndex(tableStruct.urlColumn)
                val columnTopicIdIndex = c.getColumnIndex(tableStruct.topicIdColumn)
                val columnPostIdIndex = c.getColumnIndex(tableStruct.postIdColumn)
                val columnUserIdIndex = c.getColumnIndex(tableStruct.userIdColumn)
                val columnUserIndex = c.getColumnIndex(tableStruct.userNameColumn)
                val columnTopicIndex = c.getColumnIndex(tableStruct.topicTitleColumn)
                val columnDateIndex = c.getColumnIndex(tableStruct.dateColumn)
                fun getDate() =
                    if (oldStruct) dateFormat.parse(c.getString(columnDateIndex)) ?: Date()
                    else
                        Date(c.getLong(columnDateIndex))
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
                        date = getDate()
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

    fun migrateFromOld(oldNotesDbPath: String) {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            Timber.w(throwable)
        }
        MainScope().launch(Dispatchers.Default + errorHandler) {
            val oldDbFile = File(oldNotesDbPath)
            if (!oldDbFile.exists()) {
                return@launch
            }
            if (notesDao.getAll().isNotEmpty()) return@launch
            val notes = getNotesFromFile(oldDbFile)
            restoreFrom(notes)
            oldDbFile.delete()
        }
    }
}

private enum class NotesTableStruct(
    val tableName: String,
    val idColumn: String,
    val titleColumn: String,
    val bodyColumn: String,
    val urlColumn: String,
    val topicIdColumn: String,
    val topicTitleColumn: String,
    val postIdColumn: String,
    val userIdColumn: String,
    val userNameColumn: String,
    val dateColumn: String
) {
    Old(
        tableName = "Notes",
        idColumn = "_id",
        titleColumn = "Title",
        bodyColumn = "Body",
        urlColumn = "Url",
        topicIdColumn = "TopicId",
        topicTitleColumn = "Topic",
        postIdColumn = "PostId",
        userIdColumn = "UserId",
        userNameColumn = "User",
        dateColumn = "Date"
    ),
    Actual(
        tableName = "note",
        idColumn = "id",
        titleColumn = "title",
        bodyColumn = "body",
        urlColumn = "url",
        topicIdColumn = "topicId",
        topicTitleColumn = "topicTitle",
        postIdColumn = "postId",
        userIdColumn = "userId",
        userNameColumn = "userName",
        dateColumn = "date"
    )
}