package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdacommon.InputFilterMinMax
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import org.softeg.slartus.forpdaplus.feature_preferences.R
import org.softeg.slartus.forpdaplus.feature_preferences.databinding.FragmentColorPickerBinding

class ColorPickerDialogFragment : DialogFragment() {
    private val colors = Preferences.Common.Overall.accentColor.let { prefColor ->
        intArrayOf(
            prefColor shr 16 and 0xFF,
            prefColor shr 8 and 0xFF,
            prefColor and 0xFF
        )
    }
    private var _binding: FragmentColorPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = FragmentColorPickerBinding.inflate(layoutInflater, null, false)
        val view = binding.root
        initEditor(binding.redText, binding.red, 0)
        initEditor(binding.greenText, binding.green, 1)
        initEditor(binding.blueText, binding.blue, 2)

        return MaterialDialog.Builder(requireContext())
            .title(R.string.color)
            .customView(view, true)
            .positiveText(R.string.accept)
            .negativeText(R.string.cancel)
            .neutralText(R.string.reset)
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
//                val colorPressed =
//                    intArrayOf(colors[0] - 30, colors[1] - 30, colors[2] - 30)
//                if (colorPressed[0] < 0) colorPressed[0] = 0
//                if (colorPressed[1] < 0) colorPressed[1] = 0
//                if (colorPressed[2] < 0) colorPressed[2] = 0
//                if (Color.rgb(
//                        colors[0],
//                        colors[1],
//                        colors[2]
//                    ) != prefs.getInt("accentColor", Color.rgb(2, 119, 189))
//                ) {
//                    prefs.edit().putBoolean("accentColorEdited", true).apply()
//                }
//                prefs.edit()
//                    .putInt("accentColor", Color.rgb(colors[0], colors[1], colors[2]))
//                    .putInt(
//                        "accentColorPressed",
//                        Color.rgb(colorPressed[0], colorPressed[1], colorPressed[2])
//                    )
//                    .apply()
            }
            .onNeutral { _: MaterialDialog?, _: DialogAction? ->
//                prefs.edit()
//                    .putInt("accentColor", Color.rgb(2, 119, 189))
//                    .putInt("accentColorPressed", Color.rgb(0, 89, 159))
//                    .putBoolean(
//                        "accentColorEdited",
//                        false
//                    ) //.putInt("accentColor", Color.rgb(96, 125, 139))
//                    //.putInt("accentColorPressed", Color.rgb(76, 95, 109))
//                    .apply()
            }.build()

    }

    private fun initEditor(editText: EditText, seekBar: SeekBar, colorIndex: Int) {
        editText.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        editText.addTextChangedListener(object : TextWatcher {
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
                if (editText.text.toString() == "") {
                    colors[colorIndex] = 0
                } else {
                    colors[colorIndex] = editText.text.toString().toInt()
                }
                binding.preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
                seekBar.progress = colors[colorIndex]
                editText.setSelection(editText.text.length)
            }
        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                editText.setText(progress.toString())
                colors[colorIndex] = seekBar.progress
                binding.preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}