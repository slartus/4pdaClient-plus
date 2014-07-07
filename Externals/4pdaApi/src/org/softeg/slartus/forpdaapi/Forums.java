package org.softeg.slartus.forpdaapi;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:41
 */
public class Forums extends ArrayList<Forum> {
    /**
     * Загрузка дерева разделов форума
     *
     * @param httpClient
     * @return
     * @throws Exception
     */
    public static Forum loadForums(IHttpClient httpClient) throws Exception, NotReportException {


        Forum mainForum = new Forum("-1", "4PDA");
        byte tryCount = 3;// Не всегда загружается в текстовом режиме. будем пробовать 3 раза.
        Boolean regimeChecked = false;
        while (tryCount > 0) {
            String pageBody = httpClient.performGet("http://4pda.ru/forum/lofiversion/index.php");


            String[] strings = pageBody.split("\n");
            pageBody = null;
            Pattern checkRegimePattern = Pattern.compile("<div id='largetext'>Полная версия этой страницы");
            Pattern forumPattern = Pattern.compile("<a href='http://4pda.ru/forum/lofiversion/index.php\\?f(\\d+).html'>(.*?)</a>");

            Forum forum = mainForum;

            for (String str : strings) {
//                regimeChecked = regimeChecked || checkRegimePattern.matcher(str).find();
//                Matcher m = forumPattern.matcher(str);
//                if (m.find()) {
//                    if (forum.getParent() != null && forum.getParent() != mainForum && forum.getForums().size() == 0)
//                        forum.addForum(new Forum(forum.getId(), forum.getTitle() + " @ темы"));
//                    forum.addForum(new Forum(m.group(1), Html.fromHtml(m.group(2)).toString()));
//                } else if (str.endsWith("<ul>")) {
//                    forum = forum.getLastChild();
//                } else if (str.trim().startsWith("</ul></li>")) {
//                    forum = forum.getParent();
//                    if (forum == null)
//                        forum = mainForum;
//                }
            }
            if (!regimeChecked)
                tryCount--;
            else
                break;
        }
        if (!regimeChecked)
            throw new NotReportException("Страница загрузилась не в текстовом режиме! Попробуйте залогиниться");

        return mainForum;
    }

    public static void markAllAsRead(IHttpClient httpClient) throws Throwable {
        httpClient.performGet("http://4pda.ru/forum/index.php?act=Login&CODE=05");
    }

    public static void markForumAsRead(IHttpClient httpClient, CharSequence forumId) throws Throwable {

        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "login"));
        qparams.add(new BasicNameValuePair("CODE", "04"));
        qparams.add(new BasicNameValuePair("f", forumId.toString()));
        qparams.add(new BasicNameValuePair("fromforum", forumId.toString()));


        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);

        httpClient.performGet(uri.toString());
    }
}
