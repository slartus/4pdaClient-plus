package org.softeg.slartus.forpdaplus.classes;

import android.os.Build;

/**
 * Created by slinkin on 14.01.14.
 */
public class Devices {
    public static boolean isAsus_EePad_TF300TG() {
        if (!"asus".equalsIgnoreCase(Build.BRAND))
            return false;
        if (!"EeePad".equalsIgnoreCase(Build.BOARD))
            return false;
        return "TF300TG".equalsIgnoreCase(Build.DEVICE);
    }

    public static boolean isNookSimpleTouch() {
        if (!"nook".equalsIgnoreCase(Build.BRAND))
            return false;
        if (!"zoom2".equalsIgnoreCase(Build.BOARD))
            return false;
        return "zoom2".equalsIgnoreCase(Build.DEVICE);
    }

    public static final int NOOK_SIMPLE_TOUCH_RIGHT_NEXTPAGE = 407;
    public static final int NOOK_SIMPLE_TOUCH_RIGHT_PREVPAGE = 158;
}
