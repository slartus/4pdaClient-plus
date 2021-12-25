package org.softeg.slartus.forpdaplus.prefs

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.Preference
import android.preference.PreferenceFragment
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.AppTheme.currentTheme
import org.softeg.slartus.forpdaplus.AppTheme.getThemeCssFileName
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.FilePath
import org.softeg.slartus.forpdaplus.classes.InputFilterMinMax
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.controls.OpenFileDialog
import org.softeg.slartus.forpdaplus.core.AppPreferences
import org.softeg.slartus.forpdaplus.db.NotesDbHelper
import org.softeg.slartus.forpdaplus.db.NotesTable
import org.softeg.slartus.forpdaplus.fragments.base.ProgressDialog
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.listtemplates.ListCore
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager
import org.softeg.slartus.forpdaplus.repositories.NotesRepository
import org.softeg.slartus.forpdaplus.styles.CssStyle
import org.softeg.slartus.forpdaplus.styles.StyleInfoActivity
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.PersistentCookieStore.Companion.getInstance
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
class PreferencesActivity : BasePreferencesActivity() {
    //private EditText red, green, blue;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(
            android.R.id.content,
            PrefsFragment()
        ).commitAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == RESULT_OK) if (requestCode == NOTIFIERS_SERVICE_SOUND_REQUEST_CODE) {
            val uri = intent?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            Preferences.Notifications.sound = uri
        }
    }

    class PrefsFragment : PreferenceFragment(), Preference.OnPreferenceClickListener {
        /* @Override
        public void onActivityCreated(android.os.Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.news_list_prefs, false);
        }*/
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
            //  ((PreferenceScreen)findPreference("common")).addPreference(new CheckBoxPreference(getContext()));
            findPreference("path.system_path").onPreferenceClickListener = this
            findPreference("appstyle").onPreferenceClickListener = this
            findPreference("accentColor").onPreferenceClickListener = this
            findPreference("mainAccentColor").onPreferenceClickListener = this
            findPreference("webViewFont").onPreferenceClickListener = this
            findPreference("userBackground").onPreferenceClickListener = this
            findPreference("About.AppVersion").onPreferenceClickListener = this
            findPreference("cookies.delete").onPreferenceClickListener = this
            findPreference("About.History").onPreferenceClickListener = this
            findPreference("About.ShareIt").onPreferenceClickListener = this
            findPreference("About.ShowTheme").onPreferenceClickListener = this
            findPreference("About.CheckNewVersion").onPreferenceClickListener = this
            findPreference("notifiers.silent_mode.start_time")?.let { preference ->
                preference.onPreferenceClickListener = this
                val clndr = Preferences.Notifications.SilentMode.startTime
                preference.summary = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    clndr[Calendar.HOUR_OF_DAY],
                    clndr[Calendar.MINUTE]
                )
            }

            findPreference("notifiers.silent_mode.end_time")?.let { preference ->
                preference.onPreferenceClickListener = this
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
                    findPreference("notifiers.service.is_default_sound").isEnabled = useSound!!
                    findPreference("notifiers.service.sound").isEnabled = useSound
                    true
                }

            findPreference("notifiers.service.is_default_sound")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, o: Any? ->
                    val isDefault = o as Boolean?
                    findPreference("notifiers.service.sound").isEnabled = !isDefault!!
                    true
                }

            findPreference("notifiers.service.sound").onPreferenceClickListener = this
            findPreference("notes.backup").onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    showBackupNotesBackupDialog()
                    true
                }
            findPreference("notes.restore").onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    restoreNotes()
                    true
                }
            DonateActivity.setDonateClickListeners(this)
            findPreference("showExitButton").onPreferenceClickListener = this

            notesCategory()
        }

        private fun notesCategory() {
            findPreference("notes.placement")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, value ->
                    if (value == "remote") {
                        showNotesRemoteServerDialog()
                    }
                    true
                }
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

            val PROGRESS_TAG = "PROGRESS_TAG"
            val fragment = fragmentManager.findFragmentByTag(PROGRESS_TAG)
            if (fragment != null && !progress) {
                (fragment as ProgressDialog).dismissAllowingStateLoss()
                fragmentManager.executePendingTransactions()
            } else if (fragment == null && progress) {
                ProgressDialog().show(fragmentManager, PROGRESS_TAG)
                fragmentManager.executePendingTransactions()
            }
        }

        private fun showNotesRemoteServerDialog() {
            val inflater = (activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            val view = inflater.inflate(R.layout.input_notes_remote_url_layout, null as ViewGroup?)
            val editText = view.findViewById<EditText>(R.id.edit_text)
            editText.setText(Preferences.Notes.remoteUrl ?: "")
            MaterialDialog.Builder(activity)
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

        override fun onPreferenceClick(preference: Preference): Boolean {
            when (val key = preference.key) {
                "path.system_path" -> {
                    showSelectDirDialog()
                    return true
                }
                "About.AppVersion" -> {
                    showAbout()
                    return true
                }
                "cookies.delete" -> {
                    showCookiesDeleteDialog()
                    return true
                }
                "About.History" -> {
                    showAboutHistory()
                    return true
                }
                "About.ShareIt" -> {
                    showShareIt()
                    return true
                }
                "About.ShowTheme" -> {
                    showTheme("271502")
                    return true
                }
                "appstyle" -> {
                    showStylesDialog()
                    return true
                }
                "accentColor" -> {
                    showAccentColorDialog()
                    return true
                }
                "mainAccentColor" -> {
                    showMainAccentColorDialog()
                    return true
                }
                "webViewFont" -> {
                    webViewFontDialog()
                    return true
                }
                "userBackground" -> {
                    pickUserBackground()
                    return true
                }
                "notifiers.service.sound" -> {
                    Preferences.Notifications.sound?.let{
                        pickRingtone(it)
                    }

                    return true
                }
                "notifiers.silent_mode.start_time" -> {
                    val calendar = Preferences.Notifications.SilentMode.startTime
                    TimePickerDialog(activity, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                        Preferences.Notifications.SilentMode.setStartTime(hourOfDay, minute)
                        findPreference(key).summary =
                            String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], true).show()
                    return true
                }
                "notifiers.silent_mode.end_time" -> {
                    val endcalendar = Preferences.Notifications.SilentMode.endTime
                    TimePickerDialog(activity, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                        Preferences.Notifications.SilentMode.setEndTime(hourOfDay, minute)
                        findPreference(key).summary =
                            String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    }, endcalendar[Calendar.HOUR_OF_DAY], endcalendar[Calendar.MINUTE], true).show()
                    return true
                }
                "About.CheckNewVersion" -> {
                    checkUpdates()
                    return true
                }
            }
            return false
        }

        private fun checkUpdates() {
            val notifiersManager = NotifiersManager()
            ForPdaVersionNotifier(notifiersManager, 0, true).start(activity)
        }

        private fun setMenuItems() {
            val preferences = preferenceManager.sharedPreferences
            var items = (preferences.getString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS)
                ?: ListCore.DEFAULT_MENU_ITEMS).split(",".toRegex()).toTypedArray()
            val allItems = ListCore.getAllMenuBricks()
            if (ListCore.checkIndex(items, allItems.size)) {
                items = ListCore.DEFAULT_MENU_ITEMS.split(",".toRegex()).toTypedArray()
            }
            val selectedItems = arrayOfNulls<Int>(items.size)
            for (i in items.indices) selectedItems[i] = items[i].toInt()
            val namesArray = ArrayList<String>()
            for (item in allItems) namesArray.add(item.title)
            val finalItems = Array<Array<Int?>?>(1) { arrayOfNulls(1) }
            finalItems[0] = selectedItems
            MaterialDialog.Builder(activity)
                .title(R.string.select_items)
                .items(*namesArray.toTypedArray<CharSequence>())
                .itemsCallbackMultiChoice(selectedItems) { _: MaterialDialog?, integers: Array<Int?>?, _: Array<CharSequence?>? ->
                    finalItems[0] = integers
                    true
                }
                .alwaysCallMultiChoiceCallback()
                .positiveText(R.string.accept)
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    if (finalItems.first()?.size ?: 0 == 0) return@onPositive
                    preferences.edit().putString(
                        "selectedMenuItems",
                        Arrays.toString(finalItems[0]).replace(" ", "").replace("[", "")
                            .replace("]", "")
                    ).apply()
                }
                .neutralText(R.string.reset)
                .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                    preferences.edit().putString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS)
                        .apply()
                }
                .show()
        }

        private fun pickUserBackground() {
            MaterialDialog.Builder(activity)
                .content(R.string.pick_image)
                .positiveText(R.string.choose)
                .negativeText(R.string.cancel)
                .neutralText(R.string.reset)
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    try {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, MY_INTENT_CLICK)
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(
                            activity,
                            R.string.no_app_for_get_image_file,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (ex: Exception) {
                        AppLog.e(activity, ex)
                    }
                }
                .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                    App.getInstance().preferences
                        .edit()
                        .putString("userInfoBg", "")
                        .putBoolean("isUserBackground", false)
                        .apply()
                }
                .show()
        }

        private fun createImageFile(): File {
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return File.createTempFile(
                "PHOTO_${timeStamp}_",
                ".jpg",
                App.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
        }

        private fun copyFileToTemp(uri: Uri): String {
            val context = App.getContext()
            val fileName = FilePath.getFileName(context, uri)
            val tempFile = createImageFile()
            context.contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
                FileOutputStream(tempFile, false).use { outputStream ->
                    FileUtils.CopyStream(inputStream, outputStream)
                }
            }
            return tempFile.absolutePath
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == RESULT_OK) {
                if (requestCode == MY_INTENT_CLICK) {
                    data?.data?.let { selectedImageUri ->
                        val selectedImagePath =
                            FilePath.getPath(App.getContext(), selectedImageUri) ?: copyFileToTemp(
                                selectedImageUri
                            )
                        App.getInstance().preferences
                            .edit()
                            .putString("userInfoBg", selectedImagePath)
                            .putBoolean("isUserBackground", true)
                            .apply()
                    }

                }
            }
        }

        private fun webViewFontDialog() {
            try {
                val prefs = App.getInstance().preferences
                val selected = intArrayOf(prefs.getInt("webViewFont", 0))
                val name = arrayOf<CharSequence>("")
                val dialogShowed = booleanArrayOf(false)
                MaterialDialog.Builder(activity)
                    .title(R.string.choose_font)
                    .items(
                        App.getContext().getString(R.string.font_from_style),
                        App.getContext().getString(R.string.system_font),
                        App.getContext().getString(R.string.enter_font_name)
                    )
                    .itemsCallbackSingleChoice(selected[0]) { _: MaterialDialog?, _: View?, which: Int, _: CharSequence? ->
                        selected[0] = which
                        when (which) {
                            0 -> name[0] = ""
                            1 -> name[0] = "inherit"
                            2 -> {
                                if (dialogShowed[0]) return@itemsCallbackSingleChoice true
                                dialogShowed[0] = true
                                MaterialDialog.Builder(activity)
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input(
                                        App.getContext().getString(R.string.font_name),
                                        prefs.getString("webViewFontName", "")
                                    ) { _: MaterialDialog?, input: CharSequence -> name[0] = input }
                                    .positiveText(R.string.ok)
                                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                                        prefs.edit()
                                            .putString("webViewFontName", name[0].toString())
                                            .apply()
                                    }
                                    .show()
                            }
                        }
                        true
                    }
                    .alwaysCallSingleChoiceCallback()
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancel)
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        prefs.edit().putString("webViewFontName", name[0].toString())
                            .putInt("webViewFont", selected[0]).apply()
                    }
                    .show()
            } catch (ex: Exception) {
                AppLog.e(activity, ex)
            }
        }

        private fun showMainAccentColorDialog() {
            try {
                val prefs = App.getInstance().preferences
                val string = prefs.getString("mainAccentColor", "pink")
                var position = -1
                when (string) {
                    AppPreferences.ACCENT_COLOR_PINK_NAME -> position = 0
                    AppPreferences.ACCENT_COLOR_BLUE_NAME -> position = 1
                    AppPreferences.ACCENT_COLOR_GRAY_NAME -> position = 2
                }
                val selected = intArrayOf(0)
                MaterialDialog.Builder(activity)
                    .title(R.string.pick_accent_color)
                    .items(
                        App.getContext().getString(R.string.blue),
                        App.getContext().getString(R.string.pink),
                        App.getContext().getString(R.string.gray)
                    )
                    .itemsCallbackSingleChoice(position) { _: MaterialDialog?, _: View?, which: Int, _: CharSequence? ->
                        selected[0] = which
                        true
                    }
                    .alwaysCallSingleChoiceCallback()
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancel)
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        when (selected[0]) {
                            0 -> {
                                prefs.edit().putString("mainAccentColor", AppPreferences.ACCENT_COLOR_PINK_NAME).apply()
                                if (!prefs.getBoolean("accentColorEdited", false)) {
                                    prefs.edit()
                                        .putInt("accentColor", Color.rgb(2, 119, 189))
                                        .putInt("accentColorPressed", Color.rgb(0, 89, 159))
                                        .apply()
                                }
                            }
                            1 -> {
                                prefs.edit().putString("mainAccentColor", AppPreferences.ACCENT_COLOR_BLUE_NAME).apply()
                                if (!prefs.getBoolean("accentColorEdited", false)) {
                                    prefs.edit()
                                        .putInt("accentColor", Color.rgb(233, 30, 99))
                                        .putInt("accentColorPressed", Color.rgb(203, 0, 69))
                                        .apply()
                                }
                            }
                            2 -> {
                                prefs.edit().putString("mainAccentColor", AppPreferences.ACCENT_COLOR_GRAY_NAME).apply()
                                if (!prefs.getBoolean("accentColorEdited", false)) {
                                    prefs.edit()
                                        .putInt("accentColor", Color.rgb(117, 117, 117))
                                        .putInt("accentColorPressed", Color.rgb(87, 87, 87))
                                        .apply()
                                }
                            }
                        }
                    }
                    .show()
            } catch (ex: Exception) {
                AppLog.e(activity, ex)
            }
        }

        private fun showAccentColorDialog() {
            try {
                val prefs = App.getInstance().preferences
                val prefColor =
                    prefs.getInt("accentColor", Color.rgb(2, 119, 189)).toString().toLong(10)
                        .toInt()
                //int prefColor = (int) Long.parseLong(String.valueOf(prefs.getInt("accentColor", Color.rgb(96, 125, 139))), 10);
                val colors = intArrayOf(
                    prefColor shr 16 and 0xFF,
                    prefColor shr 8 and 0xFF,
                    prefColor and 0xFF
                )
                val inflater =
                    (activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                val view = inflater.inflate(R.layout.color_editor, null as ViewGroup?)
                val redTxt = view.findViewById<EditText>(R.id.redText)
                val greenTxt = view.findViewById<EditText>(R.id.greenText)
                val blueTxt = view.findViewById<EditText>(R.id.blueText)
                val preview = view.findViewById<View>(R.id.preview)
                val red = view.findViewById<SeekBar>(R.id.red)
                val green = view.findViewById<SeekBar>(R.id.green)
                val blue = view.findViewById<SeekBar>(R.id.blue)
                redTxt.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "255"))
                greenTxt.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "255"))
                blueTxt.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "255"))
                redTxt.setText(colors[0].toString())
                greenTxt.setText(colors[1].toString())
                blueTxt.setText(colors[2].toString())
                red.progress = colors[0]
                green.progress = colors[1]
                blue.progress = colors[2]
                preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
                redTxt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {}
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (redTxt.text.toString() == "") {
                            colors[0] = 0
                        } else {
                            colors[0] = redTxt.text.toString().toInt()
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
                        red.progress = colors[0]
                        redTxt.setSelection(redTxt.text.length)
                    }
                })
                greenTxt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {}
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (greenTxt.text.toString() == "") {
                            colors[1] = 0
                        } else {
                            colors[1] = greenTxt.text.toString().toInt()
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
                        green.progress = colors[1]
                        greenTxt.setSelection(greenTxt.text.length)
                    }
                })
                blueTxt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {}
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (blueTxt.text.toString() == "") {
                            colors[2] = 0
                        } else {
                            colors[2] = blueTxt.text.toString().toInt()
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
                        blue.progress = colors[2]
                        blueTxt.setSelection(blueTxt.text.length)
                    }
                })
                red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        redTxt.setText(progress.toString())
                        preview.setBackgroundColor(Color.rgb(progress, colors[1], colors[2]))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        colors[0] = seekBar.progress
                    }
                })
                green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        greenTxt.setText(progress.toString())
                        preview.setBackgroundColor(Color.rgb(colors[0], progress, colors[2]))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        colors[1] = seekBar.progress
                    }
                })
                blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        blueTxt.setText(progress.toString())
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], progress))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        colors[2] = seekBar.progress
                    }
                })
                MaterialDialog.Builder(activity)
                    .title(R.string.color)
                    .customView(view, true)
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancel)
                    .neutralText(R.string.reset)
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        val colorPressed =
                            intArrayOf(colors[0] - 30, colors[1] - 30, colors[2] - 30)
                        if (colorPressed[0] < 0) colorPressed[0] = 0
                        if (colorPressed[1] < 0) colorPressed[1] = 0
                        if (colorPressed[2] < 0) colorPressed[2] = 0
                        if (Color.rgb(
                                colors[0],
                                colors[1],
                                colors[2]
                            ) != prefs.getInt("accentColor", Color.rgb(2, 119, 189))
                        ) {
                            prefs.edit().putBoolean("accentColorEdited", true).apply()
                        }
                        prefs.edit()
                            .putInt("accentColor", Color.rgb(colors[0], colors[1], colors[2]))
                            .putInt(
                                "accentColorPressed",
                                Color.rgb(colorPressed[0], colorPressed[1], colorPressed[2])
                            )
                            .apply()
                    }
                    .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                        prefs.edit()
                            .putInt("accentColor", Color.rgb(2, 119, 189))
                            .putInt("accentColorPressed", Color.rgb(0, 89, 159))
                            .putBoolean(
                                "accentColorEdited",
                                false
                            ) //.putInt("accentColor", Color.rgb(96, 125, 139))
                            //.putInt("accentColorPressed", Color.rgb(76, 95, 109))
                            .apply()
                    }
                    .show()
            } catch (ex: Exception) {
                AppLog.e(activity, ex)
            }
        }

        /*private int PICK_IMAGE_REQUEST = 1;
        private void pickUserBackground() {

            try {
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("image/ *");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);


            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }

        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                App.getInstance().getPreferences()
                        .edit()
                        .putString("userBackground", uri.toString())
                        .commit();
            }
        }*/
        private fun showStylesDialog() {
            try {
                val currentValue = currentTheme
                val newStyleNames = ArrayList<CharSequence>()
                val newstyleValues = ArrayList<CharSequence>()
                getStylesList(activity, newStyleNames, newstyleValues)
                val selected = intArrayOf(newstyleValues.indexOf(currentValue))
                MaterialDialog.Builder(activity)
                    .title(R.string.app_theme)
                    .cancelable(true)
                    .items(*newStyleNames.toTypedArray())
                    .itemsCallbackSingleChoice(newstyleValues.indexOf(currentValue)) { _: MaterialDialog?, _: View?, i: Int, _: CharSequence? ->
                        selected[0] = i
                        true // allow selection
                    }
                    .alwaysCallSingleChoiceCallback()
                    .positiveText(getString(R.string.AcceptStyle))
                    .neutralText(getString(R.string.Information))
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        if (selected[0] == -1) {
                            Toast.makeText(
                                activity,
                                getString(R.string.ChooseStyle),
                                Toast.LENGTH_LONG
                            ).show()
                            return@onPositive
                        }
                        App.getInstance().preferences
                            .edit()
                            .putString("appstyle", newstyleValues[selected[0]].toString())
                            .apply()
                    }
                    .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                        if (selected[0] == -1) {
                            Toast.makeText(
                                activity,
                                getString(R.string.ChooseStyle),
                                Toast.LENGTH_LONG
                            ).show()
                            return@onNeutral
                        }
                        var stylePath = newstyleValues[selected[0]].toString()
                        stylePath = getThemeCssFileName(stylePath)
                        val xmlPath = stylePath.replace(".css", ".xml")
                        val cssStyle = CssStyle.parseStyle(activity, xmlPath)
                        if (!cssStyle.ExistsInfo) {
                            Toast.makeText(
                                activity,
                                getString(R.string.StyleDoesNotContainDesc),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@onNeutral
                        }

                        //dialogInterface.dismiss();
                        StyleInfoActivity.showStyleInfo(
                            activity,
                            newstyleValues[selected[0]].toString()
                        )
                    }
                    .show()
            } catch (ex: Exception) {
                AppLog.e(activity, ex)
            }
        }

        private fun showAbout() {
            val text = """
                <b>Неофициальный клиент для сайта <a href="https://www.4pda.ru">4pda.ru</a></b><br/><br/>
                <b>Автор: </b> Артём Слинкин aka <a href="https://4pda.ru/forum/index.php?showuser=236113">slartus</a><br/>
                <b>E-mail:</b> <a href="mailto:slartus+4pda@gmail.com">slartus+4pda@gmail.com</a><br/><br/>
                <b>Разработчик(v3.x): </b> Евгений Низамиев aka <a href="https://4pda.ru/forum/index.php?showuser=2556269">Radiation15</a><br/>
                <b>E-mail:</b> <a href="mailto:radiationx@yandex.ru">radiationx@yandex.ru</a><br/><br/>
                <b>Разработчик(v3.x):</b> Александр Тайнюк aka <a href="https://4pda.ru/forum/index.php?showuser=1726458">iSanechek</a><br/>
                <b>E-mail:</b> <a href="mailto:devuicore@gmail.com">devuicore@gmail.com</a><br/><br/>
                <b>Помощник разработчиков: </b> Алексей Шолохов aka <a href="https://4pda.ru/forum/index.php?showuser=96664">Морфий</a>
                <b>E-mail:</b> <a href="mailto:asolohov@gmail.com">asolohov@gmail.com</a><br/><br/>
                <b>Благодарности: </b> <br/>
                * <b><a href="https://4pda.ru/forum/index.php?showuser=1657987">__KoSyAk__</a></b> Иконка программы<br/>
                * <b>Пользователям 4pda</b> (тестирование, идеи, поддержка)
                <br/><br/>Copyright 2011-2016 Artem Slinkin <slartus@gmail.com>
                """.trimIndent().replace("4pda.ru", HostHelper.host)
            @Suppress("DEPRECATION")
            MaterialDialog.Builder(activity)
                .title(programFullName)
                .content(Html.fromHtml(text))
                .positiveText(R.string.ok)
                .show()
            //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            //textView.setTextSize(12);

            //textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        private fun pickRingtone(defaultSound: Uri) {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                App.getContext().getString(R.string.pick_audio)
            )
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultSound)
            if (activity != null) activity.startActivityForResult(
                intent,
                NOTIFIERS_SERVICE_SOUND_REQUEST_CODE
            )
        }

        private fun showTheme(themeId: String) {
            activity.finish()
            ThemeFragment.showTopicById(themeId)
        }

        private fun showShareIt() {
            val sendMailIntent = Intent(Intent.ACTION_SEND)
            sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Recomend))
            sendMailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.RecommendText))
            sendMailIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendMailIntent, getString(R.string.SendBy_)))
        }

        private fun showAboutHistory() {
            val sb = StringBuilder()
            try {
                val br = BufferedReader(
                    InputStreamReader(
                        App.getInstance().assets.open("history.txt"),
                        "UTF-8"
                    )
                )
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
            } catch (e: IOException) {
                AppLog.e(activity, e)
            }
            MaterialDialog.Builder(activity)
                .title(getString(R.string.ChangesHistory))
                .content(sb)
                .positiveText(R.string.ok)
                .show()
            //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            //textView.setTextSize(12);
        }

        private fun showCookiesDeleteDialog() {
            MaterialDialog.Builder(activity)
                .title(getString(R.string.ConfirmTheAction))
                .content(getString(R.string.SureDeleteFile))
                .cancelable(true)
                .positiveText(getString(R.string.Delete))
                .negativeText(getString(R.string.no))
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    try {
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

        private fun showSelectDirDialog() {
            val inflater = (activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            val view = inflater.inflate(R.layout.dir_select_dialog, null as ViewGroup?)
            val rbInternal = view.findViewById<RadioButton>(R.id.rbInternal)
            val rbExternal = view.findViewById<RadioButton>(R.id.rbExternal)
            val rbCustom = view.findViewById<RadioButton>(R.id.rbCustom)
            val txtPath = view.findViewById<EditText>(R.id.txtPath)
            txtPath.setText(Preferences.System.systemDir)
            val checkedChangeListener =
                CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                    if (b) {
                        when (compoundButton.id) {
                            rbInternal.id -> {
                                txtPath.setText(App.getInstance().filesDir.path)
                                txtPath.isEnabled = false
                            }
                            rbExternal.id -> {
                                try {
                                    txtPath.setText(
                                        App.getInstance().getExternalFilesDir(null)?.path
                                            ?: ""
                                    )
                                    txtPath.isEnabled = false
                                } catch (ex: Throwable) {
                                    AppLog.e(activity, ex)
                                }
                            }
                            rbCustom.id -> {
                                txtPath.isEnabled = true
                            }
                        }
                    }
                }
            rbInternal.setOnCheckedChangeListener(checkedChangeListener)
            rbExternal.setOnCheckedChangeListener(checkedChangeListener)
            rbCustom.setOnCheckedChangeListener(checkedChangeListener)
            MaterialDialog.Builder(activity)
                .title(R.string.path_to_data)
                .customView(view, true)
                .cancelable(true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    try {
                        var dir = txtPath.text.toString()
                        dir = dir.replace("/", File.separator)
                        FileUtils.checkDirPath(dir)
                        Preferences.System.systemDir = dir
                    } catch (ex: Throwable) {
                        AppLog.e(activity, ex)
                    }
                }
                .show()
        }

        companion object {
            private const val MY_INTENT_CLICK = 302
        }
    }

    public override fun onStop() {
        super.onStop()

        App.resStartNotifierServices()
        getInstance(App.getInstance()).reload()
    }

    companion object {
        val NOTIFIERS_SERVICE_SOUND_REQUEST_CODE = App.getInstance().uniqueIntValue
        private val appCookiesPath: String
            get() = Preferences.System.systemDir + "4pda_cookies"

        @JvmStatic
        val cookieFilePath: String
            get() {
                var res = App.getInstance().preferences.getString("cookies.path", "") ?: ""
                if (TextUtils.isEmpty(res)) res = appCookiesPath
                return res.replace("/", File.separator)
            }

        @JvmStatic
        fun getStylesList(
            context: Context,
            newStyleNames: ArrayList<CharSequence>,
            newstyleValues: ArrayList<CharSequence>
        ) {
            var xmlPath: String
            var cssStyle: CssStyle
            val styleNames = context.resources.getStringArray(R.array.appthemesArray)
            val styleValues = context.resources.getStringArray(R.array.appthemesValues)
            for (i in styleNames.indices) {
                var styleName: CharSequence = styleNames[i]
                val styleValue: CharSequence = styleValues[i]
                xmlPath = getThemeCssFileName(styleValue.toString()).replace(".css", ".xml")
                    .replace("/android_asset/", "")
                cssStyle = CssStyle.parseStyleFromAssets(context, xmlPath)
                if (cssStyle.ExistsInfo) styleName = cssStyle.Title
                newStyleNames.add(styleName)
                newstyleValues.add(styleValue)
            }
            val file = File(Preferences.System.systemDir + "styles/")
            getStylesList(newStyleNames, newstyleValues, file)
        }

        private fun getStylesList(
            newStyleNames: ArrayList<CharSequence>,
            newstyleValues: ArrayList<CharSequence>, file: File
        ) {
            var cssPath: String
            var xmlPath: String
            var cssStyle: CssStyle
            if (file.exists()) {
                val cssFiles = file.listFiles() ?: return
                for (cssFile in cssFiles) {
                    if (cssFile.isDirectory) {
                        getStylesList(newStyleNames, newstyleValues, cssFile)
                        continue
                    }
                    cssPath = cssFile.path
                    if (!cssPath.toLowerCase(Locale.getDefault()).endsWith(".css")) continue
                    xmlPath = cssPath.replace(".css", ".xml")
                    cssStyle = CssStyle.parseStyleFromFile(xmlPath)
                    val title = cssStyle.Title
                    newStyleNames.add(title)
                    newstyleValues.add(cssPath)
                }
            }
        }

        @JvmStatic
        val packageInfo: PackageInfo
            get() {
                val packageName = App.getInstance().packageName
                try {
                    return App.getInstance().packageManager.getPackageInfo(
                        packageName, PackageManager.GET_META_DATA
                    )
                } catch (e1: PackageManager.NameNotFoundException) {
                    AppLog.e(App.getInstance(), e1)
                }
                val packageInfo = PackageInfo()
                packageInfo.packageName = packageName
                packageInfo.versionName = "unknown"
                packageInfo.versionCode = 1
                return packageInfo
            }
        val programFullName: String
            get() {
                var programName = App.getInstance().getString(R.string.app_name)
                val pInfo = packageInfo
                programName += " v" + pInfo.versionName + " c" + pInfo.versionCode
                return programName
            }
    }
}