package org.softeg.slartus.forpdacommon;

import java.util.Date;

public class DateExtensions {

    public static int getHoursBetween(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime())
                / (1000 * 60 * 60));
    }
}
