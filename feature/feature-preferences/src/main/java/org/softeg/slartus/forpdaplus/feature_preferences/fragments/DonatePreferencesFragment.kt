package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import org.softeg.slartus.forpdacommon.StringUtils.copyToClipboard
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BasePreferenceFragment
import org.softeg.slartus.forpdaplus.feature_preferences.R

class DonatePreferencesFragment : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.donate_prefs, rootKey)
    }
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "Qiwi" -> {
                val marketIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://qiwi.me/aae4cc12-6926-4c0e-a171-bb98e2eb5cc0")
                )
                startActivity(
                    Intent.createChooser(
                        marketIntent,
                        getString(R.string.Choice)
                    )
                )
                return true
            }
            "Yandex.money" -> {
                val marketIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://money.yandex.ru/to/41001491859942")
                )
                startActivity(
                    Intent.createChooser(
                        marketIntent,
                        getString(R.string.Choice)
                    )
                )
                return true
            }
            "WebMoney.moneyZ" -> {
                copyToClipboard(
                    requireContext(),
                    "Z188582160272"
                )
                Toast.makeText(
                    activity,
                    getString(R.string.DonatePurseCopied),
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            "WebMoney.moneyR" -> {
                copyToClipboard(requireContext(), "R391199896701")
                Toast.makeText(
                    activity,
                    getString(R.string.DonatePurseCopied),
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            "WebMoney.moneyU" -> {
                val url =
                    "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=slartus%40gmail%2ecom&lc=RU&item_name=slartus&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest"
                openUrl(requireContext(), url)
                return true
            }
            "Morfiy.WebMoney.moneyB" -> {
                copyToClipboard(requireContext(), "B266066430353")
                Toast.makeText(activity, getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show()
                return true
            }

            "Morfiy.WebMoney.moneyU" -> {
                copyToClipboard(requireContext(), "U376942372846")
                Toast.makeText(activity, getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show()
                return true
            }
            "Morfiy.WebMoney.moneyE" -> {
                copyToClipboard(requireContext(), "E300106725068")
                Toast.makeText(activity, getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show()
                return true
            }
            "Morfiy.WebMoney.moneyR" -> {
                copyToClipboard(requireContext(), "R343791846131")
                Toast.makeText(activity, getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show()
                return true
            }
            "Morfiy.WebMoney.moneyZ" -> {
                copyToClipboard(requireContext(), "Z349073483817")
                Toast.makeText(activity, getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show()
                return true
            }
            "Radiation.Yandex.money" -> {
                copyToClipboard(requireContext(), "410012865124764")
                Toast.makeText(activity, getString(R.string.DonateAccountNimberCopied), Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}