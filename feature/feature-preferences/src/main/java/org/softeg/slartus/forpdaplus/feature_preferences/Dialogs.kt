package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.Html
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.ExternalStorage
import org.softeg.slartus.forpdacommon.FileUtils
import org.softeg.slartus.forpdacommon.appFullName
import org.softeg.slartus.forpdaplus.core_ui.AppColors
import org.softeg.slartus.forpdaplus.core_ui.CssStyles
import org.softeg.slartus.hosthelper.HostHelper
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object Dialogs {

    fun showMainAccentColorDialog(context: Context) {
        try {
            val string = Preferences.Common.Overall.mainAccentColor
            val mainAccentColors = listOf(AppColors.pink, AppColors.blue, AppColors.gray)
            val position = mainAccentColors.indexOfFirst { c -> c.name == string }
            var selected = position
            MaterialDialog.Builder(context)
                .title(R.string.pick_accent_color)
                .items(
                    context.getString(R.string.pink),
                    context.getString(R.string.blue),
                    context.getString(R.string.gray)
                )
                .itemsCallbackSingleChoice(position) { _: MaterialDialog?, _: View?, which: Int, _: CharSequence? ->
                    selected = which
                    true
                }
                .alwaysCallSingleChoiceCallback()
                .positiveText(R.string.accept)
                .negativeText(R.string.cancel)
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    val selectedColor = mainAccentColors[selected]
                    Preferences.Common.Overall.mainAccentColor = selectedColor.name
                    if (!Preferences.Common.Overall.accentColorEdited) {
                        Preferences.Common.Overall.accentColor = selectedColor.color
                        Preferences.Common.Overall.accentColorPressed = selectedColor.pressedColor
                    }
                }
                .show()
        } catch (ex: Exception) {
            Timber.e(ex, "showMainAccentColorDialog")
        }
    }

    fun webViewFontDialog(context: Context) {
        try {
            var selected = Preferences.Common.Overall.webViewFont
            var name = ""
            var dialogShowed = false
            MaterialDialog.Builder(context)
                .title(R.string.choose_font)
                .items(
                    context.getString(R.string.font_from_style),
                    context.getString(R.string.system_font),
                    context.getString(R.string.enter_font_name)
                )
                .itemsCallbackSingleChoice(selected) { _: MaterialDialog?, _: View?, which: Int, _: CharSequence? ->
                    selected = which
                    when (which) {
                        0 -> name = ""
                        1 -> name = "inherit"
                        2 -> {
                            if (dialogShowed) return@itemsCallbackSingleChoice true
                            dialogShowed = true
                            MaterialDialog.Builder(context)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .input(
                                    context.getString(R.string.font_name),
                                    Preferences.Common.Overall.webViewFontName
                                ) { _: MaterialDialog?, input: CharSequence ->
                                    name = input.toString()
                                }
                                .positiveText(R.string.ok)
                                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                                    Preferences.Common.Overall.webViewFontName = name
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
                    Preferences.Common.Overall.webViewFontName = name
                    Preferences.Common.Overall.webViewFont = selected
                }
                .show()
        } catch (ex: Exception) {
            Timber.e(ex, "webViewFontDialog")
        }
    }

    fun showStylesDialog(context: Context) {
        try {
            val currentValue = Preferences.Common.Overall.appStyle
            val newStyleNames = ArrayList<CharSequence>()
            val newStyleValues = ArrayList<CharSequence>()
            CssStyles.getStylesList(
                context,
                Preferences.System.systemDir,
                newStyleNames,
                newStyleValues
            )
            var selected = newStyleValues.indexOf(currentValue)
            MaterialDialog.Builder(context)
                .title(R.string.app_theme)
                .cancelable(true)
                .items(*newStyleNames.toTypedArray())
                .itemsCallbackSingleChoice(newStyleValues.indexOf(currentValue)) { _: MaterialDialog?, _: View?, i: Int, _: CharSequence? ->
                    selected = i
                    true // allow selection
                }
                .alwaysCallSingleChoiceCallback()
                .positiveText(context.getString(R.string.AcceptStyle))
                //.neutralText(context.getString(R.string.Information))
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    if (selected == -1) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.ChooseStyle),
                            Toast.LENGTH_LONG
                        ).show()
                        return@onPositive
                    }
                    Preferences.Common.Overall.appStyle = newStyleValues[selected].toString()
                }
//                .onNeutral { _: MaterialDialog?, _: DialogAction? ->
//                    if (selected[0] == -1) {
//                        Toast.makeText(
//                            context,
//                            context.getString(R.string.ChooseStyle),
//                            Toast.LENGTH_LONG
//                        ).show()
//                        return@onNeutral
//                    }
//                    var stylePath = newStyleValues[selected[0]].toString()
//                    stylePath = AppTheme.getThemeCssFileName(stylePath)
//                    val xmlPath = stylePath.replace(".css", ".xml")
//                    val cssStyle = CssStyle.parseStyle(context, xmlPath)
//                    if (!cssStyle.ExistsInfo) {
//                        Toast.makeText(
//                            context,
//                            context.getString(R.string.StyleDoesNotContainDesc),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        return@onNeutral
//                    }
//
//                    //dialogInterface.dismiss();
////                    StyleInfoActivity.showStyleInfo(
////                        activity,
////                        newStyleValues[selected[0]].toString()
////                    )
//                }
                .show()
        } catch (ex: Exception) {
            Timber.e(ex, "showStylesDialog")
        }
    }

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

    fun showAbout(context: Context) {
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
        MaterialDialog.Builder(context)
            .title(context.appFullName)
            .content(Html.fromHtml(text))
            .positiveText(R.string.ok)
            .show()
        //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        //textView.setTextSize(12);

        //textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    fun showAboutHistory(context: Context) {
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
            Timber.e(e)
        }
        MaterialDialog.Builder(context)
            .title(context.getString(R.string.ChangesHistory))
            .content(sb)
            .positiveText(R.string.ok)
            .show()
        //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        //textView.setTextSize(12);
    }

    fun showShareIt(context: Context) {
        val sendMailIntent = Intent(Intent.ACTION_SEND)
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.Recomend))
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.RecommendText))
        sendMailIntent.type = "text/plain"
        context.startActivity(
            Intent.createChooser(
                sendMailIntent,
                context.getString(R.string.SendBy_)
            )
        )
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