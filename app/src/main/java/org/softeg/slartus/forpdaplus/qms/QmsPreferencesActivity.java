package org.softeg.slartus.forpdaplus.qms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;



import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.BasePreferencesActivity;


/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 29.05.13
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */
public class QmsPreferencesActivity extends BasePreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.qms_prefs);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        App.reStartQmsService();

    }
}

