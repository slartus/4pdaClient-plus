package org.softeg.slartus.forpdaplus.controls;

import org.softeg.slartus.forpdaplus.Client;

/**
 * Created by isanechek on 08.01.16.
 */
public class Surprise {
    private static final String[] inBlock = { "Googleoff", "Stealth3001", "shpatelman" };

    public static boolean isBlocked() {
        String user = Client.getInstance().getUser();
        for (String obj : inBlock) {
            if (user.equals(obj)) {
                return true;
            }
        }
        return false;
    }
}
