package org.softeg.slartus.forpdacommon;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slinkin on 28.01.14.
 */
public class UrlExtensions {
    public static String getFileNameFromUrl(String url) throws UnsupportedEncodingException {
        String decodedUrl = UrlExtensions.decodeUrl(url).toString();
        int index = decodedUrl.lastIndexOf("/");

        return normalize(decodedUrl.substring(index + 1));
    }

    private static String normalize(String fileName) {
        return fileName.replaceAll("[^а-яА-Яa-zA-z0-9._-]", "_");
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
