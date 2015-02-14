package org.softeg.slartus.forpdacommon;

import org.apache.http.cookie.Cookie;

import java.util.Date;

/**
 * Created by slinkin on 06.08.13.
 */
public class SimpleCookie implements Cookie {
    private String mName;
    private String mValue;

    public SimpleCookie(String name, String value) {

        mName = name;
        mValue = value;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public String getCommentURL() {
        return null;
    }

    @Override
    public Date getExpiryDate() {
        return null;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public int[] getPorts() {
        return new int[0];
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isExpired(Date date) {
        return false;
    }
}