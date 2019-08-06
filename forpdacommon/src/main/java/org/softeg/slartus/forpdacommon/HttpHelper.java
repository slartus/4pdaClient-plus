package org.softeg.slartus.forpdacommon;


import android.support.v4.util.Pair;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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


    // Establish client once, as static field with static setup block.
    // (This is a best practice in HttpClient docs - but will leave reference until *process* stopped on Android.)
    protected final DefaultHttpClient client;


    private RuntimeException mLeakedException = new IllegalStateException(
            "AndroidHttpClient created and never closed");

    @Override
    public void finalize() throws Throwable {
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

    private ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }




    public HttpHelper(String userAgent) {
        responseHandler = new ResponseHandler<String>() {
            public String handleResponse(HttpResponse httpResponse) throws IOException {


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
        schemeRegistry.register(new Scheme("https", sf, 443));
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
                    m_Cookies = new ArrayList<>();
                    //readExternalCookies(this, cookiesPath);
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
            public void process(final HttpResponse response, final HttpContext context) {
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
                    //noinspection deprecation
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


    protected void checkStatus(StatusLine status, String url) throws IOException {
        int statusCode = status.getStatusCode();
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
            if (statusCode != 300) {
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
        GzipDecompressingEntity(final HttpEntity entity) {
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
