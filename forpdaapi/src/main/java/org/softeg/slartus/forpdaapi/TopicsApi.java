package org.softeg.slartus.forpdaapi;

import android.net.Uri;
import androidx.core.util.Pair;
import android.text.Html;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.search.SearchApi;
import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.forpdacommon.HttpHelper;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.URIUtils;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;

/*
 * Created by slinkin on 20.02.14.
 */
public class TopicsApi {

    private final static Pattern countPattern = PatternExtensions.compile("<a href=\"/forum/index.php\\?act=[^\"]*?st=(\\d+)\">&raquo;</a>");
    private final static Pattern mainPattern = PatternExtensions.compile("cat_name[\\s\\S]*?</div>([\\s\\S]*<br />)<div class=\"forum_mod_funcs\">");
    private final static Pattern topicsPattern = PatternExtensions.compile("(<div data-item-fid[\\s\\S]*?</script></div></div>)");
    private final static Pattern topicPattern = PatternExtensions.compile("<div data-item-fid=\"(\\d*)\" data-item-track=\"(\\w*)\" data-item-pin=\"(\\d)\"[\\s\\S]*?<a href=\"([^\"]*?)\"[^>]*>(<strong>|)([\\s\\S]*?)(<\\/strong>|)<\\/a>[\\s\\S]*?<span class=\"topic_desc\">([\\s\\S]*?)(<[\\s\\S]*?showforum=(\\d*?)\"[\\s\\S]*?)<a href=\"[^\"]*view=getlastpost[^\"]*\">Послед.:<\\/a>\\s*<a href=\"[^\"]*?\\/forum\\/index.php\\?showuser=\\d+\">(.*?)<\\/a>(.*?)<");

    public static ArrayList<FavTopic> getFavTopics(
            ListInfo listInfo) throws IOException {
        return getFavTopics(null, null, null, null, false, false, listInfo);
    }

