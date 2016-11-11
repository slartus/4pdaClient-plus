package org.softeg.slartus.forpdaplus.classes;

import org.softeg.slartus.forpdaapi.Topic;

/**
 * User: slinkin
 * Date: 22.11.11
 * Time: 13:11
 */
public final class ThemeOpenParams {
    public static final String BROWSER = "browser";

    public static String getUrlParams(String openParam, String defaultUrlParam) {
        if (openParam == null) return defaultUrlParam;
        if (openParam.equals(ThemeOpenParams.BROWSER))
            return "";
        if (openParam.equals(Topic.NAVIGATE_VIEW_FIRST_POST))
            return "";
        if (openParam.equals(Topic.NAVIGATE_VIEW_LAST_POST))
            return "view=getlastpost";
        if (openParam.equals(Topic.NAVIGATE_VIEW_NEW_POST))
            return "view=getnewpost";

        return defaultUrlParam;

    }
}
