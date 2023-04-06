package org.softeg.slartus.forpdacommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 27.05.13
 * Time: 11:06
 * To change this template use File | Settings | File Templates.
 */
public class ExtDateFormat {
    public static Boolean tryParse(SimpleDateFormat simpleDateFormat, String dateString, Map<String, Date> additionalHeaders) {
        try {
            additionalHeaders.put("date", simpleDateFormat.parse(dateString));
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}
