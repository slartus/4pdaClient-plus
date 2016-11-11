package org.softeg.slartus.forpdaplus.utils;

import android.util.DisplayMetrics;

import org.softeg.slartus.forpdaplus.App;

/**
 * Created by isanechek on 16.03.16.
 */
public class Utils {

    public static String getS(int id) {
        return App.getContext().getString(id);
    }

    public static int dpToPix(int dp, DisplayMetrics metrics) {
        return (dp * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }
}
