package org.softeg.slartus.forpdaplus.core_ui.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import org.softeg.slartus.forpdaplus.core_ui.R

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {
    private var title: String? = null

    override fun setPreferenceScreen(preferenceScreen: PreferenceScreen?) {
        super.setPreferenceScreen(preferenceScreen)
        preferenceScreen?.setGroupIconSpaceReserved(false)
        title = preferenceScreen?.title?.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TITLE, title)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = title ?: getString(R.string.settings)
    }

    companion object {
        private const val KEY_TITLE = "BasePreferenceFragment.title"
        private fun PreferenceGroup.setGroupIconSpaceReserved(iconSpaceReserved: Boolean) {
            for (i in 0 until preferenceCount) {
                val preference = getPreference(i)
                preference?.isIconSpaceReserved = false
                if (preference is PreferenceGroup) {
                    preference.setGroupIconSpaceReserved(iconSpaceReserved)
                }
            }
        }
    }
}