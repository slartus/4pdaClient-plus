package org.softeg.slartus.forpdaplus.prefs

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.NotesDbHelper
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showBackupNotesBackupDialog
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showSelectDirDialog
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import org.softeg.slartus.forpdaplus.fragments.base.ProgressDialog
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

            "cookies.delete" -> {
                showCookiesDeleteDialog()
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

            "notes.backup" -> {
                showBackupNotesBackupDialog(
                    requireContext(),
                    NotesDbHelper.DATABASE_DIR + "/" + NotesDbHelper.DATABASE_NAME
                )
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
