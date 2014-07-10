package org.softeg.slartus.forpdaapi.qms;

import android.text.Html;

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

            Matcher m = Pattern.compile("<span class=\"navbar-title\">\\s*?<a href=\"/forum/index.php\\?showuser=\\d+\" target=\"_blank\"><strong>(.*?):</strong></a>([\\s\\S]*?)\\s*?</span>")
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
        Matcher m = Pattern.compile("<div class=\"scrollframe-body\">[\\s\\S]*?<div id=\"thread-inside-top\"></div>([\\s\\S]*?)<div id=\"thread-inside-bottom\">").matcher(pageBody);
        if (m.find())
            return "<div id=\"thread_form\"><div id=\"thread-inside-top\"></div>" + m.group(1) + "</div>";

        m = Pattern.compile("<div class=\"list_item\" t_id=([\\s\\S]*?)</form>").matcher(pageBody);
        if (m.find())
            return "<div id=\"thread_form\"><div class=\"list_item\" t_id=" + m.group(1) + "</div>";

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
        Matcher m = Pattern.compile("<a class=\"list-group-item[^\"]*\"[^>]*?data-member-id=\"(\\d+)\"[^>]*?>([\\s\\S]*?)</a>", Pattern.CASE_INSENSITIVE).matcher(pageBody);
        Pattern newMessagesCountPattern = Pattern.compile("<div class=\"bage[^\"]*\">\\((\\d+)\\)</div>", Pattern.CASE_INSENSITIVE);
        Pattern userPattern = Pattern.compile("<img class=\"avatar\" src=\"([^\"]*)\" title=\"([^\"]*)\" alt=\"\" />");
        while (m.find()) {
            QmsUser qmsUser = new QmsUser();
            qmsUser.setId(m.group(1));
            Matcher countMatcher = newMessagesCountPattern.matcher(m.group(2));
            if (countMatcher.find())
                qmsUser.setNewMessagesCount(countMatcher.group(1));
            Matcher userMatcher = userPattern.matcher(m.group(2));
            if (userMatcher.find()) {
                qmsUser.setNick(Html.fromHtml(userMatcher.group(2)).toString().trim());
                qmsUser.setAvatarUrl(userMatcher.group(1));
            }

            res.add(qmsUser);
        }
        return res;
    }

    public static QmsUserThemes getQmsUserThemes(IHttpClient httpClient, String mid,
                                                 ArrayList<QmsUser> outUsers, Boolean parseNick) throws Throwable {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?act=qms&mid=" + mid);
        Matcher m = Pattern.compile("<a class=\"list-group-item[^\"]*\"[^>]*?data-thread-id=\"(\\d+)\"[^>]*?>([\\s\\S]*?)</a>", Pattern.CASE_INSENSITIVE).matcher(pageBody);
        Pattern themePattern = Pattern.compile("<div class=\"bage[^\"]*\">([^<]*?)</div>\n" +
                "\\s*(?:<strong>)?(.*?)\\s+\\((\\d+)(?: / (\\d+))?\\)(?:</strong>)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        QmsUserThemes res = new QmsUserThemes();
        while (m.find()) {
            QmsUserTheme item = new QmsUserTheme();
            item.Id = m.group(1);
            Matcher themeMatcher = themePattern.matcher(m.group(2));
            if (themeMatcher.find()) {
                item.Date = themeMatcher.group(1);
                item.Title = themeMatcher.group(2);
                item.Count = themeMatcher.group(3);
                if (themeMatcher.group(4) != null)
                    item.NewCount = themeMatcher.group(4);
            }

            res.add(item);
        }
        outUsers.addAll(parseQmsUsers(pageBody));
        if (parseNick) {
            String[] nickPatterns = {
                    "<span class=\"navbar-title\">\\s*?<a href=\"/forum/index.php\\?showuser=\\d+\" target=\"_blank\"><strong>(.*?)</strong>",
                    "src=\"http://s.4pda.ru/forum/style_images/qms/back.png\"[\\s\\S]*?<strong>(.*?)</strong></a>",
                    "<span class=\"title\">Диалоги с: (.*?)</span>",
                    "<span class=\"navbar-title\">\\s*<strong>(.*?)</strong>"
            };
            for (String pattern : nickPatterns) {
                m = Pattern.compile(pattern).matcher(pageBody);
                if (m.find()) {
                    res.Nick = Html.fromHtml(m.group(1)).toString();
                    break;
                }
            }
        }
        return res;
    }

    public static int getNewQmsCount(String pageBody) {
        final Pattern qms_2_0_Pattern = PatternExtensions.compile("id=\"events-count\">Сообщений:\\s+(\\d+)</a>");
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
