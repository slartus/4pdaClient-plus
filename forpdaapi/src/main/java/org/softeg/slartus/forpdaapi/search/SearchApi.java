package org.softeg.slartus.forpdaapi.search;/*
 * Created by slinkin on 29.04.2014.
 */

import android.text.Html;

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



        return parse(body,listInfo);
    }

    public static  ArrayList<Topic> parse(String body, ListInfo listInfo){
        Matcher m = Pattern.compile("<table class=\"ipbtable\" cellspacing=\"1\">([\\s\\S]*?)</table>",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(body);
        if (!m.find()) {
            return new ArrayList<Topic>();
        }

        ArrayList<Topic> res = new ArrayList<Topic>();
        Matcher trMatcher = Pattern.compile("<tr>([\\s\\S]*?)</tr>", Pattern.CASE_INSENSITIVE)
                .matcher(m.group(1));
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        while (trMatcher.find()) {
            Matcher tdMatcher = Pattern.compile("<td[^>]*>([\\s\\S]*?)</td>", Pattern.CASE_INSENSITIVE)
                    .matcher(trMatcher.group(1));
            int trInd = 0;
            Topic topic = null;
            while (tdMatcher.find()) {
                String tdBody = tdMatcher.group(1);

                switch (trInd++) {
                    case 2:
                        m = Pattern.compile("<a href=\"[^\"]*/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a>", Pattern.CASE_INSENSITIVE)
                                .matcher(tdBody);
                        if (!m.find()) break;
                        topic = new Topic();
                        topic.setId(m.group(1));
                        topic.setTitle(m.group(2));
                        topic.setIsNew(tdBody.contains("view=getnewpost"));
                        m = Pattern.compile("<span class=\"desc\">(.*?)</span>", Pattern.CASE_INSENSITIVE)
                                .matcher(tdBody);
                        if (m.find())
                            topic.setDescription(m.group(1));
                        break;
                    case 3:
                        if (topic == null)
                            break;
                        m = Pattern.compile("<a href=\"[^\"]*/forum/index.php\\?showforum=(\\d+)\"[^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE)
                                .matcher(tdBody);
                        if (m.find()) {
                            topic.setForumId(m.group(1));
                            topic.setForumTitle(Html.fromHtml(m.group(1)).toString());
                        }
                        break;
                    case 7:
                        if (topic == null)
                            break;
                        m = Pattern.compile("<span class=\"desc\">(.*?)<br /><a href=\"[^\"]*/forum/index.php\\?showtopic=\\d+&amp;view=getlastpost\">Послед.:</a> <b><a href=\"[^\"]*/forum/index.php\\?showuser=(\\d+)\">(.*?)</a>", Pattern.CASE_INSENSITIVE)
                                .matcher(tdBody);
                        if (m.find()) {
                            topic.setLastMessageDate(Functions.parseForumDateTime(m.group(1), today, yesterday));
                            topic.setLastMessageAuthor(m.group(3));
                        }
                        break;
                }
                if (trInd > 2 && topic == null)
                    break;
            }
            if (topic != null)
                res.add(topic);
            topic = null;
        }

        Pattern pagesCountPattern = Pattern.compile("<a href=\"/forum/index.php[^\"]*st=(\\d+)\">",Pattern.CASE_INSENSITIVE);
        m = pagesCountPattern.matcher(body);
        while (m.find()) {
            listInfo.setOutCount(Math.max(Integer.parseInt(m.group(1)) + 1, listInfo.getOutCount()));
        }

        return res;
    }

}
