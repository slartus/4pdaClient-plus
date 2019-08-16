package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.content.SharedPreferences;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.DateExtensions;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BuildConfig;

import java.util.Date;
import java.util.GregorianCalendar;

/*
 * Created by slartus on 03.06.2014.
 */
public abstract class MainNotifier {
    private NotifiersManager notifiersManager;
    protected String name;
    private int period;

    protected static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
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
        if(period==0)
            return true;
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
