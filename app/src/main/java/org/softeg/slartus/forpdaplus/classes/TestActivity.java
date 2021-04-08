package org.softeg.slartus.forpdaplus.classes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.R;

/**
 * Created by slinkin on 18.12.13.
 */
public class TestActivity extends AppCompatActivity{
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
        new MaterialDialog.Builder(this)
                .title("title")
                .content("Message")
                .positiveText("OK").show();
    }
}
