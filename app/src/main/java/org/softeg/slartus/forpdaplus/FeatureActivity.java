package org.softeg.slartus.forpdaplus;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by radiationx on 16.01.16.
 */
public class FeatureActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(AppTheme.getThemeStyleResID());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_activity);
    }
}
