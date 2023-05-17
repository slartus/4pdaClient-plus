package org.softeg.slartus.forpdaplus.prefs

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.AppTheme.getColorAccent
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.classes.InputFilterMinMax
import org.softeg.slartus.forpdaplus.common.AppLog
import ru.softeg.slartus.common.api.AppAccentColorType

fun showAccentColorDialog(context: Context) {
    try {
        val prefs = App.getInstance().preferences
        val prefColor = getColorAccent(AppAccentColorType.Accent)
        //int prefColor = (int) Long.parseLong(String.valueOf(prefs.getInt("accentColor", Color.rgb(96, 125, 139))), 10);
        val colors = intArrayOf(
            prefColor shr 16 and 0xFF,
            prefColor shr 8 and 0xFF,
            prefColor and 0xFF
        )
        val inflater =
            (context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val view = inflater.inflate(R.layout.color_editor, null as ViewGroup?)
        val redTxt = view.findViewById<EditText>(R.id.redText)
        val greenTxt = view.findViewById<EditText>(R.id.greenText)
        val blueTxt = view.findViewById<EditText>(R.id.blueText)
        val preview = view.findViewById<View>(R.id.preview)
        val red = view.findViewById<SeekBar>(R.id.red)
        val green = view.findViewById<SeekBar>(R.id.green)
        val blue = view.findViewById<SeekBar>(R.id.blue)
        redTxt.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        greenTxt.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
        blueTxt.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 255))
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
        MaterialDialog.Builder(context)
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
        AppLog.e(context, ex)
    }
}