package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.DonateActivity;

/*
 * Created by slartus on 03.06.2014.
 */
public class DonateNotifier extends MainNotifier {
    public DonateNotifier() {
        super("Donate", 31);
    }

    public void start(FragmentActivity fragmentActivity){
        if(!needShow())
            return;
        saveTime();
        showNotify(fragmentActivity);
        saveSettings();
    }

    public void showNotify(FragmentActivity fragmentActivity) {
        try {
            DialogFragment dialogFragment = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    return new AlertDialogBuilder(getActivity())
                            .setTitle("Неофициальный 4pda клиент")
                            .setMessage("Ваша поддержка - единственный стимул к дальнейшей разработке и развитию программы\n" +
                                    "\n" +
                                    "Вы можете сделать это позже через меню>>настройки>>Помочь проекту")
                            .setPositiveButton("Помочь проекту..",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            Intent settingsActivity = new Intent(
                                                    getActivity(), DonateActivity.class);
                                            startActivity(settingsActivity);

                                        }
                                    }
                            )
                            .setNegativeButton("Позже",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();

                                        }
                                    }
                            ).create();
                }
            };
            dialogFragment.show(fragmentActivity.getSupportFragmentManager(), "dialog");
        } catch (Throwable ex) {
            Log.e(fragmentActivity, ex);
        }

    }

    protected boolean needShow() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        if (prefs.getBoolean("donate.DontShow", false)) return false;


        String appVersion = getAppVersion(MyApp.getContext());
        if (prefs.getString("DonateShowVer", "").equals(appVersion)) {
            if (!isTime()) return false;
        }
        prefs.edit().putString("DonateShowVer",appVersion).commit();
        return true;
    }

    protected void saveSettings() {
        saveTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("DonateShowVer", getAppVersion(MyApp.getContext()));
        editor.commit();
    }
}
