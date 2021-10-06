package org.softeg.slartus.forpdaplus.prefs

import android.app.Activity
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.OpenFileDialog
import org.softeg.slartus.forpdaplus.db.NotesDbHelper
import org.softeg.slartus.forpdaplus.db.NotesTable
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showAbout
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showAboutHistory
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showSelectDirDialog
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showShareIt
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import org.softeg.slartus.forpdaplus.fragments.base.ProgressDialog
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager
import org.softeg.slartus.forpdaplus.repositories.NotesRepository
import java.io.File
import java.util.*

class PrefsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (val key = preference?.key) {
            "path.system_path" -> {
                showSelectDirDialog(requireContext())
                return true
            }
            "About.AppVersion" -> {
                showAbout(requireContext())
                return true
            }
            "cookies.delete" -> {
                showCookiesDeleteDialog()
                return true
            }
            "About.History" -> {
                showAboutHistory(requireContext())
                return true
            }
            "About.ShareIt" -> {
                showShareIt(requireContext())
                return true
            }
            "About.ShowTheme" -> {
                showTheme("271502")
                return true
            }
            "notifiers.service.sound" -> {
                pickRingtone(Preferences.Notifications.sound)
                return true
            }
            "notifiers.silent_mode.start_time" -> {
                val calendar = Preferences.Notifications.SilentMode.startTime
                TimePickerDialog(activity, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    Preferences.Notifications.SilentMode.setStartTime(hourOfDay, minute)
                    findPreference(key)?.summary =
                        String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], true).show()
                return true
            }
            "notifiers.silent_mode.end_time" -> {
                val endcalendar = Preferences.Notifications.SilentMode.endTime
                TimePickerDialog(activity, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    Preferences.Notifications.SilentMode.setEndTime(hourOfDay, minute)
                    findPreference(key)?.summary =
                        String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                }, endcalendar[Calendar.HOUR_OF_DAY], endcalendar[Calendar.MINUTE], true).show()
                return true
            }
            "About.CheckNewVersion" -> {
                checkUpdates()
                return true
            }
            "About.OpenThemeForPda" -> {
                showTheme("820313")
                return true
            }
            "notes.backup" -> {
                showBackupNotesBackupDialog()
            }
            "notes.restore" -> {
                restoreNotes()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun findPreference(key: String) =
        super.findPreference<Preference>(key)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference("notifiers.silent_mode.start_time")?.let { preference ->
            val clndr = Preferences.Notifications.SilentMode.startTime
            preference.summary = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                clndr[Calendar.HOUR_OF_DAY],
                clndr[Calendar.MINUTE]
            )
        }

        findPreference("notifiers.silent_mode.end_time")?.let { preference ->

            val clndr = Preferences.Notifications.SilentMode.endTime
            preference.summary = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                clndr[Calendar.HOUR_OF_DAY],
                clndr[Calendar.MINUTE]
            )
        }

        findPreference("notifiers.service.use_sound")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, o: Any? ->
                val useSound = o as Boolean?
                findPreference("notifiers.service.is_default_sound")?.isEnabled = useSound!!
                findPreference("notifiers.service.sound")?.isEnabled = useSound
                true
            }

        findPreference("notifiers.service.is_default_sound")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, o: Any? ->
                val isDefault = o as Boolean?
                findPreference("notifiers.service.sound")?.isEnabled = !isDefault!!
                true
            }

        // DonateActivity.setDonateClickListeners(this)

        notesCategory()
    }

    private fun notesCategory() {

        findPreference("notes.remote.url")?.let { p ->
            p.summary = Preferences.Notes.remoteUrl
            p.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showNotesRemoteServerDialog()
                true
            }
        }
        findPreference("notes.remote.help")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                IntentActivity.showInDefaultBrowser(
                    activity,
                    "https://github.com/slartus/4pdaClient-plus/wiki/Notes"
                )
                true
            }
        refreshNotesEnabled()
    }

    private fun refreshNotesEnabled() {
//            findPreference("notes.remote.settings").isEnabled = !Preferences.Notes.isLocal()
//            findPreference("notes.backup.category").isEnabled = Preferences.Notes.isLocal()
    }

    private fun setLoading(progress: Boolean) {
        if (!isAdded) return

        val progressTag = "PROGRESS_TAG"
        val fragment = childFragmentManager.findFragmentByTag(progressTag)
        if (fragment != null && !progress) {
            (fragment as ProgressDialog).dismissAllowingStateLoss()
            childFragmentManager.executePendingTransactions()
        } else if (fragment == null && progress) {
            ProgressDialog().show(childFragmentManager, progressTag)
            childFragmentManager.executePendingTransactions()
        }
    }

    private fun showNotesRemoteServerDialog() {
        val inflater =
            (activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val view = inflater.inflate(R.layout.input_notes_remote_url_layout, null as ViewGroup?)
        val editText = view.findViewById<EditText>(R.id.edit_text)
        editText.setText(Preferences.Notes.remoteUrl)
        MaterialDialog.Builder(requireContext())
            .title(R.string.notes_remote_url)
            .customView(view, true)
            .cancelable(true)
            .positiveText(R.string.ok)
            .negativeText(R.string.cancel)
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                try {
                    val baseUrl = editText?.text.toString()
                    setLoading(true)
                    NotesRepository.checUrlAsync(baseUrl, {
                        setLoading(false)
                        Preferences.Notes.setPlacement("remote")
                        Preferences.Notes.remoteUrl = baseUrl
                        findPreference("notes.remote.url")?.summary = baseUrl
                        refreshNotesEnabled()
                    }, {
                        setLoading(false)
                        Preferences.Notes.setPlacement("local")
                        findPreference("notes.placement")?.summary = resources
                            .getStringArray(R.array.NotesStoragePlacements)
                            .firstOrNull()
                        refreshNotesEnabled()

                        AppLog.e(
                            activity, NotReportException(
                                it.localizedMessage
                                    ?: it.message, it
                            )
                        )
                    })
                } catch (ex: Throwable) {
                    AppLog.e(activity, ex)
                }
            }
            .show()
    }

    private fun showBackupNotesBackupDialog() {
        try {
            val dbFile = File(NotesDbHelper.DATABASE_DIR + "/" + NotesDbHelper.DATABASE_NAME)
            if (!dbFile.exists()) {
                AlertDialog.Builder(requireContext())
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
                AlertDialog.Builder(requireContext())
                    .setTitle("Ошибка").setMessage("Не удалось создать файл: $toPath")
                    .setPositiveButton("ОК", null)
                    .create().show()
                return
            }
            FileUtils.copy(dbFile, newFile)
            AlertDialog.Builder(requireContext())
                .setTitle("Успех!")
                .setMessage("Резервная копия заметок сохранена в файл:\n$newFile")
                .setPositiveButton("ОК", null)
                .create().show()
        } catch (ex: Throwable) {
            AppLog.e(activity, ex)
        }
    }

    private fun restoreNotes() {
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
                    AlertDialog.Builder(requireContext())
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
                                AlertDialog.Builder(requireContext())
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

    private fun checkUpdates() {
        val notifiersManager = NotifiersManager()
        ForPdaVersionNotifier(notifiersManager, 0, true).start(requireContext())
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri =
                    result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                Preferences.Notifications.sound = uri
            }
        }

    private fun pickRingtone(defaultSound: Uri?) {

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            App.getContext().getString(R.string.pick_audio)
        )
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultSound)

        resultLauncher.launch(intent)
    }

    private fun showTheme(themeId: String) {
        activity?.finish()
        ThemeFragment.showTopicById(themeId)
    }

    private fun showCookiesDeleteDialog() {
        MaterialDialog.Builder(requireContext())
            .title(getString(R.string.ConfirmTheAction))
            .content(getString(R.string.SureDeleteFile))
            .cancelable(true)
            .positiveText(getString(R.string.Delete))
            .negativeText(getString(R.string.no))
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                try {
                    val cookieFilePath = Preferences.cookieFilePath
                    val f = File(cookieFilePath)
                    if (!f.exists()) {
                        Toast.makeText(
                            activity, getString(R.string.CookiesFileNotFound) +
                                    ": " + cookieFilePath, Toast.LENGTH_LONG
                        ).show()
                    }
                    if (f.delete()) Toast.makeText(
                        activity, getString(R.string.CookiesFileDeleted) +
                                ": " + cookieFilePath, Toast.LENGTH_LONG
                    ).show() else Toast.makeText(
                        activity, getString(R.string.FailedDeleteCookies) +
                                ": " + cookieFilePath, Toast.LENGTH_LONG
                    ).show()
                } catch (ex: Exception) {
                    AppLog.e(activity, ex)
                }
            }
            .show()
    }

}
