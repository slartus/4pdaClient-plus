package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import timber.log.Timber
import java.io.File
import java.util.*

object Dialogs {



//    fun selectedMenuItemsDialog() {
//        val preferences = preferenceManager.sharedPreferences
//        var items = (preferences.getString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS)
//            ?: ListCore.DEFAULT_MENU_ITEMS).split(",".toRegex()).toTypedArray()
//        val allItems = ListCore.getAllMenuBricks()
//        if (ListCore.checkIndex(items, allItems.size)) {
//            items = ListCore.DEFAULT_MENU_ITEMS.split(",".toRegex()).toTypedArray()
//        }
//        val selectedItems = arrayOfNulls<Int>(items.size)
//        for (i in items.indices) selectedItems[i] = items[i].toInt()
//        val namesArray = ArrayList<String>()
//        for (item in allItems) namesArray.add(item.title)
//        val finalItems = Array<Array<Int?>?>(1) { arrayOfNulls(1) }
//        finalItems[0] = selectedItems
//        MaterialDialog.Builder(requireContext())
//            .title(R.string.select_items)
//            .items(*namesArray.toTypedArray<CharSequence>())
//            .itemsCallbackMultiChoice(selectedItems) { _: MaterialDialog?, integers: Array<Int?>?, _: Array<CharSequence?>? ->
//                finalItems[0] = integers
//                true
//            }
//            .alwaysCallMultiChoiceCallback()
//            .positiveText(R.string.accept)
//            .onPositive { _: MaterialDialog?, _: DialogAction? ->
//                if (finalItems.first()?.size ?: 0 == 0) return@onPositive
//                preferences.edit().putString(
//                    "selectedMenuItems",
//                    Arrays.toString(finalItems[0]).replace(" ", "").replace("[", "")
//                        .replace("]", "")
//                ).apply()
//            }
//            .neutralText(R.string.reset)
//            .onNeutral { _: MaterialDialog?, _: DialogAction? ->
//                preferences.edit().putString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS)
//                    .apply()
//            }
//            .show()
//    }

    fun showSelectDirDialog(context: Context) {
        val inflater =
            (context.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
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
                                Timber.e(ex, "showSelectDirDialog")
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
        MaterialDialog.Builder(context)
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
                    Timber.e(ex)
                }
            }
            .show()
    }


    fun showBackupNotesBackupDialog(context: Context, dataBasePath: String) {
        try {
            val dbFile = File(dataBasePath)
            if (!dbFile.exists()) {
                AlertDialog.Builder(context)
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

}