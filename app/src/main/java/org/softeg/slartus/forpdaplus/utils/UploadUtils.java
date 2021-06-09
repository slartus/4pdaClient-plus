package org.softeg.slartus.forpdaplus.utils;

import androidx.core.util.Pair;

import org.json.JSONObject;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;

/**
 * Created by isanechek on 1/27/18.
 */

public class UploadUtils {

    public static AppResponse okUploadFile(String url, String pathToFile,
                                           Map<String, String> additionalHeaders, ProgressState progress) {

        String nameValue = "";
        try {
            nameValue = FileUtils.getFileNameFromUrl(pathToFile);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ArrayList<Pair<String, String>> values = new ArrayList<>();
        for (String key : additionalHeaders.keySet()) {
            values.add(new Pair<>(key, additionalHeaders.get(key)));
        }

        return Http.Companion.getInstance()
                .uploadFile(url, nameValue, pathToFile, "FILE_UPLOAD",
                        values, num -> progress.update("", num));

    }

    private static String okUploadFile(String pathToFile, Map<String, String> additionalHeaders) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        File file = new File(pathToFile);
        String res = "";
        if (file.exists()) {
            final MediaType MT = MediaType.parse("image/png");
            String nameValue = "";
            try {
                nameValue = FileUtils.getFileNameFromUrl(pathToFile);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            builder.addFormDataPart("file", nameValue, RequestBody.create(MT, file)); // <-------
            builder.addFormDataPart("Cache-Control", "max-age=0");
            builder.addFormDataPart("Upgrade-Insecure-Reaquest", "1");
            builder.addFormDataPart("Accept", "text-/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            builder.addFormDataPart("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36 OPR/38.0.2220.31");
            builder.addFormDataPart("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
            builder.addFormDataPart("Referer", "https://savepice.ru/");
            builder.addFormDataPart("Origin", "https://savepice.ru");
            builder.addFormDataPart("X-Requested-With", "XMLHttpRequest");
            if (additionalHeaders.size() > 0) {
                for (String key: additionalHeaders.keySet()) {
                    builder.addFormDataPart(key, additionalHeaders.get(key));
                }
            }
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url("https://savepice.ru/upload")
                    .post(requestBody)
                    .build();
            OkHttpClient client = Http.newClientBuiler(App.getContext()).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    res = response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static String attachSavePiceFile(String newFilePath) throws Exception {
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("img", "file");
        additionalHeaders.put("url", "");
        additionalHeaders.put("selected_input", "file");
        additionalHeaders.put("size", "640");
        additionalHeaders.put("preview_size", "180");
        additionalHeaders.put("rotation_type", "0");

        String response = okUploadFile(newFilePath, additionalHeaders);
        JSONObject jsonObject = new JSONObject(response);
        if (jsonObject.optBoolean("error", false)) {
            throw new NotReportException(jsonObject.optString("text"));
        }
        return jsonObject.optString("redirect_path").replace("/uploaded/", "/uploads/").replace(".html", "");
    }

}
