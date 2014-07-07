package org.softeg.slartus.forpdanotifyservice;

import android.text.TextUtils;

import org.apache.http.client.CookieStore;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.HttpHelper;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 24.05.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class Client implements IHttpClient {
    private String cookiesPath;

    public Client(String cookiesPath) {

        this.cookiesPath = cookiesPath;
    }

    @Override
    public String performGetWithCheckLogin(String s, OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException {
        return performGet(s);
    }

    @Override
    public String performGet(String s, Boolean checkEmptyResult) throws IOException {
        return performGet(s);
    }

    @Override
    public String performGet(String s) throws IOException, NotReportException {
        HttpHelper httpHelper = new HttpHelper(HttpHelper.USER_AGENT, cookiesPath);
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performGet(s);
        } finally {
            httpHelper.close();

        }
        if (TextUtils.isEmpty(res))
            throw new NotReportException("Сервер вернул пустую страницу");
        // m_HttpHelper.close();
        return res;
    }

    @Override
    public String performGetFullVersion(String s) throws IOException, NotReportException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String performPost(String s, Map<String, String> additionalHeaders) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String uploadFile(String url, String filePath, Map<String, String> additionalHeaders, ProgressState progress) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CookieStore getCookieStore() {
        HttpHelper httpHelper = new HttpHelper(HttpHelper.USER_AGENT, cookiesPath);
        return httpHelper.getCookieStore();
    }
}
