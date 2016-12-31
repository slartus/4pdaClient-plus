package org.softeg.slartus.forpdaplus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by isanechek on 28.12.16.
 */

public class NewYear extends Activity {

    public static void check(Context ctx) {

        if (checkDate() & !Preferences.isNYDone()) {
            Intent intent = new Intent(ctx, NewYear.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(intent);
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_yaer);
        WebView webView = (WebView) findViewById(R.id.ny_webview);
        webView.loadUrl("file:///android_asset/newyear/index.html");
        Preferences.NYDone();

    }


    private static boolean checkDate() {

        String startDate = "1/1/2017";
        String endDate = "8/1/2017";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date strDate, enDate = null;
        try {
            strDate = sdf.parse(startDate);
            enDate = sdf.parse(endDate);
            long time = System.currentTimeMillis();
            if (time >= strDate.getTime() & time < enDate.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}
