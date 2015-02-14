package org.softeg.slartus.forpdaplus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.classes.CountingMultipartEntity;
import org.softeg.slartus.forpdaplus.classes.common.Translit;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class HttpHelper extends org.softeg.slartus.forpdacommon.HttpHelper {

    public HttpHelper() throws IOException {
        this(USER_AGENT);
    }

    public HttpHelper(String userAgent) throws IOException {
        super(userAgent, PreferencesActivity.getCookieFilePath(MyApp.getContext()));
    }

    public void writeExternalCookies() throws Exception {
        String cookiesFile = PreferencesActivity.getCookieFilePath(MyApp.getContext());
        writeExternalCookies(cookiesFile);
    }



    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {
            Log.e(null, mLeakedException);
            mLeakedException = null;
        }
    }

        public String uploadFile(String url, String filePath, Map<String, String> additionalHeaders
                , final ProgressState progress) throws Exception {

            // process headers using request interceptor
            final Map<String, String> sendHeaders = new HashMap<String, String>();
            sendHeaders.put(HttpHelper.CONTENT_TYPE, "multipart/form-data;");
            // sendHeaders.put(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP_CONTENT_CHARSET);
            // add encoding cat_name for gzip if not present
            if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
                sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
            }

            if (sendHeaders.size() > 0) {
                client.addRequestInterceptor(new HttpRequestInterceptor() {
                    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                        for (String key : sendHeaders.keySet()) {
                            if (!request.containsHeader(key)) {
                                request.addHeader(key, sendHeaders.get(key));
                            }
                        }
                    }
                });
            }

            final File file = new File(filePath);

            CountingMultipartEntity mpEntity = new CountingMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,progress);
            ContentBody cbFile = new FileBody(file, Translit.translit(FileUtils.getFileNameFromUrl(filePath)).replace(' ', '_'), "text/plain", "UTF-8");
            FormBodyPart formBodyPart = new FormBodyPart("FILE_UPLOAD", cbFile);

            mpEntity.addPart(formBodyPart);
            m_RedirectUri = null;

            HttpPost httppost = new HttpPost(url);
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                mpEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
            }

            httppost.setEntity(mpEntity);
            HttpResponse response = client.execute(httppost);
            StatusLine line = response.getStatusLine();


            // return code indicates upload failed so throw exception
            if (line.getStatusCode() < 200 || line.getStatusCode() >= 300) {
                throw new NotReportException("Ошибка загрузки файла:" + line.getReasonPhrase());
            }
            String res = EntityUtils.toString(response.getEntity());
            return res;
        }

    private class TestMultipartEntity extends MultipartEntity{
        public TestMultipartEntity(org.apache.http.entity.mime.HttpMultipartMode mode){
            super(mode);
        }
        public int i=0;
        public boolean isRepeatable() {
            i++;
            return true;
        }
    }


    public HttpEntity getDownloadResponse(String url, long range) throws Exception {

        // String url = downloadTask.getUrl();
        //url = "http://4pda.ru/forum/dl/post/944795/PolarisOffice_3.0.3047Q_SGS.apk"; //9.5Mb

        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        if (range != 0)
            sendHeaders.put("Range", "bytes=" + range + "-");

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }

        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        // Check if server response is valid
        StatusLine status = response.getStatusLine();
        checkStatus(status, url);
//        int statusCode= status.getStatusCode();
//
//        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
//            if (statusCode >= 500 && statusCode < 600)
//                throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()),url);
//            else if (statusCode ==404)
//                throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()),url);
//            else
//                throw new ShowInBrowserException(statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()),url);
//        }


        return response.getEntity();
    }

    public InputStream getImageStream(String url) throws Exception {
        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        // add encoding cat_name for gzip if not present
        if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
            sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        }

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }

        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        // Check if server response is valid
        StatusLine status = response.getStatusLine();
        checkStatus(status, url);

        return response.getEntity().getContent();

    }


}
