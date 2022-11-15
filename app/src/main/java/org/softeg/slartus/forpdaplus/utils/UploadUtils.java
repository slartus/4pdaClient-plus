package org.softeg.slartus.forpdaplus.utils;


import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.UrlExtensions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import kotlin.Pair;
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
