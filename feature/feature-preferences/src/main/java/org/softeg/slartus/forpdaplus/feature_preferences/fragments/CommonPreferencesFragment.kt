package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.FilePath
import org.softeg.slartus.forpdaplus.feature_preferences.App
import org.softeg.slartus.forpdaplus.feature_preferences.Dialogs
import org.softeg.slartus.forpdaplus.feature_preferences.R
import org.softeg.slartus.forpdaplus.feature_preferences.preferences

@Suppress("unused")
class CommonPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.common_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "appstyle" -> {
                Dialogs.showStylesDialog(requireContext())
                return true
            }
            "accentColor" -> {
                showAccentColorDialog()
                return true
            }
            "mainAccentColor" -> {
                Dialogs.showMainAccentColorDialog(requireContext())
                return true
            }
            "webViewFont" -> {
                Dialogs.webViewFontDialog(requireContext())
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
                if (selectedImagePath != null) App.getInstance().preferences
                    .edit()
                    .putString("userInfoBg", selectedImagePath)
                    .putBoolean("isUserBackground", true)
                    .apply() else Toast.makeText(
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
                App.getInstance().preferences
                    .edit()
                    .putString("userInfoBg", "")
                    .putBoolean("isUserBackground", false)
                    .apply()
            }
            .show()
    }

}