    public static ArrayList<FavTopic> getFavTopics(String sortKey,
                                                   String sortBy,
                                                   String pruneDay,
                                                   String topicFilter,
                                                   Boolean unreadInTop,
                                                   Boolean fullPagesList,
                                                   ListInfo listInfo) throws IOException {
        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "fav"));
        qparams.add(new BasicNameValuePair("type", "topics"));
        if (sortKey != null)
            qparams.add(new BasicNameValuePair("sort_key", sortKey));
        if (sortBy != null)
            qparams.add(new BasicNameValuePair("sort_by", sortBy));
        if (pruneDay != null)
            qparams.add(new BasicNameValuePair("prune_day", pruneDay));
        if (topicFilter != null)
            qparams.add(new BasicNameValuePair("topicfilter", topicFilter));
        qparams.add(new BasicNameValuePair("st", Integer.toString(listInfo.getFrom())));


        String uri = URIUtils.createURI("http", HostHelper.getHost(), "/forum/index.php",
                qparams, "UTF-8");

        String pageBody = HttpHelper.performGet(uri).getResponseBody();

        Matcher m = countPattern.matcher(pageBody);
        if (m.find()) {
            listInfo.setOutCount(Integer.parseInt(m.group(1)) + 1);
        }

        ArrayList<FavTopic> res = new ArrayList<>();
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        int sortOrder = 1000 + listInfo.getFrom() + 1;

        m = mainPattern.matcher(pageBody);
        Matcher tmp;
        if (m.find()) {
            m = topicsPattern.matcher(m.group(1));
            while (m.find()) {
                tmp = topicPattern.matcher(m.group(1));
                if (tmp.find()) {
                    String tId = tmp.group(1);
                    String trackType = tmp.group(2);
                    Boolean pinned = "1".equals(tmp.group(3));
                    Uri ur = Uri.parse(tmp.group(4));

                    if (TextUtils.isEmpty(ur.getQueryParameter("showtopic"))) {
                        FavTopic topic = new FavTopic(null, tmp.group(6));
                        topic.setTid(tId);
                        topic.setPinned(pinned);
                        topic.setTrackType(trackType);
                        topic.setDescription("Форум");
                        topic.setSortOrder(Integer.toString(sortOrder++));
                        res.add(topic);
                        continue;
                    }

                    String id = ur.getQueryParameter("showtopic");
                    String title = tmp.group(6);
                    FavTopic topic = new FavTopic(id, title);
                    topic.setTid(tId);
                    topic.setPinned(pinned);
                    topic.setTrackType(trackType);
                    if (TextUtils.isEmpty(tmp.group(8)))
                        topic.setDescription(tmp.group(9).replaceFirst("<span class=\"topic_desc\"[\\s\\S]*$", "").replaceAll("<a[^>]*?>([\\s\\S]*?)</a>", "$1"));
                    else
                        topic.setDescription(tmp.group(8));
                    topic.setIsNew(tmp.group(9).contains("view=getnewpost"));
                    topic.setForumId(tmp.group(10));
                    topic.setLastMessageAuthor(tmp.group(11));
                    topic.setLastMessageDate(Functions.parseForumDateTime(tmp.group(12), today, yesterday));
                    topic.setSortOrder(Integer.toString(sortOrder++));
                    res.add(topic);
                }
            }
        }

        if (fullPagesList) {
            while (true) {
                if (listInfo.getOutCount() <= res.size())
                    break;
                listInfo.setFrom(res.size());
                ArrayList<FavTopic> nextPageTopics = getFavTopics(sortKey, sortBy, pruneDay, topicFilter, false, false, listInfo);
                if (nextPageTopics.size() == 0)
                    break;
                res.addAll(nextPageTopics);
            }
        }
        if (unreadInTop) {
            final int asc = -1;// новые вверху
            Collections.sort(res, new Comparator<Topic>() {
                @Override
                public int compare(Topic topic, Topic topic2) {
                    if (topic.getState() == topic2.getState())
                        return 0;
                    return (topic.getState() == Topic.FLAG_NEW) ? asc : (-asc);

                }
            });
        }
        if (res.size() == 0) {
            m = PatternExtensions.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>")
                    .matcher(pageBody);
            if (m.find()) {
                throw new NotReportException(Html.fromHtml(m.group(1)).toString(), new Exception(Html.fromHtml(m.group(1)).toString()));
            }
        }
        return res;
    }

    public static Pair<String, ArrayList<Topic>> getForumTopics(String url,
                                                                String forumId,

                                                                Boolean unreadInTop,
                                                                ListInfo listInfo) throws IOException {

        AppResponse appResponse = Http.Companion.getInstance().performGetFull(url);
        String pageBody = appResponse.getResponseBody();
        if (pageBody == null)
            pageBody = "";
        ArrayList<Topic> res = new ArrayList<>();

        if ((HttpHelper.getRedirectUri() != null && HttpHelper.getRedirectUri().toString().toLowerCase().contains("act=search"))
                || url.toLowerCase().contains("act=search")) {
            res = SearchApi.INSTANCE.parse(pageBody, listInfo);
        } else {
            int start = listInfo.getFrom();
            Pattern lastPageStartPattern = Pattern.compile("<a href=\"(https?://"+ HostHelper.getHost() +")?/forum/index.php\\?showforum=\\d[^\"]*?st=(\\d+)",
                    Pattern.CASE_INSENSITIVE);

            String today = Functions.getToday();
            String yesterday = Functions.getYesterToday();
            Pattern idPattern = Pattern.compile("showtopic=(\\d+)", Pattern.CASE_INSENSITIVE);

            Document doc = Jsoup.parse(pageBody);
            Elements trElements = doc.select("table:has(th:contains(Название темы)) tr");
            int sortOrder = 1000 + start + 1;
            for (Element trElement : trElements) {
                if (trElement.children().size() < 3)
                    continue;
                Element tdElement = trElement.child(2);
                Element el = tdElement.select("a[id~=tid-link]").last();
                if (el == null)
                    continue;
                Matcher m = idPattern.matcher(el.attr("href"));
                if (!m.find())
                    continue;
                Topic theme = new Topic(m.group(1), el.text());
                el = tdElement.select("span[id~=tid-desc]").first();
                if (el != null)
                    theme.setDescription(el.text());
                theme.setIsNew(tdElement.select("a[href*=view=getnewpost]").first() != null);

                theme.setForumId(forumId);


                el = trElement.select("span.lastaction").first();
                if (el != null) {
                    theme.setLastMessageDate(Functions.parseForumDateTime(el.ownText(), today, yesterday));
                }
                el = trElement.select("span.lastaction a[href*=showuser=]").first();
                if (el != null) {
                    theme.setLastMessageAuthor(el.text());
                }
                theme.setSortOrder(Integer.toString(sortOrder++));
                res.add(theme);
            }

            Matcher m = lastPageStartPattern.matcher(pageBody);
            while (m.find()) {
                listInfo.setOutCount(Math.max(Integer.parseInt(m.group(2)), listInfo.getFrom()));
            }
        }
        if (unreadInTop) {
            final int asc = -1;// новые вверху
            Collections.sort(res, new Comparator<Topic>() {
                @Override
                public int compare(Topic topic, Topic topic2) {
                    if (topic.getState() == topic2.getState())
                        return 0;
                    return (topic.getState() == Topic.FLAG_NEW) ? asc : (-asc);

                }
            });
        }
        if (res.size() == 0) {
            Matcher m = PatternExtensions.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>")
                    .matcher(pageBody);
            if (m.find()) {
                throw new NotReportException(Html.fromHtml(m.group(1)).toString(), new Exception(Html.fromHtml(m.group(1)).toString()));
            }
        }
        return new Pair<>(appResponse.redirectUrlElseRequestUrl(), res);
    }

}
