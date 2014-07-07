package org.softeg.slartus.forpdaplus.video.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video value object, initialized by String url encoded params
 */
public class VideoStream {

    private String mUrl;
    private String mSig;

    public VideoStream(String params) {
        Matcher m = Pattern.compile("(\\w+)=([^&]*)").matcher(params);
        String sig = null;
        String url = null;
        while (m.find()) {
            String name = m.group(1);
            if (url == null && "url".equalsIgnoreCase(name))
                url = m.group(2);
            else if (sig == null && "sig".equalsIgnoreCase(name))
                sig = m.group(2);
            if (url != null && sig != null)
                break;
        }

        mUrl = url + "&signature=" + sig;
    }

    public String getUrl() {
        return mUrl;
    }
}
