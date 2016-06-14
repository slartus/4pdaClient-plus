package org.softeg.slartus.forpdacommon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by slinkin on 17.12.13.
 */
public class DateExtensions {
    public static int getDaysBetween(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime())
                / (1000 * 60 * 60 * 24));
    }

    public static int getHoursBetween(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime())
                / (1000 * 60 * 60));
    }

    public static long getTimeOutForNextMinute() {
        int nextMinute = GregorianCalendar.getInstance().get(Calendar.MINUTE) + 1;

        Calendar nextCalendar = new GregorianCalendar();
        nextCalendar.setTime(GregorianCalendar.getInstance().getTime());
        nextCalendar.set(Calendar.MINUTE, nextMinute);
        nextCalendar.set(Calendar.SECOND, 0);
        nextCalendar.set(Calendar.MILLISECOND, 0);

        return nextCalendar.getTime().getTime() - GregorianCalendar.getInstance().getTime().getTime();
    }
}
