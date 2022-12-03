package org.softeg.slartus.forpdaplus.prefs

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.OpenFileDialog
import org.softeg.slartus.forpdaplus.db.NotesDbHelper
import org.softeg.slartus.forpdaplus.db.NotesTable
import java.io.File
import java.util.*

object PreferencesNotes {
    val BACKUP_REQUEST_CODE = App.getInstance().uniqueIntValue
    val RESTORE_REQUEST_CODE = App.getInstance().uniqueIntValue
    fun showBackupNotesBackupDialog(activity: Activity) {
        if (!checkPermissions(
                activity = activity,
                permissions = setOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                requestCode = BACKUP_REQUEST_CODE
            )
        ) return
        try {
            val dbFile = File(NotesDbHelper.DATABASE_DIR + "/" + NotesDbHelper.DATABASE_NAME)
            if (!dbFile.exists()) {
                AlertDialog.Builder(activity)
                    .setTitle("Ошибка")
                    .setMessage("Файл базы заметок не найден. Возможно, вы ещё не создали ни одной заметки")
                    .setPositiveButton("ОК", null)
                    .create().show()
                return
            }
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
                AlertDialog.Builder(activity)
                    .setTitle("Ошибка").setMessage("Не удалось создать файл: $toPath")
                    .setPositiveButton("ОК", null)
                    .create().show()
                return
            }
            FileUtils.copy(dbFile, newFile)
            AlertDialog.Builder(activity)
                .setTitle("Успех!")
                .setMessage("Резервная копия заметок сохранена в файл:\n$newFile")
                .setPositiveButton("ОК", null)
                .create().show()
        } catch (ex: Throwable) {
            AppLog.e(activity, ex)
        }
    }

    fun restoreNotes(activity: Activity) {
        if (!checkPermissions(
                activity = activity,
                permissions = setOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                requestCode = BACKUP_REQUEST_CODE
            )
        ) return
        OpenFileDialog(activity)
            .setFilter(".*\\.(?i:sqlite)")
            .setOpenDialogListener { fileName: String? ->
                try {
                    val sourceuri = Uri.parse(fileName)
                    if (sourceuri == null) {
                        Toast.makeText(activity, "Файл не выбран!", Toast.LENGTH_SHORT).show()
                        return@setOpenDialogListener
                    }
                    val notes = NotesTable.getNotesFromFile(fileName)
                    AlertDialog.Builder(activity)
                        .setTitle("Внимание!")
                        .setMessage(
                            """
    Заметок для восстановления: ${notes.size}
    
    Восстановление заметок приведёт к полной потере всех существующих заметок!
    """.trimIndent()
                        )
                        .setPositiveButton("Продолжить") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            try {
                                val count = NotesTable.restoreFrom(notes)
                                AlertDialog.Builder(activity)
                                    .setTitle("Успех!")
                                    .setMessage("Резервная копия заметок восстановлена!\nЗаметок восстановлено: $count")
                                    .setPositiveButton("ОК", null)
                                    .create().show()
                            } catch (ex: Throwable) {
                                AppLog.e(activity, ex)
                            }
                        }
                        .setNegativeButton("Отмена", null)
                        .create().show()
                } catch (ex: Throwable) {
                    AppLog.e(activity, ex)
                }
            }
            .show()
    }

    private fun checkPermissions(
        activity: Activity,
        permissions: Set<String>,
        requestCode: Int
    ): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->
                true

            permissions.any { permission ->
                ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            } -> {
                activity.requestPermissions(permissions.toTypedArray(), requestCode)
                false
            }

            permissions.any { permission ->
                shouldShowRequestPermissionRationale(activity, permission)
            } -> {
                Toast.makeText(
                    activity,
                    "Need permissions for access to files",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager() -> {
                activity.startActivity(Intent().apply {
                    action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    data = Uri.fromParts("package", activity.packageName, null)
                });
                false
            }
            else -> true
        }
    }
}