package org.softeg.slartus.forpdaplus;


import java.io.IOException;
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


//
//
//    public HttpResponse getDownloadResponse(String url, long range) throws IOException {
//        // process headers using response interceptor
//        final Map<String, String> sendHeaders = new HashMap<>();
//        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
//        if (range != 0)
//            sendHeaders.put("Range", "bytes=" + range + "-");
//
//        if (sendHeaders.size() > 0) {
//            client.addRequestInterceptor((response, context) -> {
//                for (String key : sendHeaders.keySet()) {
//                    if (!response.containsHeader(key)) {
//                        response.addHeader(key, sendHeaders.get(key));
//                    }
//                }
//            });
//        }
//
//        HttpGet response = new HttpGet(url);
//        HttpResponse response = client.execute(response);
//        StatusLine status = response.getStatusLine();
//        checkStatus(status, url);
//
//        return response;
//    }
////
////    public Response getDownloadResponse(String url, long range) throws IOException {
////        // process headers using response interceptor
////        final Map<String, String> sendHeaders = new HashMap<String, String>();
////        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
////        if (range != 0)
////            sendHeaders.put("Range", "bytes=" + range + "-");
////
////        OkHttpClient okHttpClient= new OkHttpClient();
////        Headers headers = Headers.of(sendHeaders);
////        Request response = new Request.Builder()
////                .headers(headers)
////                .url(url)
////                .build();
////
////        return okHttpClient.newCall(response).execute();
////    }
//
//    public HttpEntity getDownloadEntity(String url, long range) throws Exception {
//        return getDownloadResponse(url, range).getEntity();
//    }


}
