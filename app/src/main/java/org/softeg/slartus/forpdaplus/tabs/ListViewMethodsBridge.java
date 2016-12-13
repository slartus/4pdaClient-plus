package org.softeg.slartus.forpdaplus.tabs;

import android.content.Context;
import android.content.SharedPreferences;

import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdaplus.App;

/**
 * User: slinkin
 * Date: 21.11.11
 * Time: 8:03
 */
public class ListViewMethodsBridge {

    public static int getItemId(Context context, int i, long l) {
//        int sdk = new Integer(Build.VERSION.SDK).intValue();
//
//        if (sdk < 8) {
//            return getItemIdOld(i, l);
//        }
        SharedPreferences prefs = App.getInstance().getPreferences();

        return getItemId(i, l) + ExtPreferences.parseInt(prefs, "topics.list.offset", 0);
    }

    private static int getItemId(int i, long l) {
//        int sdk = new Integer(Build.VERSION.SDK).intValue();
//
//        if (sdk < 8) {
//            return getItemIdOld(i, l);
//        }

        return getItemIdNew(i, l);
    }

    private static int getItemIdOld(int i, long l) {
        return (int) l - 1;
    }

    private static int getItemIdNew(int i, long l) {
        return (int) l;
    }
}
