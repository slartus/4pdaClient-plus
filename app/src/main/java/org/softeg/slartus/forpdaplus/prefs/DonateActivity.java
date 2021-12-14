package org.softeg.slartus.forpdaplus.prefs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;


/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 18.10.12
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class DonateActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commitAllowingStateLoss();
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.donate_prefs, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

    }
    public static void setDonateClickListeners(final PreferenceFragment fragment) {

        fragment.findPreference("Qiwi").setOnPreferenceClickListener(preference -> {
            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://my.qiwi.com/Artem-Sy0u6mkcdM"));
            fragment.startActivity(Intent.createChooser(marketIntent, fragment.getString(R.string.Choice)));
            return true;
        });

        fragment.findPreference("Yandex.money").setOnPreferenceClickListener(preference -> {
            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://money.yandex.ru/to/41001491859942"));
            fragment.startActivity(Intent.createChooser(marketIntent, fragment.getString(R.string.Choice)));
            return true;
        });

        fragment.findPreference("WebMoney.moneyZ").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "Z188582160272");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("WebMoney.moneyR").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "R391199896701");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("WebMoney.moneyU").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "U177333629317");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Paypal.money").setOnPreferenceClickListener(preference -> {
            String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=slartus%40gmail%2ecom&lc=RU&item_name=slartus&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest";
            IntentActivity.showInDefaultBrowser(fragment.getActivity(), url);
            return true;
        });

//        fragment.findPreference("GooglePlay").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(fragment.getActivity(), DonateMarketActivity.class);
//                fragment.startActivity(intent);
//                return true;
//            }
//        });
//
//        fragment.findPreference("donate.other").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                ExtUrl.showInBrowser(fragment.getActivity(),"http://softeg.org/donate");
//                return true;
//            }
//        });


        fragment.findPreference("Morfiy.WebMoney.moneyB").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "B266066430353");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Morfiy.WebMoney.moneyU").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "U376942372846");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Morfiy.WebMoney.moneyE").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "E300106725068");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Morfiy.WebMoney.moneyR").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "R343791846131");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Morfiy.WebMoney.moneyZ").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "Z349073483817");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
            return true;
        });

        fragment.findPreference("Radiation.Yandex.money").setOnPreferenceClickListener(preference -> {
            StringUtils.copyToClipboard(fragment.getActivity(), "410012865124764");
            Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.DonateAccountNimberCopied), Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
