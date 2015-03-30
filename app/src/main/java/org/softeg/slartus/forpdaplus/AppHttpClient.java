package org.softeg.slartus.forpdaplus;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by slinkin on 30.03.2015.
 */
public class AppHttpClient {
    private static final String TAG = "AppHttpClient";
    public static String HTTP_CONTENT_CHARSET = "UTF-8";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String GZIP = "gzip";
    protected static final String CONTENT_TYPE = "Content-Type";
    public static final String MIME_FORM_ENCODED = "application/x-www-form-urlencoded";
    protected static final int POST_TYPE = 1;
    protected static final int GET_TYPE = 2;

    public String performGet(String link) throws IOException {
        return performRequestClient(GET_TYPE, link, null);
    }

    public String performRequestClient(int requestType, String link,
                                       List<NameValuePair> additionalHeaders) throws IOException {
        AndroidHttpClient client = null;
        try {
            client = AndroidHttpClient.newInstance("Android");

            Log.i(TAG, link);


            String encoding = HTTP_CONTENT_CHARSET;

            HttpRequestBase request = requestType == POST_TYPE ? new HttpPost(link) : new HttpGet(link);


            HttpContext http_context = new BasicHttpContext();


            final Map<String, String> sendHeaders = new HashMap<String, String>();
            // add encoding cat_name for gzip if not present

            sendHeaders.put(ACCEPT_ENCODING, GZIP);
            if (requestType == POST_TYPE) {
                request.addHeader(CONTENT_TYPE, MIME_FORM_ENCODED);
            }

            if ((additionalHeaders != null) && (additionalHeaders.size() > 0)) {
                ((HttpPost) request).setEntity(new UrlEncodedFormEntity(additionalHeaders, encoding));
            }

            Log.i(TAG, link);
            String res = client.execute(request, new MyResponseHandler(http_context), http_context);
            if (res == null)
                throw new NotReportException("Сайт не отвечает");
            return res;
        } finally {
            if (client != null)
                client.close();
        }

    }

    public class MyResponseHandler extends BasicResponseHandler
            implements ResponseHandler<String> {

        private HttpContext mContext;

        public MyResponseHandler(HttpContext context) {
            super();

            mContext = context;
        }


        public String handleResponse(HttpResponse httpResponse) throws IOException {
            StatusLine status = httpResponse.getStatusLine();

            checkStatus(status, "");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), HTTP_CONTENT_CHARSET));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }


            return sb.toString();
        }

        private void checkStatus(StatusLine status, String url) throws IOException {
            int statusCode = status.getStatusCode();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {

                if (statusCode != 300) {
                    if (statusCode >= 500 && statusCode < 600)
                        throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + getReasonPhrase(statusCode, status.getReasonPhrase()), url);
                    else if (statusCode == 404)
                        throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + getReasonPhrase(statusCode, status.getReasonPhrase()), url);
                    else
                        throw new ShowInBrowserException(statusCode + " " + getReasonPhrase(statusCode, status.getReasonPhrase()), url);
                }
            }
        }
    }

    public static String getReasonPhrase(int code, String defaultPhrase) {
        switch (code) {
            case 500:
                return "Внутренняя ошибка сервера";
            case 501:
                return "Не реализовано";
            case 502:
                return "Плохой шлюз";
            case 503:
                return "Сервис недоступен";
            case 504:
                return "Шлюз не отвечает";
            case 505:
                return "Версия HTTP не поддерживается";
            case 506:
                return "Вариант тоже согласован";
            case 507:
                return "Переполнение хранилища";
            case 509:
                return "Исчерпана пропускная ширина канала";
            case 510:
                return "Не расширено";
        }
        return defaultPhrase;
    }

}
