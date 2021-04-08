package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.DonateActivity;

/*
 * Created by slartus on 03.06.2014.
 */
public class DonateNotifier extends MainNotifier {
    public DonateNotifier(NotifiersManager notifiersManager) {
        super(notifiersManager, "Donate", 14*24);
    }

    public void start(FragmentActivity fragmentActivity) {
        if (!needShow())
            return;
        saveTime();
        showNotify(fragmentActivity);
        saveSettings();
    }

    private void showNotify(final FragmentActivity fragmentActivity) {
        try {
            addToStack(new MaterialDialog.Builder(fragmentActivity)
                    .title("Неофициальный 4pda клиент")
                    .content("Ваша поддержка - единственный стимул к дальнейшей разработке и развитию программы\n" +
                            "\n" +
                            "Вы можете сделать это позже через меню>>настройки>>Помочь проекту")
                    .positiveText("Помочь проекту")
                    .onPositive((dialog, which) -> {
                        Intent settingsActivity = new Intent(
                                fragmentActivity, DonateActivity.class);
                        fragmentActivity.startActivity(settingsActivity);
                    })
                    .negativeText("Позже").build());
        } catch (Throwable ex) {
            AppLog.e(fragmentActivity, ex);
        }

    }

    private boolean needShow() {
        SharedPreferences prefs = App.getInstance().getPreferences();
        String appVersion = getAppVersion();
        if (appVersion.toLowerCase().contains("beta")) return false;

        if (prefs.getString("DonateShowVer", "").equals(appVersion)) {
            if (!isTime()) return false;
        }
        prefs.edit().putString("DonateShowVer", appVersion).apply();
        return true;
    }

    private void saveSettings() {
        saveTime();
        App.getInstance().getPreferences().edit().putString("DonateShowVer", getAppVersion()).apply();
    }
}
