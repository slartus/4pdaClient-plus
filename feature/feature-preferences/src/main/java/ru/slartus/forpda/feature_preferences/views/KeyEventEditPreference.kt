package ru.slartus.forpda.feature_preferences.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.preference.EditTextPreference
import ru.slartus.forpda.feature_preferences.R
import ru.slartus.forpda.feature_preferences.getAttr

class KeyEventEditPreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = context.getAttr(
        R.attr.editTextPreferenceStyle,
        android.R.attr.editTextPreferenceStyle
    ),
    defStyleRes: Int = 0
) : EditTextPreference(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    fun init() {
        setOnBindEditTextListener { editText: EditText? ->
            editText?.setOnKeyListener { _, _, keyEvent ->
                editText.setText(keyEvent?.keyCode?.toString())
                false
            };
        }
    }
}