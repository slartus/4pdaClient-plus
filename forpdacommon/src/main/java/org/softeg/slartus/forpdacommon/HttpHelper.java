package org.softeg.slartus.forpdacommon;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
    protected static final int POST_TYPE = 1;
    protected static final int GET_TYPE = 2;
    protected static final int DOWNLOAD_TYPE = 3;
    public static final String GZIP = "gzip";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static String HTTP_CONTENT_CHARSET = "windows-1251";
    public static String USER_AGENT = "android";
    public static String FULL_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";
    public static final String MIME_FORM_ENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
    public static final String HTTP_RESPONSE_ERROR = "HTTP_RESPONSE_ERROR";

    // Establish client once, as static field with static setup block.
    // (This is a best practice in HttpClient docs - but will leave reference until *process* stopped on Android.)
    protected final DefaultHttpClient client;

    public CookieStore getCookieStore() {
        return client.getCookieStore();
    }

    public List<Cookie> getLastCookies() {
        return client.getCookieStore().getCookies();
    }

    public void writeExternalCookies(String cookiesFile) throws Exception {

        if (!FileUtils.mkDirs(cookiesFile))
            throw new Exception("Не могу создать директорию '" + cookiesFile + "' для cookies");

        new File(cookiesFile).createNewFile();
        FileOutputStream fw = new FileOutputStream(cookiesFile, false);

        ObjectOutput out = new ObjectOutputStream(fw);
        final List<Cookie> cookies = client.getCookieStore().getCookies();


        for (Cookie cookie : cookies) {
            new SerializableCookie(cookie).writeExternal(out);
        }
        out.close();
        fw.close();
    }

    protected RuntimeException mLeakedException = new IllegalStateException(
            "AndroidHttpClient created and never closed");

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {

            mLeakedException = null;
        }
    }

    /**
     * Release resources associated with this client.  You must call this,
     * or significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        if (mLeakedException != null) {
            getConnectionManager().shutdown();

            mLeakedException = null;
        }
    }

    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    public void clearCookies() {
        client.getCookieStore().clear();
    }


    public static void readExternalCookies(CookieStore cookieStore, String cookieFile) throws IOException {
        FileInputStream fw = new FileInputStream(cookieFile);
        ObjectInput input = new ObjectInputStream(fw);
        while (true) {
            try {
                SerializableCookie serializableCookie = new SerializableCookie();
                serializableCookie.readExternal(input);
                cookieStore.addCookie(serializableCookie);
            } catch (Exception ex) {
                break;
            }

        }
        input.close();
        fw.close();
    }

    public List<Cookie> getCookies() {
        return client.getCookieStore().getCookies();
    }


    public HttpHelper(String userAgent, final String cookiesPath) {
        responseHandler = new ResponseHandler<String>() {
            public String handleResponse(HttpResponse httpResponse) throws IOException {
                StatusLine status = httpResponse.getStatusLine();
                checkStatus(status, m_LastUrl);
                return EntityUtils.toString(httpResponse.getEntity(), HTTP_CONTENT_CHARSET);

            }
        };

        MySSLSocketFactory sf = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }


        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP_CONTENT_CHARSET);
        params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);


        params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        HttpProtocolParams.setUseExpectContinue(params, false);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        schemeRegistry.register(new Scheme("https", sf,  443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(cm, params);


        client.setCookieStore(new CookieStore() {
            private List<Cookie> m_Cookies = null;

            public void addCookie(Cookie cookie) {
                for (int i = 0; i < m_Cookies.size(); i++) {
                    if (m_Cookies.get(i).getName().equals(cookie.getName())) {
                        m_Cookies.remove(i);
                        break;
                    }
                }
                m_Cookies.add(cookie);
            }

            public List<Cookie> getCookies() {
                if (m_Cookies == null) {
                    m_Cookies = new ArrayList<Cookie>();
                    try {
                        readExternalCookies(this, cookiesPath);
                    } catch (IOException ignoreEx) {
                        // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                return m_Cookies;
            }

            public boolean clearExpired(Date date) {
                for (int i = m_Cookies.size() - 1; i >= 0; i--) {
                    if (m_Cookies.get(i).getExpiryDate() != null && date != null && date.after(m_Cookies.get(i).getExpiryDate())) {
                        m_Cookies.remove(i);
                        break;
                    }
                }
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void clear() {
                if (m_Cookies != null)
                    m_Cookies.clear();
            }
        });
        // add gzip decompressor to handle gzipped content in responses
        // (default we *do* always send accept encoding gzip cat_name in request)
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header contentEncodingHeader = entity.getContentEncoding();

                if (contentEncodingHeader != null) {
                    HeaderElement[] codecs = contentEncodingHeader.getElements();
                    for (HeaderElement codec : codecs) {
                        if (codec.getName().equalsIgnoreCase(HttpHelper.GZIP)) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });

        client.setRedirectHandler(new DefaultRedirectHandler() {
            private static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

            public URI getLocationURI(HttpResponse response, HttpContext context) {
                if (response == null) {
                    throw new IllegalArgumentException("HTTP response may not be null");
                }
                //get the location header to find out where to redirect to
                Header locationHeader = response.getFirstHeader("location");

                assert locationHeader != null;
                String location = locationHeader.getValue();

                Matcher matcher = Pattern.compile("(http://sdl\\d+.4pda.ru/\\d+/)(.*?)(\\?.*)").matcher(location);
                if (matcher.find()) {
                    location = matcher.group(1) + URLEncoder.encode(matcher.group(2)) + matcher.group(3);
                } else
                    location = location.replaceAll(" ", "%20");

                if (location.contains("/#"))
                    location = location.substring(0, location.indexOf("#"));
                URI uri = null;
                try {
                    uri = new URI(location);
                } catch (URISyntaxException ignored) {

                }


                HttpParams params = response.getParams();
                // rfc2616 demands the location value be a complete URI
                // Location       = "Location" ":" absoluteURI
                assert uri != null;
                if (!uri.isAbsolute()) {
                    // Adjust location URI
                    HttpHost target = (HttpHost) context.getAttribute(
                            ExecutionContext.HTTP_TARGET_HOST);
                    if (target == null) {
                        throw new IllegalStateException("Target host not available " +
                                "in the HTTP context");
                    }

                    HttpRequest request = (HttpRequest) context.getAttribute(
                            ExecutionContext.HTTP_REQUEST);

                    try {
                        URI requestURI = new URI(request.getRequestLine().getUri());
                        URI absoluteRequestURI = URIUtils.rewriteURI(requestURI, target, true);
                        uri = URIUtils.resolve(absoluteRequestURI, uri);
                    } catch (URISyntaxException ignored) {

                    }
                }

                if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {

                    RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute(
                            REDIRECT_LOCATIONS);

                    if (redirectLocations == null) {
                        redirectLocations = new RedirectLocations();
                        context.setAttribute(REDIRECT_LOCATIONS, redirectLocations);
                    }

                    URI redirectURI = null;
                    if (uri.getFragment() != null) {
                        try {
                            HttpHost target = new HttpHost(
                                    uri.getHost(),
                                    uri.getPort(),
                                    uri.getScheme());
                            redirectURI = URIUtils.rewriteURI(uri, target, true);
                        } catch (URISyntaxException ignored) {

                        }
                    } else {
                        redirectURI = uri;
                    }

                    if (!redirectLocations.contains(redirectURI)) {
                        redirectLocations.add(redirectURI);
                    }
                }
                m_RedirectUri = uri;
                return uri;
            }
        });
    }


    protected final ResponseHandler<String> responseHandler;

    public String performGet(final String url) throws IOException {
        return performRequest(null, url, null, null, null, new ArrayList<NameValuePair>(), HttpHelper.GET_TYPE, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform an HTTP GET operation with user/pass and headers.
     */
    public String performGet(final String url, final String user, final String pass,
                             final Map<String, String> additionalHeaders) throws IOException {
        return performRequest(null, url, user, pass, additionalHeaders, new ArrayList<NameValuePair>(), HttpHelper.GET_TYPE, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform a simplified HTTP POST operation.
     */
    public String performPost(final String url, final Map<String, String> params) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, null, null, null, params, HttpHelper.POST_TYPE, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform a simplified HTTP POST operation.
     */
    public String performPost(final String url, final List<NameValuePair> params) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, null, null, null, params, HttpHelper.POST_TYPE, HTTP_CONTENT_CHARSET);
    }

    public String performPost(final String url, final Map<String, String> params, String encoding) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, null, null, null, params, HttpHelper.POST_TYPE, encoding);
    }

    protected static URI m_RedirectUri;
    protected static String m_LastUrl;

    public static URI getRedirectUri() {
        return m_RedirectUri;
    }

    public static String getLastUri() {
        return m_LastUrl;
    }


    private String performRequest(final String contentType, String url, final String user, final String pass,
                                  final Map<String, String> headers, final Map<String, String> params, final int requestType,
                                  String encoding) throws IOException {
        List<NameValuePair> nvps = null;
        if ((params != null) && (params.size() > 0)) {
            nvps = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return performRequest(contentType, url, user, pass, headers, nvps, requestType, encoding);
    }

    //
    // private methods
    //
    private String performRequest(final String contentType, String url, final String user, final String pass,
                                  final Map<String, String> headers, final List<NameValuePair> nvps, final int requestType,
                                  String encoding) throws IOException {
        Log.d("kek", "request url " + url);
        if (url.substring(0, 2).equals("//")) {
            url = "http:".concat(url);
        }
        url = url.replace("\"", "").replace("'", "");
        m_LastUrl = url;
        // add user and pass to client credentials if present
        if ((user != null) && (pass != null)) {
            client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, pass));
        }

        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<>();
        // add encoding cat_name for gzip if not present

        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);

        if ((headers != null) && (headers.size() > 0)) {
            sendHeaders.putAll(headers);
        }
        if (requestType == HttpHelper.POST_TYPE) {
            sendHeaders.put(HttpHelper.CONTENT_TYPE, contentType);
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


        m_RedirectUri = null;

        // handle POST or GET request respectively
        HttpRequestBase method = null;
        if (requestType == HttpHelper.POST_TYPE) {
            method = new HttpPost(url);
            // data - name/value params

            if (nvps != null) {
                try {
                    HttpPost methodPost = (HttpPost) method;
                    methodPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error peforming HTTP request: " + e.getMessage(), e);
                }
            }
        } else if (requestType == HttpHelper.GET_TYPE) {
            method = new HttpGet(url);
        }
        // execute request
        return execute(method);
    }

    private synchronized String execute(final HttpRequestBase method) throws IOException {
        String response;
        // execute method returns?!? (rather than async) - do it here sync, and wrap async elsewhere

        response = client.execute(method, responseHandler);


        return response;
    }

    protected void checkStatus(StatusLine status, String url) throws IOException {
        int statusCode = status.getStatusCode();
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
            if (statusCode != 200 && statusCode != 300) {
                if (statusCode >= 500 && statusCode < 600)
                    throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()), url);
                else if (statusCode == 404)
                    throw new ShowInBrowserException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()), url);
                else
                    throw new ShowInBrowserException(statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()), url);
            }
        }
    }


    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }
}
