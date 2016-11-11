package org.softeg.slartus.forpdaapi;

import android.net.Uri;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.classes.ForumsData;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:41
 */
public class ForumsApi extends ArrayList<Forum> {

    /**
     * Загрузка дерева разделов форума
     */
    public static ForumsData loadForums(IHttpClient httpClient, ProgressState progressState)
            throws Exception {
        ForumsData res = new ForumsData();

        String pageBody = httpClient.performGetFullVersion("http://4pda.ru/forum/index.php?act=idx");
        Document doc = Jsoup.parse(pageBody, "http://4pda.ru");
        Elements categoryElements = doc.select("div.borderwrap[id~=fo_\\d+]");

        for (Element catElement : categoryElements) {
            progressState.update("Обновление структуры форума...",
                    res.getItems().size());
            Element el = catElement.select("div.maintitle a[href~=showforum=\\d+]").first();

            if (el == null) continue;
            Uri uri = Uri.parse(el.attr("href"));
            Forum forum = new Forum(uri.getQueryParameter("showforum"), el.text());


            forum.setHasTopics(false);

            forum.setDescription(null);
            res.getItems().add(forum);
            int c = res.getItems().size();

            loadCategoryForums(httpClient, catElement.select("table.ipbtable>tbody").first(), forum,
                    res, progressState);
            if (res.getItems().size() > c)
                forum.setIconUrl(res.getItems().get(c).getIconUrl());
        }


        return res;
    }

    public static void loadCategoryForums(IHttpClient httpClient, Element boardForumRowElement, Forum parentForum,
                                          ForumsData data, ProgressState progressState) throws Exception {
        if (boardForumRowElement == null)
            return;

        Elements categoryElements = boardForumRowElement.select("tr:has(td)");
        if (categoryElements.size() > 0)
            parentForum.setHasForums(true);
        for (Element trElement : categoryElements) {
            progressState.update("Обновление структуры форума...",
                    data.getItems().size());

            Elements tdElements = trElement.children();

            if (tdElements.size() < 5) continue;


            Element tdElement = tdElements.get(0);
            Element el = tdElement.select("img").first();
            String iconUrl = null;
            if (el != null)
                iconUrl = el.attr("src");

            tdElement = tdElements.get(1);

            el = tdElement.select("b>a").first();
            if (el == null)
                continue;
            Uri uri = Uri.parse(el.absUrl("href"));
            Forum forum = new Forum(uri.getQueryParameter("showforum"), el.text());
            forum.setIconUrl(iconUrl);
            forum.setHasTopics(true);
            forum.setParentId(parentForum.getId());
            data.getItems().add(forum);

            el = tdElement.select("span.forumdesc").first();
            if (el != null) {
                forum.setDescription(el.ownText());
                if (el.select("a[href~=showforum=\\d+]").size() > 0) {
                    loadSubForums(httpClient, uri.toString(), forum, data, progressState);
                }
            }
        }
    }

    public static void loadSubForums(IHttpClient httpClient, String url, Forum parentForum,
                                     ForumsData data, ProgressState progressState) throws Exception {
        String pageBody = httpClient.performGetFullVersion(url);
        Document doc = Jsoup.parse(pageBody, "http://4pda.ru");
        Element catElement = doc.select("div.borderwrap[id~=fo_\\d+]").first();
        if (catElement == null) return;
        Element boardForumRowElement = catElement.select("table.ipbtable>tbody").first();
        if (boardForumRowElement == null) return;


        Elements categoryElements = boardForumRowElement.select("tr:has(td)");
        if (categoryElements.size() > 0)
            parentForum.setHasForums(true);
        for (Element trElement : categoryElements) {
            progressState.update("Обновление структуры форума...",
                    data.getItems().size());
            Elements tdElements = trElement.children();
            if (tdElements.size() < 5) continue;

            Element tdElement = tdElements.get(0);
            Element el = tdElement.select("img").first();
            String iconUrl = null;
            if (el != null)
                iconUrl = el.attr("src");

            tdElement = tdElements.get(1);

            el = tdElement.select("b>a").first();
            if (el == null)
                continue;
            Uri uri = Uri.parse(el.absUrl("href"));
            Forum forum = new Forum(uri.getQueryParameter("showforum"), el.text());
            forum.setIconUrl(iconUrl);
            forum.setHasTopics(true);
            forum.setParentId(parentForum.getId());
            data.getItems().add(forum);

            el = tdElement.select("span.forumdesc").first();
            if (el != null) {
                forum.setDescription(el.ownText());
                if (el.select("a[href~=showforum=\\d+]").size() > 0) {
                    loadSubForums(httpClient, uri.toString(), forum, data, progressState);
                }
            }

        }
    }

    public static void markAllAsRead(IHttpClient httpClient) throws Throwable {
        httpClient.performGet("http://4pda.ru/forum/index.php?act=Login&CODE=05", true, false);
    }

    public static void markForumAsRead(IHttpClient httpClient, CharSequence forumId) throws Throwable {

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("act", "login"));
        qparams.add(new BasicNameValuePair("CODE", "04"));
        qparams.add(new BasicNameValuePair("f", forumId.toString()));
        qparams.add(new BasicNameValuePair("fromforum", forumId.toString()));


        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);

        httpClient.performGet(uri.toString());
    }
}
