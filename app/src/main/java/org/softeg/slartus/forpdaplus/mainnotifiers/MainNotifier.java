package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.DateExtensions;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.util.Date;
import java.util.GregorianCalendar;

/*
 * Created by slartus on 03.06.2014.
 */
public abstract class MainNotifier {
    private NotifiersManager notifiersManager;
    protected String name;
    protected int period;

    protected static String getAppVersion(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            AppLog.e(context, e1);
        }
        return "";
    }

    public void addToStack(MaterialDialog materialDialog){
        notifiersManager.addNotifyDialog(materialDialog);
    }

    public MainNotifier(NotifiersManager notifiersManager, String name, int period) {
        this.notifiersManager = notifiersManager;
        this.name = name;
        this.period = period;

    }

    public String getName(){
        return name;
    }

    protected boolean isTime() {
        GregorianCalendar lastShowpromoCalendar = new GregorianCalendar();
        SharedPreferences prefs = App.getInstance().getPreferences();
        Date lastCheckDate=ExtPreferences.getDateTime(prefs, "notifier." + name, null);
        if(lastCheckDate==null){
            saveTime();
            return true;
        }

        lastShowpromoCalendar.setTime(lastCheckDate);

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        int days = DateExtensions.getDaysBetween(calendar.getTime(), lastShowpromoCalendar.getTime());
        return days >= period;
    }

    protected void saveTime() {
        SharedPreferences.Editor editor = App.getInstance().getPreferences().edit();
        ExtPreferences.putDateTime(editor,  "notifier." + name, new Date());
        editor.apply();
    }
}
