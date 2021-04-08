package org.softeg.slartus.forpdacommon;


import androidx.core.util.Pair;

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

    public static AppResponse performGet(final String url) {
        AppResponse response = Http.Companion.getInstance().performGet(url);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;

        return response;
    }

    public static AppResponse performPost(final String url, final List<NameValuePair> params) {
        ArrayList<Pair<String, String>> listParams = new ArrayList<>();
        for (NameValuePair key : params) {
            listParams.add(new Pair<>(key.getName(), key.getValue()));
        }
        AppResponse response = Http.Companion.getInstance().postMultipart(url, listParams);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;

        return response;
    }

    public static AppResponse performPost(final String url, final Map<String, String> params) throws IOException {
        ArrayList<Pair<String, String>> listParams = new ArrayList<>();
        for (String key : params.keySet()) {
            listParams.add(new Pair<>(key, params.get(key)));
        }
        AppResponse response = Http.Companion.getInstance().performPost(url, listParams);
        m_RedirectUri = response.getRedirectUrl() != null ? URI.create(response.getRedirectUrl()) : null;

        return response;
    }

    private static URI m_RedirectUri;

    public static URI getRedirectUri() {
        return m_RedirectUri;
    }

}
