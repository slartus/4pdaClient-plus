package org.softeg.slartus.forpdaplus.controls

import android.content.Context
import org.softeg.slartus.forpdaplus.App
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.ListPreference
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils
import org.softeg.slartus.forpdaplus.common.AppLog

class SummaryValueListPreference(context: Context?, attrs: AttributeSet?) : ListPreference(context, attrs) {


    override fun setKey(key: String) {
        super.setKey(key)
        setCurrentSummary()
    }

    private fun getTextValue(value: String?): CharSequence {
        val ind = ArrayUtils.indexOf(value, this.entryValues)
        return if (ind == -1) "" else this.entries[ind]
    }

    override fun setValue(value: String) {
        val prevValue = getValue()
        super.setValue(value)
        if (value != prevValue)
            setCurrentSummary()
    }

    private fun setOwnSummary(summary: CharSequence) {
        if (getSummary() != summary) {
            setSummary(summary)
        }
    }

    private fun setCurrentSummary() {
        try {
            val value = App.getInstance().preferences.getString(key, null)
            if (TextUtils.isEmpty(value)) {
                var defValue: Any? = value
                if (defValue == null) defValue = ""
                val ind = findIndexOfValue(value)
                if (ind != -1) setValueIndex(ind)
                setOwnSummary(getTextValue(defValue.toString()))
                return
            }
            setOwnSummary(getTextValue(value))
            val ind = findIndexOfValue(value)
            if (ind != -1) setValueIndex(ind)
        } catch (ex: Throwable) {
            AppLog.toastE(context, ex)
        }
    }

    init {
        setCurrentSummary()
    }
}