package org.softeg.slartus.forpdaplus.video.api;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.softeg.slartus.forpdaplus.Client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeAPI {
    public static final String URL_GET_VIDEO_INFO = "http://www.youtube.com/get_video_info?&video_id=";

    public static CharSequence getYoutubeId(CharSequence youtubeUrl) {
        String[] patterns = {
                "v=([a-zA-Z0-9_\\-]*)",
                "v/([a-zA-Z0-9_\\-]*)",
                "youtu.be/([a-zA-Z0-9_\\-]*)",
                "vnd.youtube:([a-zA-Z0-9_\\-]*)",
                "embed/([a-zA-Z0-9_\\-]*)",
        };
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(youtubeUrl);
            if (m.find())
                return m.group(1);
        }
        return null;
    }

    public static void parse(VideoItem videoItem, String id) throws Exception {
        String url = URL_GET_VIDEO_INFO + id;


        String infoStr = Client.getInstance().performGet(url);
        Matcher m = Pattern.compile("([^&=]*)=([^$&]*)", Pattern.CASE_INSENSITIVE).matcher(infoStr);

        String fmtList = null;
        String url_encoded_fmt_stream_map = null;

        CharSequence title = videoItem.getTitle();

        String error = null;
        while (m.find()) {
            String name = m.group(1);

            if (fmtList == null && "fmt_list".equalsIgnoreCase(name))
                fmtList = URLDecoder.decode(m.group(2), "UTF-8");
            else if (url_encoded_fmt_stream_map == null && "url_encoded_fmt_stream_map".equalsIgnoreCase(name))
                url_encoded_fmt_stream_map = URLDecoder.decode(m.group(2), "UTF-8");
            else if (title == null && "title".equalsIgnoreCase(name))
                title = URLDecoder.decode(m.group(2), "UTF-8");
            else if (error == null && "reason".equalsIgnoreCase(name))
                error = Html.fromHtml(URLDecoder.decode(m.group(2), "UTF-8")).toString();

            if (fmtList != null && url_encoded_fmt_stream_map != null
                    && title != null && error != null)
                break;
        }        if (!TextUtils.isEmpty(error)) {
            videoItem.setDefaultBitrate("http://www.youtube.com/watch?v=" + id);
            return;
        }
        videoItem.setTitle(title);

        if (fmtList != null && url_encoded_fmt_stream_map != null) {
            Matcher fmtMatcher = Pattern.compile("(\\d+)\\/(\\d+x\\d+)").matcher(fmtList);
            String streamStrs[] = url_encoded_fmt_stream_map.split(",");
            int fmtInd = 0;
            while (fmtMatcher.find()) {
                fmtInd++;
                if (streamStrs.length == fmtInd)
                    break;
                int ind = indexOf(Integer.parseInt(fmtMatcher.group(1)), YouTubeFMTQuality.supported);
                if (ind == -1) continue;
                Quality videoFormat = new Quality();
                videoFormat.setHeight(YouTubeFMTQuality.supported_titles[ind]);
                videoFormat.setFileName(URLDecoder.decode(getUrlFromParams(streamStrs[fmtInd - 1]), "UTF-8"));

                videoItem.getQualities().add(videoFormat);
            }
        }

    }

    public static int indexOf(int needle, int[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) return i;
        }

        return -1;
    }

    private static boolean parse2(VideoItem info) throws Exception {

        String page = Client.getInstance().performGet(String.format("https://gdata.youtube.com/feeds/api/videos/%s?v=2&alt=json", info.getvId()));

        try {
            JSONObject jsonObject = new JSONObject(page);


            info.setTitle(jsonObject.getJSONObject("entry").getJSONObject("title").getString("$t"));

            String url = jsonObject.getJSONObject("entry").getJSONObject("content").getString("src");
            if (!url.endsWith("3gp")) {
                url = null;
                JSONArray jarray = jsonObject.getJSONObject("entry").getJSONObject("media$group").getJSONArray("media$content");
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jitem = jarray.getJSONObject(i);
                    String qualityUrl = jitem.getString("url");
                    if (!qualityUrl.endsWith("3gp")) continue;
                    url = qualityUrl;
                    break;
                }
            }
            return url != null;
        } catch (Throwable ex) {
            return false;
        }

    }


    private static String getUrlFromParams(CharSequence params) {

        Matcher m = Pattern.compile("(\\w+)=([^&]*)").matcher(params);
        String sig = null;
        String quality = null;
        String url = null;
        while (m.find()) {
            String name = m.group(1);
            if (url == null && "url".equalsIgnoreCase(name))
                url = m.group(2);
            else if (sig == null && "sig".equalsIgnoreCase(name))
                sig = m.group(2);
            else if (quality == null && "quality".equalsIgnoreCase(name))
                quality = m.group(2);
            if (url != null && sig != null && quality != null)
                break;
        }

        return url + "&signature=" + sig;

    }

    public static class YouTubeFMTQuality {

        public static final int GPP3_LOW = 13;        //3GPP (MPEG-4 encoded) Low quality
        public static final int GPP3_MEDIUM = 17;        //3GPP (MPEG-4 encoded) Medium quality
        public static final int MP4_NORMAL = 18;        //MP4  (H.264 encoded) Normal quality
        public static final int MP4_HIGH = 22;        //MP4  (H.264 encoded) High quality
        public static final int MP4_HIGH1 = 37;        //MP4  (H.264 encoded) High quality

        public static final CharSequence GPP3_LOW_TITLE = "240p";        //3GPP (MPEG-4 encoded) Low quality
        public static final CharSequence GPP3_MEDIUM_TITLE = "360p";        //3GPP (MPEG-4 encoded) Medium quality
        public static final CharSequence MP4_NORMAL_TITLE = "480p";        //MP4  (H.264 encoded) Normal quality
        public static final CharSequence MP4_HIGH_TITLE = "720p HD";        //MP4  (H.264 encoded) High quality
        public static final CharSequence MP4_HIGH1_TITLE = "1080p HD";        //MP4  (H.264 encoded) High quality

        public static final int[] supported = {
                GPP3_LOW,
                GPP3_MEDIUM,
                MP4_NORMAL,
                MP4_HIGH,
                MP4_HIGH1
        };

        public static final CharSequence[] supported_titles = {
                GPP3_LOW_TITLE,
                GPP3_MEDIUM_TITLE,
                MP4_NORMAL_TITLE,
                MP4_HIGH_TITLE,
                MP4_HIGH1_TITLE
        };

    }
}
