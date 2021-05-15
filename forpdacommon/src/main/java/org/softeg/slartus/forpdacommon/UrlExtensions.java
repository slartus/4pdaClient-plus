package org.softeg.slartus.forpdacommon;

import android.net.Uri;

import org.softeg.slartus.hosthelper.HostHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slinkin on 28.01.14.
 */
public class UrlExtensions {

    public static Dictionary<CharSequence, CharSequence> get4pdaUrlParams(CharSequence url,
                                                                          CharSequence[] constParams,
                                                                          CharSequence[] patterns) {
        if (!Pattern.compile(HostHelper.getHost(), Pattern.CASE_INSENSITIVE)
                .matcher(url).find())
            return null;
        Dictionary<CharSequence, CharSequence> res = new Hashtable<>();
        for (CharSequence pattern : patterns) {
            Matcher m = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE)
                    .matcher(url);
            if (!m.find())// если хоть один паттерн не найден, возвращаем нулл
                return null;
            if (m.groupCount() == 2)
                res.put(m.group(1), m.group(2));
        }
        return res;
    }

    public static Uri getUri(CharSequence uriString) {
        return Uri.parse((String) uriString);
    }

    public static CharSequence decodeUrl(CharSequence url) throws UnsupportedEncodingException {
        if (isUrlUtf8Encoded(url))
            return URLDecoder.decode(url.toString(), "UTF-8");
        if (isUrlWindows1251Encoded(url))
            return URLDecoder.decode(url.toString(), "windows-1251");
        return url;
    }


    public static boolean isUrlUtf8Encoded(CharSequence url)
            throws UnsupportedEncodingException {
        return isUrlEncoded(url, "UTF-8");
    }

    public static boolean isUrlWindows1251Encoded(CharSequence url)
            throws UnsupportedEncodingException {
        return isUrlEncoded(url, "windows-1251");
    }

    public static boolean isUrlEncoded(CharSequence url, String encoding)
            throws UnsupportedEncodingException {
        Matcher paramsMatcher = Pattern.compile("(?:\\w+=|/+|\\\\+)([\\w%+_*\\.-]*)", Pattern.MULTILINE).matcher(url);
        while (paramsMatcher.find()) {
            if (!isAlphaNumeric(URLDecoder.decode(paramsMatcher.group(1), encoding)))
                return false;
        }
        return true;
    }

    private static boolean isAlphaNumeric(String decode) {
        String symbls = "!@#$%^&*()_+=-`\"{}[]:;'<>,.?/\\| ";
        for (char c : decode.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !symbls.contains(c + "")) {
                return false;
            }
        }
        return true;
    }

    public static String removeDoubleSplitters(String url) {
        return url.replaceAll("([^:])//", "$1/");
    }
}
