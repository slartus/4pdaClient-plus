package ru.slartus.forpda.feature_preferences.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.EditTextPreference
import org.softeg.slartus.forpdacommon.simplifyNumber
import ru.slartus.forpda.feature_preferences.App
import ru.slartus.forpda.feature_preferences.R
import ru.slartus.forpda.feature_preferences.preferences

/*
 * Created by slinkin on 15.08.13.
 */
class ExtEditTextPreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
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
        setCurrentSummary()
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
        return result!!.simplifyNumber()
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

    private fun setOwnSummary(summary: CharSequence) {
        if (getSummary() != summary) {
            setSummary(summary)
        }
    }

    override fun setText(text: String?) {
        val prevValue = getText()
        super.setText(text)
        if (text != prevValue)
            setCurrentSummary()
    }

    private fun setCurrentSummary() {
        val value: String = when (inputType) {
            InputType.Number -> App.instance().preferences.getInt(
                key,
                defaultValue!!.toInt()
            ).toString()
            InputType.NumberDecimal -> App.instance().preferences.getFloat(
                key,
                defaultValue!!.toFloat()
            ).toString()
        }
        setOwnSummary(String.format(defaultSummary.toString(), value.simplifyNumber()))
    }
}