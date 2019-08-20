package org.softeg.slartus.forpdaapi;

import org.softeg.slartus.forpdacommon.NameValuePair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ru.slartus.http.AppResponse;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:06
 */
public interface IHttpClient {
    AppResponse performGet(String s, Boolean checkEmptyResult, Boolean checkLoginAndMails) throws IOException;

    AppResponse performGet(String s) throws IOException;

    AppResponse performGetFullVersion(String s) throws IOException;

    AppResponse performPost(String s, Map<String, String> additionalHeaders) throws IOException;

    AppResponse performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException;

    AppResponse performPost(String s, List<NameValuePair> additionalHeaders) throws IOException;

    AppResponse uploadFile(String url, String filePath, Map<String, String> additionalHeaders, ProgressState progress) throws Exception;

}
