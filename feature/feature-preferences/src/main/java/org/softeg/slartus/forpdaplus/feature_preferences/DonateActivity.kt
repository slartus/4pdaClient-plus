package org.softeg.slartus.forpdaplus.feature_preferences;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.softeg.slartus.forpdaplus.feature_preferences.fragments.DonatePreferencesFragment;


/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 18.10.12
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class DonateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new DonatePreferencesFragment())
                    .commitAllowingStateLoss();
        }
    }

}
