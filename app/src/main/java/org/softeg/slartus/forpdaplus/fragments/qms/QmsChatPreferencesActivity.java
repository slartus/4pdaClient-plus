package org.softeg.slartus.forpdaplus.fragments.qms;

import android.os.Bundle;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.BasePreferencesActivity;

/**
 * User: slinkin
 * Date: 18.06.12
 * Time: 14:55
 */
public class QmsChatPreferencesActivity extends BasePreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qms_chat_prefs);
    }
}
