package org.softeg.slartus.forpdacommon;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.net.URI;

/**
 * Created by slartus on 09.02.14.
 */
public class Http {
    public static String getPage(String url, String encoding) throws Exception {
        HttpGet httpMethod = new HttpGet();
        httpMethod.setURI(new URI(url));
        HttpResponse response = new DefaultHttpClient().execute(httpMethod);
        String infoStr = null;
        switch(response.getStatusLine().getStatusCode()){
            case 200:
                HttpEntity entity = response.getEntity();
                if(entity != null)
                    infoStr = EntityUtils.toString(entity);
                break;
        }
        if (TextUtils.isEmpty(infoStr))
            throw new Exception("Сервер вернул пустую страницу");
        return infoStr;
    }

    public static Boolean ping(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();


        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        return checkStatus(response.getStatusLine());
    }

    protected static boolean checkStatus(StatusLine status) {
        int statusCode = status.getStatusCode();
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
            if (statusCode != 200 && statusCode != 300) {
                return false;
            }
        }
        return true;
    }
}
