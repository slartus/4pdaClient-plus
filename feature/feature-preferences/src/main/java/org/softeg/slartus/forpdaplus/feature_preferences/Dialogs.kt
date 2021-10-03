package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaplus.core_ui.AppColors
import org.softeg.slartus.forpdaplus.core_ui.AppTheme
import org.softeg.slartus.forpdaplus.core_ui.CssStyles
import timber.log.Timber
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
                                ) { _: MaterialDialog?, input: CharSequence -> name = input.toString() }
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
            val currentValue = AppTheme.currentTheme
            val newStyleNames = ArrayList<CharSequence>()
            val newStyleValues = ArrayList<CharSequence>()
            CssStyles.getStylesList(
                context,
                Preferences.System.systemDir,
                newStyleNames,
                newStyleValues
            )
            val selected = intArrayOf(newStyleValues.indexOf(currentValue))
            MaterialDialog.Builder(context)
                .title(R.string.app_theme)
                .cancelable(true)
                .items(*newStyleNames.toTypedArray())
                .itemsCallbackSingleChoice(newStyleValues.indexOf(currentValue)) { _: MaterialDialog?, _: View?, i: Int, _: CharSequence? ->
                    selected[0] = i
                    true // allow selection
                }
                .alwaysCallSingleChoiceCallback()
                .positiveText(context.getString(R.string.AcceptStyle))
                //.neutralText(context.getString(R.string.Information))
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    if (selected[0] == -1) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.ChooseStyle),
                            Toast.LENGTH_LONG
                        ).show()
                        return@onPositive
                    }
                    Preferences.Common.Overall.appStyle = newStyleValues[selected[0]].toString()
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
}