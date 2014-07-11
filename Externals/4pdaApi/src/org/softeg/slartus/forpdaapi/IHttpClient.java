package org.softeg.slartus.forpdaapi;

import java.io.IOException;
import java.util.Map;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:06
 */
public interface IHttpClient {
    /**
     * Метод под доставке страницы, в которой можно проверить логин
     *
     * @throws IOException
     */
    String performGetWithCheckLogin(String s, OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException;

    String performGet(String s, Boolean checkEmptyResult) throws IOException;

    String performGet(String s) throws IOException;

    String performGetFullVersion(String s) throws IOException;

    String performPost(String s, Map<String, String> additionalHeaders) throws IOException;

    String performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException;

    String uploadFile(String url, String filePath, Map<String, String> additionalHeaders, ProgressState progress) throws Exception;

    org.apache.http.client.CookieStore getCookieStore() throws IOException;


}
