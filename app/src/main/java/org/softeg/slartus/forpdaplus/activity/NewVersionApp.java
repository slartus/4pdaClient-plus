package org.softeg.slartus.forpdaplus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.List;

/**
 * Created by isanechek on 4/9/18.
 */

public class NewVersionApp extends AppCompatActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, NewVersionApp.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_four_pda_version);

        Preferences.newForPdaShowDone();

        findViewById(R.id.new_app_close_btn)
                .setOnClickListener(v -> {
                    startActivity(new Intent(NewVersionApp.this, MainActivity.class));
                    finish();
                });
        findViewById(R.id.new_app_open_gp_btn).setOnClickListener((View v) -> {
            if (isGpInstalled()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.forpdateam.forpda")));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.forpdateam.forpda")));
            }
            finish();
        });
        findViewById(R.id.new_app_open_theme_btn).setOnClickListener(v -> {
            IntentActivity.showTopic("http://4pda.ru/forum/index.php?showtopic=820313");
            finish();
        });
    }

    private boolean isGpInstalled() {
        PackageManager packageManager = getApplication().getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        boolean yes = false;
        for (PackageInfo info : packages) {
            if (info.packageName.equals("com.google.market") || info.packageName.equals("com.android.vending")) {
                yes = true;
                break;
            }
        }
        return yes;
    }
}