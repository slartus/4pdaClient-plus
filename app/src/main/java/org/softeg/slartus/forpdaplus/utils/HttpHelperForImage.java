package org.softeg.slartus.forpdaplus.utils;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.apache.http.HttpResponse;
import org.softeg.slartus.forpdaplus.HttpHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by isanechek on 15.02.16.
 */
public class HttpHelperForImage extends BaseImageDownloader {

    public HttpHelperForImage(Context context) {
        super(context);
    }

    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpResponse httpResponse = new HttpHelper().getDownloadResponse(imageUri, 0);
        return httpResponse.getEntity().getContent();
    }
}
