package org.softeg.slartus.forpdaplus;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HttpContext;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
        super(userAgent, PreferencesActivity.getCookieFilePath());
    }

    public void writeExternalCookies() throws Exception {
        String cookiesFile = PreferencesActivity.getCookieFilePath();
        writeExternalCookies(cookiesFile);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {
            AppLog.e(null, mLeakedException);
            mLeakedException = null;
        }
    }

//    public String uploadFile2(final String url, String filePath, Map<String, String> additionalHeaders,
//                             final ProgressState progress) throws Exception {
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .build();
//    }

    public String uploadFile(final String url, String filePath, Map<String, String> additionalHeaders,
                             final ProgressState progress) throws Exception {
        final Map<String, String> sendHeaders = new HashMap<>();
        sendHeaders.put(HttpHelper.CONTENT_TYPE, "multipart/form-data;");
        if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
            sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        }

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor((request, context) -> {
                for (String key : sendHeaders.keySet()) {
                    if (!request.containsHeader(key)) {
                        request.addHeader(key, sendHeaders.get(key));
                    }
                }
                if (url.contains("savepice")) {
                    for (Cookie cookie : getCookieStore().getCookies()) {
                        if (cookie.getName().equals("PHPSESSID")) {
                            request.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
                        }
                    }
                    request.removeHeaders("User-Agent");
                    request.addHeader("Cache-Control", "max-age=0");
                    request.addHeader("Upgrade-Insecure-Reaquest", "1");
                    request.addHeader("Accept", "text-/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    request.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36 OPR/38.0.2220.31");
                    request.addHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
                    request.addHeader("Referer", "https://savepice.ru/");
                    request.addHeader("Origin", "https://savepice.ru");
                    request.addHeader("X-Requested-With", "XMLHttpRequest");

                }

            });
        }

        MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
        multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntity.setCharset(Charset.forName("windows-1251"));
        File uploadFile = new File(filePath);
        multipartEntity.addBinaryBody(url.contains("savepice") ? "file" : "FILE_UPLOAD", uploadFile, ContentType.create("image/png"),
                FileUtils.getFileNameFromUrl(filePath));

        m_RedirectUri = null;

        HttpPost httppost = new HttpPost(url);
        if (additionalHeaders != null)
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                multipartEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
            }
        final HttpEntity yourEntity = multipartEntity.build();

        final long totalSize = uploadFile.length();
        class ProgressiveEntity implements HttpEntity {
            @Override
            public void consumeContent() throws IOException {
                yourEntity.consumeContent();
            }

            @Override
            public InputStream getContent() throws IOException,
                    IllegalStateException {
                return yourEntity.getContent();
            }

            @Override
            public Header getContentEncoding() {
                return yourEntity.getContentEncoding();
            }

            @Override
            public long getContentLength() {
                return yourEntity.getContentLength();
            }

            @Override
            public Header getContentType() {
                return yourEntity.getContentType();
            }

            @Override
            public boolean isChunked() {
                return yourEntity.isChunked();
            }

            @Override
            public boolean isRepeatable() {
                return yourEntity.isRepeatable();
            }

            @Override
            public boolean isStreaming() {
                return yourEntity.isStreaming();
            } // CONSIDER put a _real_ delegator into here!

            @Override
            public void writeTo(OutputStream outstream) throws IOException {

                class ProxyOutputStream extends FilterOutputStream {
                    /**
                     * @author Stephen Colebourne
                     */
                    private long totalSent;

                    ProxyOutputStream(OutputStream proxy) {
                        super(proxy);
                        totalSent = 0;

                    }

                    public void write(int idx) throws IOException {
                        out.write(idx);
                    }

                    public void write(byte[] bts) throws IOException {
                        out.write(bts);
                    }

                    public void write(byte[] bts, int st, int end)
                            throws IOException {
                        totalSent += end;
                        progress.update("", (int) ((totalSent / (float) totalSize) * 100));

                        out.write(bts, st, end);
                    }

                    public void flush() throws IOException {
                        out.flush();
                    }

                    public void close() throws IOException {
                        out.close();
                    }
                } // CONSIDER import this class (and risk more Jar File Hell)

                class ProgressiveOutputStream extends ProxyOutputStream {
                    private ProgressiveOutputStream(OutputStream proxy) {
                        super(proxy);
                    }
                }

                yourEntity.writeTo(new ProgressiveOutputStream(outstream));
            }

        }


        ProgressiveEntity myEntity = new ProgressiveEntity();

        httppost.setEntity(myEntity);

        String res = client.execute(httppost, responseHandler);
        if (res == null)
            throw new NotReportException(App.getContext().getString(R.string.site_not_respond));
        return res;
    }

    public HttpResponse getDownloadResponse(String url, long range) throws IOException {
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
        StatusLine status = response.getStatusLine();
        checkStatus(status, url);

        return response;
    }
//
//    public Response getDownloadResponse(String url, long range) throws IOException {
//        // process headers using request interceptor
//        final Map<String, String> sendHeaders = new HashMap<String, String>();
//        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
//        if (range != 0)
//            sendHeaders.put("Range", "bytes=" + range + "-");
//
//        OkHttpClient okHttpClient= new OkHttpClient();
//        Headers headers = Headers.of(sendHeaders);
//        Request request = new Request.Builder()
//                .headers(headers)
//                .url(url)
//                .build();
//
//        return okHttpClient.newCall(request).execute();
//    }

    public HttpEntity getDownloadEntity(String url, long range) throws Exception {
        return getDownloadResponse(url, range).getEntity();
    }


}
