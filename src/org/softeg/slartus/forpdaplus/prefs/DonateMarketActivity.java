package org.softeg.slartus.forpdaplus.prefs;

import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest;
import net.robotmedia.billing.helper.AbstractBillingActivity;
import net.robotmedia.billing.model.Transaction;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 28.10.12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class DonateMarketActivity extends AbstractBillingActivity implements BillingController.IConfiguration {

    private DonateMarketActivity getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.donate_market_prefs);

        findPreference("Billing.OneLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                getContext().requestPurchase("a897afdb29d442419495f23c02c3c7fb");
                return true;
            }
        });

        findPreference("Billing.ThreeLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("fc04b65225e44c348a73555e670c8bac");
                return true;
            }
        });

        findPreference("Billing.FiveLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("c2cde3bf38914ac999e888a08320de8f");
                return true;
            }
        });

        findPreference("Billing.TenLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("ffd80654e1e64afaaec2a30d2986819a");
                return true;
            }
        });
    }

    @Override
    public void onPurchaseStateChanged(String itemId, Transaction.PurchaseState state) {

    }

    @Override
    public void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {
        if (response == BillingRequest.ResponseCode.RESULT_OK)
            Toast.makeText(this, getString(R.string.ThanksForDonate), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingChecked(boolean supported) {

    }


    public byte[] getObfuscationSalt() {
        return new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};
    }

    public String getPublicKey() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm9Wk8JIPMlyADr5C9SYWRBDJ0wrD4ffVvcxbdo9USoRtCfyskN+6hISorpke3WEeSeR6gCT5wlSDBFRt1OOwDY1XV5i3P143PxrRqI6xSI0wQ0/IPQ3AkWky7ickB/BbHxdHlahv3bgm8lldTNFQ/wQEWXPU55tvOSB85VVkhSpLQaMPDqGWlrAYXNlpcYTpd0+oRd/S94U3IkK35Ti1olQnhvCFGB+jkucoESc6F05UGPGPzQUPk9iz7HvR9TqDZ58JEogLErLZT0q58Lgj4rW9hgRGTuy1IjHDB3P3wpKrCIjHyyVMJgLxvqWiVcwoWjdUi2XIpt78UHSyc7KxEQIDAQAB";
    }
}
