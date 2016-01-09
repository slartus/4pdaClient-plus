package org.softeg.slartus.forpdaplus.controls;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.utils.LogUtil;

import java.nio.charset.Charset;

/**
 * Created by isanechek on 08.01.16.
 */
public class Surprise {
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static String[] inBlock = {
            "\u0047\u006F\u006F\u0067\u006C\u0065\u006F\u0066\u0066",
            "\u0053\u0074\u0065\u0061\u006C\u0074\u0068\u0033\u0030\u0030\u0031",
            "\u0073\u0068\u0070\u0061\u0074\u0065\u006C\u006D\u0061\u006E"
    };

    private static String forceUtf8Coding(String input) {
        return new String(input.getBytes(UTF_8), UTF_8);
    }

    public static boolean isBlocked() {
        String user = Client.getInstance().getUser();
        for (String obj : inBlock) {
            String m = forceUtf8Coding(obj);
            if (m.equals(user)) {
                return true;
            }
        }
        return false;
    }
}
