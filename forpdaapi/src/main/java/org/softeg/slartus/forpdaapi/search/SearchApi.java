package org.softeg.slartus.forpdaapi.search;/*
 * Created by slinkin on 29.04.2014.
 */

import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdacommon.Functions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchApi {
    /**
     * @param searchUrl - номер страницы из
     * @throws MalformedURLException
     */
    public static ArrayList<Topic> getSearchTopicsResult(IHttpClient client, String searchUrl, ListInfo listInfo)
            throws IOException {
        int st = 0;
        Matcher m = Pattern.compile("st=(\\d+)", Pattern.CASE_INSENSITIVE).matcher(searchUrl);
        if (m.find())
            st = Integer.parseInt(m.group(1));
        st += listInfo.getFrom();

        String body = client.performGet(searchUrl.replaceAll("st=\\d+", "") + "&st=" + st);


        return parse(body, listInfo);
    }

    public static ArrayList<Topic> parse(String body, ListInfo listInfo) {
        ArrayList<Topic> res = new ArrayList<>();
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        Matcher matcher = Pattern.compile("<div data-topic=\"([^\"]*?)\"[\\s\\S]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[\\s\\S]*?<div class=\"topic_body\"><span class=\"topic_desc\">(?:([\\s\\S]*?)(?:<br[^>]*?>)?)?форум[^<]*?<a href=\"[^\"]*?showforum=(\\d+)\">([\\s\\S]*?)<\\/a><br[^>]*?><\\/span>[\\s\\S]*?showuser=(\\d+)\">([\\s\\S]*?)<\\/a><\\/span><br[^>]*?>(<a href=\"[^\"]*?getnewpost[\\s\\S]*?<\\/a>)?[\\s\\S]*?<a href=\"[^\"]*?showuser[^>]*?>([\\s\\S]*?)<\\/a> ([\\s\\S]*?)<\\/div><\\/div>").matcher(body);
        String desc;
        Topic topic;
        while (matcher.find()) {
            topic = new Topic();
            topic.setId(matcher.group(1));
            topic.setTitle(matcher.group(2), true);

            //Check!
            desc = matcher.group(3);
            topic.setDescription(desc == null ? "" : desc, true);

            topic.setForumId(matcher.group(4));
            topic.setForumTitle(matcher.group(5));

            topic.setIsNew(matcher.group(8) != null);
            topic.setLastMessageAuthor(matcher.group(9));
            topic.setLastMessageDate(Functions.parseForumDateTime(matcher.group(10), today, yesterday));
            topic.setId(matcher.group(1));
            topic.setId(matcher.group(1));
            topic.setId(matcher.group(1));
            res.add(topic);
        }

        Pattern pagesCountPattern = Pattern.compile("<a href=\"/forum/index.php[^\"]*st=(\\d+)\">", Pattern.CASE_INSENSITIVE);
        matcher = pagesCountPattern.matcher(body);
        while (matcher.find()) {
            listInfo.setOutCount(Math.max(Integer.parseInt(matcher.group(1)) + 1, listInfo.getOutCount()));
        }

        return res;
    }

}
