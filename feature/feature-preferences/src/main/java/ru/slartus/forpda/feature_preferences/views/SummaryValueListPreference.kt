package ru.slartus.forpda.feature_preferences.views

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class SummaryValueListPreference(context: Context?, attrs: AttributeSet?) :
    ListPreference(context, attrs) {

    init {
        summaryProvider = SimpleSummaryProvider.instance
    }

    class SimpleSummaryProvider private constructor() : SummaryProvider<ListPreference> {
        override fun provideSummary(preference: ListPreference?): CharSequence {
            return getTextValue(preference)
        }

        private fun getTextValue(preference: ListPreference?): CharSequence {
            val ind = preference?.entryValues?.indexOf(preference.value) ?: -1
            return if (ind == -1) "" else preference?.entries?.get(ind) ?: ""
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