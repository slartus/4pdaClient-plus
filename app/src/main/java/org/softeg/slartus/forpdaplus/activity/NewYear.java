package org.softeg.slartus.forpdaplus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.webkit.WebView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_yaer);
        WebView webView = findViewById(R.id.ny_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/newyear/index.html");
        Preferences.NYDone();

    }


    private static boolean checkDate() {
        int year = GregorianCalendar.getInstance().get(Calendar.YEAR);

        Calendar startDate = new GregorianCalendar(year, Calendar.JANUARY, 1);
        Calendar endDate = new GregorianCalendar(year, Calendar.JANUARY, 7);

        Calendar currentDate = GregorianCalendar.getInstance();
        return (startDate.compareTo(currentDate) * currentDate.compareTo(endDate)) >= 0;
    }
}
