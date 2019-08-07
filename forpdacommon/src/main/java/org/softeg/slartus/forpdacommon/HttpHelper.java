package org.softeg.slartus.forpdacommon;


import android.support.v4.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class HttpHelper {
    protected static final String TAG = "HttpHelper";
    protected static final String CONTENT_TYPE = "Content-Type";
    public static final String GZIP = "gzip";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    private static String HTTP_CONTENT_CHARSET = "windows-1251";
    protected static String USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Mobile Safari/537.36";
    public static String FULL_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36";









    public static String performGet(final String url) {
        AppResponse response = Http.Companion.getInstance().performGet(url);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;
        m_LastUrl=response.getRequestUrl();
        return response.getResponseBody();
    }

    /**
     * Perform a simplified HTTP POST operation.
     */
    public static String performPost(final String url, final List<NameValuePair> params) {

        ArrayList<Pair<String, String>> listParams = new ArrayList<>();
        for (NameValuePair key : params) {
            listParams.add(new Pair<>(key.getName(), key.getValue()));
        }
        AppResponse response = Http.Companion.getInstance().postMultipart(url, listParams);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;
        m_LastUrl=response.getRequestUrl();
        return response.getResponseBody();
    }

    public static String performPost(final String url, final Map<String, String> params) throws IOException {
        ArrayList<Pair<String, String>> listParams = new ArrayList<>();
        for (String key : params.keySet()) {
            listParams.add(new Pair<>(key, params.get(key)));
        }
        AppResponse response = Http.Companion.getInstance().performPost(url, listParams);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;
        m_LastUrl=response.getRequestUrl();
        return response.getResponseBody();
    }

    protected static URI m_RedirectUri;
    private static String m_LastUrl;


    public static URI getRedirectUri() {
        return m_RedirectUri;
    }

    public static String getLastUri() {
        return m_LastUrl;
    }



}
