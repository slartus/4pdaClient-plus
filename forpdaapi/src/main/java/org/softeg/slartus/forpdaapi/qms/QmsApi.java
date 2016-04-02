package org.softeg.slartus.forpdaapi.qms;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slartus on 02.03.14.
 */
public class QmsApi {


    public static String getChatPage(IHttpClient httpClient, String mid, String themeId) throws Throwable {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("xhr", "body");
        return httpClient.performPost("http://4pda.ru/forum/index.php?act=qms&mid=" + mid + "&t=" + themeId, additionalHeaders);
    }

    public static String getChat(IHttpClient httpClient, String mid, String themeId) throws Throwable {

        return getChat(httpClient, mid, themeId, null);
    }

    private static void checkChatError(String pageBody) throws Exception {

        Matcher m = Pattern.compile("<div class=\"error\">([\\s\\S]*?)</div>").matcher(pageBody);
        if (m.find()) {
            throw new Exception(Html.fromHtml(m.group(1)).toString());
        }

    }

    public static String getChat(IHttpClient httpClient, String mid, String themeId, Map<String, String> additionalHeaders) throws Throwable {
        String pageBody = getChatPage(httpClient, mid, themeId);
        checkChatError(pageBody);
        if (additionalHeaders != null) {

            Matcher m = Pattern.compile("<span class=\"navbar-title\">\\s*?<a href=\"[^\"]*/forum/index.php\\?showuser=\\d+\"[^>]*><strong>(.*?):</strong></a>([\\s\\S]*?)\\s*?</span>")
                    .matcher(pageBody);
            if (m.find()) {
                additionalHeaders.put("Nick", Html.fromHtml(m.group(1)).toString());
                additionalHeaders.put("ThemeTitle", Html.fromHtml(m.group(2)).toString());
                // break;
            }

        }
        return matchChatBody(pageBody);
    }

    private static String matchChatBody(String pageBody) {
        String chatInfo = "";
        Matcher m = Pattern.compile("<span class=\"nav-text\"[\\s\\S]*?<a href=\"[^\"]*showuser[^>]*>([^>]*?)</a>:</b>([^<]*)").matcher(pageBody);
        if(m.find())
            chatInfo = "<span id=\"chatInfo\" style=\"display:none;\">"+m.group(1).trim()+"|:|"+m.group(2).trim()+"</span>";
        m = Pattern.compile("<div id=\"thread-inside-top\"><\\/div>([\\s\\S]*)<div id=\"thread-inside-bottom\">").matcher(pageBody);
        if (m.find())
            return chatInfo+"<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>" + m.group(1) + "</div>";

        m = Pattern.compile("<div class=\"list_item\" t_id=([\\s\\S]*?)</form>").matcher(pageBody);
        if (m.find())
            return chatInfo+"<div id=\"thread_form\"><div class=\"list_item\" t_id=" + m.group(1) + "</div>";

        // ни одного сообщения
        m = Pattern.compile("</form>\\s*<div class=\"form\">").matcher(pageBody);
        if (m.find())
            return "<div id=\"thread_form\"></div>";
        m = Pattern.compile("<script>try\\{setTimeout \\( function\\(\\)\\{ updateScrollbar \\( \\$\\(\"#thread_container>.scrollbar_wrapper\"\\), \"bottom\" \\); \\}, 1 \\);\\}catch\\(e\\)\\{\\}</script>\\s*</div>")
                .matcher(pageBody);
        if (m.find())
            return "<div id=\"thread_form\"></div>";
        else
            pageBody = pageBody + "";
        return pageBody;
    }

    public static String sendMessage(IHttpClient httpClient, String mid, String tid, String message
            , String encoding) throws Throwable {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("action", "send-message");

        additionalHeaders.put("mid", mid);
        additionalHeaders.put("t", tid);

        additionalHeaders.put("message", message);
        httpClient.performPost("http://4pda.ru/forum/index.php?act=qms-xhr&",
                additionalHeaders, encoding);
        return getChat(httpClient, mid, tid);
    }

    public static String createThread(IHttpClient httpClient, String userID, String userNick, String title, String message,
                                      Map<String, String> outParams, String encoding) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("action", "create-thread");
        additionalHeaders.put("username", userNick);
        additionalHeaders.put("title", title);
        additionalHeaders.put("message", message);
        String pageBody = httpClient.performPost("http://4pda.ru/forum/index.php?act=qms&mid=" + userID + "&xhr=body&do=1", additionalHeaders, encoding);

