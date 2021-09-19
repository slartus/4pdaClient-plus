package org.softeg.slartus.forpdaplus.controls

import android.content.Context
import org.softeg.slartus.forpdacommon.simplifyNumber
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.res.TypedArray
import org.softeg.slartus.forpdaplus.R
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.EditTextPreference
import org.softeg.slartus.forpdaplus.App

/*
 * Created by slinkin on 15.08.13.
 */
class ExtEditTextPreference : EditTextPreference {
    private enum class InputType {
        Number, NumberDecimal
    }

    private var m_InputType = InputType.Number
    private var m_DefaultValue: String? = null
    private var m_DefaultSummary: CharSequence? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ExtEditTextPreference)
        try {
            val i = a.getInt(R.styleable.ExtEditTextPreference_myInputType, -1)
            when (i) {
                0 -> m_InputType = InputType.Number
                1 -> m_InputType = InputType.NumberDecimal
            }
            m_DefaultValue = a.getString(R.styleable.ExtEditTextPreference_appDefaultValue)
        } finally {
            a.recycle()
        }
        m_DefaultSummary = summary
        setCurrentSummary()
    }

    override fun getText(): String {
        return getPersistedString(m_DefaultValue!!)
    }

    override fun getPersistedString(defaultReturnValue: String): String {
        var defaultReturnValue: String? = defaultReturnValue
        defaultReturnValue = m_DefaultValue
        var result = defaultReturnValue
        result = try {
            super.getPersistedString(defaultReturnValue)
        } catch (ex: Throwable) {
            when (m_InputType) {
                InputType.Number -> getPersistedInt(defaultReturnValue?.toInt() ?: 0).toString()
                InputType.NumberDecimal -> getPersistedFloat(defaultReturnValue?.toFloat()
                        ?: 0f).toString()
            }
        }
        return result!!.simplifyNumber()
    }

    override fun getPersistedFloat(defaultReturnValue: Float): Float {
        return try {
            super.getPersistedFloat(defaultReturnValue)
        } catch (ex: Throwable) {
            getPersistedString(java.lang.Float.toString(defaultReturnValue)).toFloat()
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
            return when (m_InputType) {
                InputType.Number -> persistInt(value.toInt())
                InputType.NumberDecimal -> {
                    val fvalue = value.toFloat()
                    persistFloat(fvalue)
                }
            }
            return true
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
        var value = ""
        value = when (m_InputType) {
            InputType.Number -> Integer.toString(App.getInstance().preferences.getInt(key, m_DefaultValue!!.toInt()))
            InputType.NumberDecimal -> java.lang.Float.toString(App.getInstance().preferences.getFloat(key, m_DefaultValue!!.toFloat()))
        }
        setOwnSummary(String.format(m_DefaultSummary.toString(), value.simplifyNumber()))
    }
}