package org.softeg.slartus.forpdaplus.prefs

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showCookiesDeleteDialog
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs.showSelectDirDialog
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
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
                showCookiesDeleteDialog(requireContext())
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

}
