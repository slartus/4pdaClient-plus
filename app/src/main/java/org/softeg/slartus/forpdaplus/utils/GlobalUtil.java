package org.softeg.slartus.forpdaplus.utils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by isanechek on 16.03.16.
 */
public class GlobalUtil {

    public static String getJsonFromGithub(String link) {

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(link).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
