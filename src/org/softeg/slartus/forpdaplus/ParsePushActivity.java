package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Артём on 01.05.14.
 */
public class ParsePushActivity extends BaseFragmentActivity {
    TextView textView;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setContentView(R.layout.parse_push_layout);


        textView = ((TextView) findViewById(R.id.text));
        try {
            tryShow(getIntent().getExtras().getString("com.parse.Data"));
        } catch (Throwable e) {
            finish();
        }
    }

    private void tryShow(String data) throws JSONException {

        JSONObject jsonObject = new JSONObject(data);
        textView.setText(jsonObject.getString("alert"));

    }
}
