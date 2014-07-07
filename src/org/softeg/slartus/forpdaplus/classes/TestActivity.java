package org.softeg.slartus.forpdaplus.classes;

import android.os.Bundle;
import android.view.View;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.R;

/**
 * Created by slinkin on 18.12.13.
 */
public class TestActivity extends BaseFragmentActivity {
    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        setContentView(R.layout.test_activity);
        findViewById(R.id.btnShowAlertDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlerDialog();
            }
        });
        showAlerDialog();
    }

    private void showAlerDialog() {
        new AlertDialogBuilder(this)
                .setTitle("title")
                .setMessage("Message")
                .setPositiveButton("OK", null).create().show();
    }
}
