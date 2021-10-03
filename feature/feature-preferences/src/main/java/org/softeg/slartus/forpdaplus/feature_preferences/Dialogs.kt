package org.softeg.slartus.forpdaplus.feature_preferences

import android.content.Context
import android.text.InputType
import android.view.View
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaplus.core_ui.AppColors
import timber.log.Timber

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
}