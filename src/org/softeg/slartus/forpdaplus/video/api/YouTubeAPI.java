package org.softeg.slartus.forpdaplus.video.api;

import android.text.Html;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils;
import org.softeg.slartus.forpdaplus.video.api.exceptions.ApiException;
import org.softeg.slartus.forpdaplus.video.api.exceptions.IdException;
import org.softeg.slartus.forpdacommon.Http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeAPI {
    public static final CharSequence ID = "youtube.com";
    public static final String URL_GET_VIDEO_INFO = "http://www.youtube.com/get_video_info?&video_id=";

    public static CharSequence getYoutubeId(CharSequence youtubeUrl) {
        String[] patterns = {
                "v=([^&?#]*)",
                "v/([^&?#/]*)",
                "youtu.be/([^/?&#]*)",
                "vnd.youtube:([^/?&#]*)",
                "embed/([^&?#]*)",
        };
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(youtubeUrl);
            if (m.find())
                return m.group(1);
        }
        return null;
    }

    public static boolean isListUrl(CharSequence url) {
        return Pattern.compile("youtube.com/channel/", Pattern.CASE_INSENSITIVE).matcher(url).find();
    }

    private static boolean parse2(ParseResult info) throws Exception {

        String page = Http.getPage(String.format("https://gdata.youtube.com/feeds/api/videos/%s?v=2&alt=json", info.getId()), "UTF-8");

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
            if (url == null)
                return false;
            info.setVideoUrl(url);


            return true;
        } catch (Throwable ex) {
            return false;
        }

    }

    public static ParseResult getInfo(String url, Boolean youtubeInstalled) throws Exception {
        url = URLDecoder.decode(url.toString(), "UTF-8");

        ParseResult info = new ParseResult();
        info.setId(url);
        info.setTitle(url);
        info.setRequestUrl(url);

        if (isListUrl(url)) {
            new ApiException(ID, "Не умею открывать ссылки на канал");
            return info;
        }

        CharSequence id = getYoutubeId(url);
        if (TextUtils.isEmpty(id))
            throw new IdException("youtube");
        info.setId(id);
        info.setTitle(id);

        url = URL_GET_VIDEO_INFO + id;
        info.setVideoUrl(url);

        String infoStr = Http.getPage(url.toString(), "UTF-8");


        Matcher m = Pattern.compile("([^&=]*)=([^$&]*)", Pattern.CASE_INSENSITIVE).matcher(infoStr);

        String fmtList = null;
        String url_encoded_fmt_stream_map = null;
        String title = null;
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
        }
        if (!TextUtils.isEmpty(error)) {
            if (!youtubeInstalled && parse2(info))
                return info;
            throw new ApiException(ID, error);
        }
        info.setTitle(title);

        if (fmtList != null && url_encoded_fmt_stream_map != null) {
            Matcher fmtMatcher = Pattern.compile("(\\d+)/(\\d+x\\d+)/\\d+/\\d+/\\d+").matcher(fmtList);
            String streamStrs[] = url_encoded_fmt_stream_map.split(",");
            int fmtInd = 0;
            while (fmtMatcher.find()) {
                fmtInd++;
                int ind = ArrayUtils.indexOf(Integer.parseInt(fmtMatcher.group(1)), YouTubeFMTQuality.supported);
                if (ind == -1) continue;
                VideoFormat videoFormat = new VideoFormat();
                videoFormat.setTitle(YouTubeFMTQuality.supported_titles[ind]);
                videoFormat.setUrl(getUrlFromParams(info.getId(), streamStrs[fmtInd - 1]));

                info.getFormats().add(videoFormat);
            }
        }

        return info;

    }


    public static String getYouTubeUrl(int youTubeFmt, boolean fallback, ArrayList<VideoFormat> formats)
            throws IOException {
//        VideoFormat searchFormat = new VideoFormat(youTubeFmt);
//
//        while (!formats.contains(searchFormat) && fallback) {
//            int oldId = searchFormat.getId();
//            Log.e("format", "format: " + oldId);
//            int newId = YouTubeFMTQuality.getPreviousSupportedFormat(oldId);
//
//            if (oldId == newId)
//                break;
//
//            searchFormat = new VideoFormat(newId);
//        }
//        if (formats.contains(searchFormat))
//            return formats.get(formats.indexOf(searchFormat)).getUrl();
        return null;
    }

    private static CharSequence getUrlFromParams(CharSequence id, CharSequence params) throws UnsupportedEncodingException {

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
}
