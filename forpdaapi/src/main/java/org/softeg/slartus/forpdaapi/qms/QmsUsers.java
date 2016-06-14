package org.softeg.slartus.forpdaapi.qms;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 11:06
 */
public class QmsUsers extends ArrayList<QmsUser> {

    public int unreadMessageUsersCount() {
        return unreadMessageUsersCount(this);
    }

    public static int unreadMessageUsersCount(ArrayList<QmsUser> users) {
        int senders = 0;
        for (QmsUser qmsUser : users) {
            if (!TextUtils.isEmpty(qmsUser.getNewMessagesCount()))
                senders += Integer.parseInt(qmsUser.getNewMessagesCount().toString());
        }
        return senders;
    }

}
