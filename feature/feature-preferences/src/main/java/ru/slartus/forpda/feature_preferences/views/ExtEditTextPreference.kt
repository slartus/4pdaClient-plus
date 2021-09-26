package ru.slartus.forpda.feature_preferences.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.EditTextPreference
import org.softeg.slartus.forpdacommon.simplifyNumber
import ru.slartus.forpda.feature_preferences.App
import ru.slartus.forpda.feature_preferences.R
import ru.slartus.forpda.feature_preferences.getAttr
import ru.slartus.forpda.feature_preferences.preferences

class ExtEditTextPreference @JvmOverloads constructor(
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
    private enum class InputType {
        Number, NumberDecimal
    }

    private var inputType = InputType.Number
    private var defaultValue: String? = null
    private var defaultSummary: CharSequence? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.ExtEditTextPreference)
        try {
            when (a?.getInt(R.styleable.ExtEditTextPreference_myInputType, -1) ?: -1) {
                0 -> inputType = InputType.Number
                1 -> inputType = InputType.NumberDecimal
            }
            defaultValue = a?.getString(R.styleable.ExtEditTextPreference_appDefaultValue)
        } finally {
            a?.recycle()
        }
        defaultSummary = summary
        summaryProvider = SimpleSummaryProvider.instance
    }

    override fun getText(): String {
        return getPersistedString(defaultValue!!)
    }

    override fun getPersistedString(defaultReturnValuex: String?): String {
        val defaultReturnValue: String? = defaultValue
        val result: String? = try {
            super.getPersistedString(defaultReturnValue)
        } catch (ex: Throwable) {
            when (inputType) {
                InputType.Number -> getPersistedInt(defaultReturnValue?.toInt() ?: 0).toString()
                InputType.NumberDecimal -> getPersistedFloat(
                    defaultReturnValue?.toFloat()
                        ?: 0f
                ).toString()
            }
        }
        return result?.simplifyNumber() ?: ""
    }

    override fun getPersistedFloat(defaultReturnValue: Float): Float {
        return try {
            super.getPersistedFloat(defaultReturnValue)
        } catch (ex: Throwable) {
            getPersistedString(defaultReturnValue.toString()).toFloat()
        }
    }

    override fun persistFloat(value: Float): Boolean {
        if (shouldPersist()) {
            if (value == getPersistedFloat(Float.NaN)) {
                // It's already there, so the same as persisting
                return true
            }
            val editor = preferenceManager.sharedPreferences.edit()
            editor.putFloat(key, value)
            editor.apply()
            return true
        }
        return false
    }

    override fun persistString(value: String): Boolean {
        try {
            return when (inputType) {
                InputType.Number -> persistInt(value.toInt())
                InputType.NumberDecimal -> {
                    val fvalue = value.toFloat()
                    persistFloat(fvalue)
                }
            }
        } catch (ex: Throwable) {
            Toast.makeText(context, R.string.invalid_number_format, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    class SimpleSummaryProvider private constructor() : SummaryProvider<ExtEditTextPreference> {
        override fun provideSummary(preference: ExtEditTextPreference?): CharSequence {
            return getTextValue(preference)
        }

        private fun getTextValue(preference: ExtEditTextPreference?): CharSequence {
            return when (preference?.inputType) {
                InputType.Number -> App.getInstance().preferences.getInt(
                    preference.key,
                    preference.defaultValue?.toInt() ?: 0
                ).toString()
                InputType.NumberDecimal -> App.getInstance().preferences.getFloat(
                    preference.key,
                    preference.defaultValue?.toFloat() ?: 0f
                ).toString()
                else -> ""
            }
        }

        companion object {
            private var sSimpleSummaryProvider: SimpleSummaryProvider? = null

            val instance: SimpleSummaryProvider?
                get() {
                    if (sSimpleSummaryProvider == null) {
                        sSimpleSummaryProvider = SimpleSummaryProvider()
                    }
                    return sSimpleSummaryProvider
                }
        }

    }
}