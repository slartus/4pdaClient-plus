package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.FilePath
import org.softeg.slartus.forpdaplus.core_ui.AppColors
import org.softeg.slartus.forpdaplus.core_ui.CssStyles
import org.softeg.slartus.forpdaplus.feature_preferences.*
import timber.log.Timber
import java.util.ArrayList

@Suppress("unused")
class CommonPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.common_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "appstyle" -> {
                showStylesDialog(requireContext())
                return true
            }
            "accentColor" -> {
                showAccentColorDialog()
                return true
            }
            "mainAccentColor" -> {
                showMainAccentColorDialog(requireContext())
                return true
            }
            "webViewFont" -> {
                webViewFontDialog(requireContext())
                return true
            }
            "userBackground" -> {
                pickUserBackground()
                return true
            }
//
//            "visibleMenuItems" -> {
//                setMenuItems()
//                return true
//            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showAccentColorDialog() {
        val newFragment = ColorPickerDialogFragment()
        newFragment.show(childFragmentManager, "color_picker")
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result?.data?.data
                val selectedImagePath = FilePath.getPath(App.getInstance(), selectedImageUri)
                if (selectedImagePath != null) {
                    Preferences.Common.Overall.userInfoBg = selectedImagePath
                    Preferences.Common.Overall.isUserBackground = true
                } else Toast.makeText(
                    activity,
                    "Не могу прикрепить файл",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun pickUserBackground() {
        MaterialDialog.Builder(requireContext())
            .content(R.string.pick_image)
            .positiveText(R.string.choose)
            .negativeText(R.string.cancel)
            .neutralText(R.string.reset)
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                takePicture.launch(intent)
            }
            .onNeutral { _: MaterialDialog?, _: DialogAction? ->
                Preferences.Common.Overall.userInfoBg = ""
                Preferences.Common.Overall.isUserBackground = false
            }
            .show()
    }


    private fun showStylesDialog(context: Context) {
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


    private fun webViewFontDialog(context: Context) {
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


    private fun showMainAccentColorDialog(context: Context) {
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

}