        Matcher m = Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"mid\"\\s*value=\"(\\d+)\"\\s*/>").matcher(pageBody);
        if (m.find())
            outParams.put("mid", m.group(1));
        m = Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"t\"\\s*value=\"(\\d+)\"\\s*/>").matcher(pageBody);
        if (m.find())
            outParams.put("t", m.group(1));
        //  m = Pattern.compile("<strong>(.*?):\\s*</strong></a>\\s*(.*?)\\s*?</span>").matcher(pageBody);
        //if (m.find()) {
        outParams.put("user", userNick);
        outParams.put("title", title);
        //}
        if (outParams.size() == 0) {
            m = Pattern.compile("<div class=\"form-error\">(.*?)</div>").matcher(pageBody);
            if (m.find())
                throw new NotReportException(m.group(1));
        }
        return matchChatBody(pageBody);
    }

    public static void deleteDialogs(IHttpClient httpClient, String mid, List<String> ids) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("action", "delete-threads");
        additionalHeaders.put("title", "");
        additionalHeaders.put("message", "");
        for (String id : ids) {
            additionalHeaders.put("thread-id[" + id + "]", id);
        }
        httpClient.performPost("http://4pda.ru/forum/index.php?act=qms&xhr=body&do=1&mid=" + mid, additionalHeaders);
    }

    public static String deleteMessages(IHttpClient httpClient, String mid, String threadId, List<String> ids, String encoding) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "qms");
        additionalHeaders.put("mid", mid);
        additionalHeaders.put("t", threadId);
        additionalHeaders.put("xhr", "body");
        additionalHeaders.put("do", "1");
        additionalHeaders.put("action", "delete-messages");
        additionalHeaders.put("forward-messages-username", "");
        additionalHeaders.put("forward-thread-username", "");
        additionalHeaders.put("message", "");
        for (String id : ids) {
            additionalHeaders.put("message-id[" + id + "]", id);
        }

        return matchChatBody(httpClient.performPost("http://4pda.ru/forum/index.php?act=qms&mid" + mid + "&t=" + threadId + "&xhr=body&do=1", additionalHeaders, encoding));
    }

    public static ArrayList<QmsUser> getQmsSubscribers(IHttpClient httpClient) throws Throwable {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?&act=qms-xhr&action=userlist");
        return parseQmsUsers(pageBody);
    }

    public static ArrayList<QmsUser> parseQmsUsers(String pageBody) {
        ArrayList<QmsUser> res = new ArrayList<>();
        Matcher m = Pattern.compile("<a class=\"list-group-item[^>]*=(\\d*)\">[^<]*<div class=\"bage\">([^<]*)[\\s\\S]*?src=\"([^\"]*)\" title=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE).matcher(pageBody);
        String count;
        QmsUser qmsUser;
        while (m.find()) {
            qmsUser = new QmsUser();
            qmsUser.setId(m.group(1));
            qmsUser.setAvatarUrl(m.group(3));
            qmsUser.setNick(Html.fromHtml(m.group(4)).toString().trim());
            count = m.group(2).trim();
            if(!count.equals(""))
                qmsUser.setNewMessagesCount(count.replace("(", "").replace(")", ""));

            res.add(qmsUser);
        }
        return res;
    }

    public static QmsUserThemes getQmsUserThemes(IHttpClient httpClient, String mid,
                                                 ArrayList<QmsUser> outUsers, Boolean parseNick) throws Throwable {
        QmsUserThemes res = new QmsUserThemes();
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?act=qms&mid=" + mid);
        Pattern newCountPattern = Pattern.compile("(.*?)\\((\\d+)\\s*/\\s*(\\d+)\\)\\s*$");
        Pattern countPattern = Pattern.compile("(.*?)\\((\\d+)\\)\\s*$");
        Pattern strongPattern = Pattern.compile("<strong>([\\s\\S]*?)</strong>");
        Matcher matcher = Pattern.compile("<div class=\"list-group\">([\\s\\S]*)<form [^>]*>([\\s\\S]*?)<\\/form>").matcher(pageBody);
        if(matcher.find()){
            outUsers.addAll(parseQmsUsers(matcher.group(1)));
            matcher = Pattern.compile("<a class=\"list-group-item[^>]*-(\\d*)\">[\\s\\S]*?<div[^>]*>([\\s\\S]*?)<\\/div>([\\s\\S]*?)<\\/a>").matcher(matcher.group(2));
            QmsUserTheme item;
            Matcher m;
            String info;
            while (matcher.find()){
                item = new QmsUserTheme();
                item.Id = matcher.group(1);
                item.Date = matcher.group(2);

                info = matcher.group(3);
                m = strongPattern.matcher(info);
                if(m.find()){
                    m = newCountPattern.matcher(m.group(1));
                    if (m.find()) {
                        item.Title = m.group(1).trim();
                        item.Count = m.group(2);
                        item.NewCount = m.group(3);
                    } else
                        item.Title = m.group(2).trim();
                }else {
                    m = countPattern.matcher(info);
                    if (m.find()) {
                        item.Title = m.group(1).trim();
                        item.Count = m.group(2).trim();
                    } else
                        item.Title = info.trim();
                }
                res.add(item);
            }
            if (parseNick) {
                matcher = Pattern.compile("<div class=\"nav\">[\\s\\S]*?showuser[^>]*([\\s\\S]*?)<\\/a>[\\s\\S]*?<\\/div>").matcher(pageBody);
                if(matcher.find()){
                    res.Nick=matcher.group(1);
                }
            }
        }


        return res;
    }

    public static int getNewQmsCount(String pageBody) {
        final Pattern qms_2_0_Pattern = PatternExtensions.compile("id=\"events-count\"[^>]*>[^\\d]*?(\\d+)<");
        Matcher m = qms_2_0_Pattern.matcher(pageBody);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    public static int getNewQmsCount(IHttpClient client) throws IOException {
        String body = client.performGet("http://4pda.ru/forum/index.php?showforum=200");
        return getNewQmsCount(body);
    }
}
