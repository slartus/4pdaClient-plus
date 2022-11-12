package org.softeg.slartus.forpdaplus.utils;

import androidx.core.util.Pair;

import org.json.JSONObject;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.UrlExtensions;
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
import ru.slartus.http.FileForm;
import ru.slartus.http.Http;

/**
 * Created by isanechek on 1/27/18.
 */

public class UploadUtils {

    public static AppResponse okUploadFile(String url, String pathToFile,
                                           Map<String, String> additionalHeaders, ProgressState progress) {

        String nameValue = "";
        try {
            nameValue = UrlExtensions.getFileNameFromUrl(pathToFile);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ArrayList<Pair<String, String>> values = new ArrayList<>();
        for (String key : additionalHeaders.keySet()) {
            values.add(new Pair<>(key, additionalHeaders.get(key)));
        }

        return Http.Companion.getInstance()
                .uploadFile(url, nameValue, pathToFile, FileForm.FileUpload,
                        values, num -> progress.update("", num));

    }
}
