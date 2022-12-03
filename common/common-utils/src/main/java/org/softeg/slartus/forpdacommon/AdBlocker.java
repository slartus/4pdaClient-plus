package org.softeg.slartus.forpdacommon;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {
    private static final Set<String> AD_HOSTS =
            new HashSet<>(Arrays.asList("swfly743.ru", "doubleclick.net",
                    "an.yandex.ru","google-analytics.com"));

    public static boolean isAd(String url) {
        try {
            return isAdHost(getHost(url));
        } catch (MalformedURLException e) {
            Log.e("Devangi..", e.toString());
            return false;
        }
    }

    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    private static String getHost(String url) throws MalformedURLException {
        return new URL(url).getHost();
    }

}
