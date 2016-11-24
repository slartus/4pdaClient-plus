package org.softeg.slartus.forpdaapi;

import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.softeg.slartus.forpdacommon.DateTimeExternals;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.UrlExtensions;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * Created by slinkin on 20.02.14.
 */
public class NewsApi {
    public static Boolean like(IHttpClient httpClient, String newsId) throws IOException {
        String res = httpClient.performGet("http://4pda.ru/wp-content/plugins/karma/ajax.php?p=" + newsId + "&c=0&v=1", false, false);
        return res != null;
    }

    public static Boolean likeComment(IHttpClient httpClient, String newsId, String postId) throws IOException {
        String res = httpClient.performGet("http://4pda.ru/wp-content/plugins/karma/ajax.php?p=" + newsId + "&c=" + postId + "&v=1", false, false);
        return res != null;
    }


    public static ArrayList<News> getNews(IHttpClient httpClient, String url, ListInfo listInfo) throws Exception {
        //http://4pda.ru/2013/page/7/
        //http://4pda.ru/2013/2/page/7/
        //http://4pda.ru/2013/2/2/page/7/
        //http://4pda.ru/tag/programs-for-ios/page/3
        //http://4pda.ru/page/5/
        //http://4pda.ru/page/5/?s=ios - поиск
        //http://4pda.ru/?s=%EF%EB%E0%ED%F8%E5%F2
        //http://4pda.ru/page/6/?s=%EF%EB%E0%ED%F8%E5%F2

        final int NEWS_PER_PAGE = 30;// 30 новостей на страницу выводит форум
        int pageNum = 1;
        String justUrl = url;// урл без страницы и параметров
        String params = "";// параметры, например, s=%EF%EB%E0%ED%F8%E5%F2
        // сначала проверим на поисковой урл
        Matcher m = Pattern.compile("(.*?)(?:page/+(\\d+)/+)?\\?(.*?)$", Pattern.CASE_INSENSITIVE)
                .matcher(url);
        if (m.find()) {
            justUrl = m.group(1);
            if (!TextUtils.isEmpty(m.group(2)))
                pageNum = Integer.parseInt(m.group(2));
            if (!TextUtils.isEmpty(m.group(3)))
                params = m.group(3);
        } else {
            m = Pattern.compile("(.*?)(?:page/+(\\d+)/+)?$", Pattern.CASE_INSENSITIVE)
                    .matcher(url);
            if (m.find()) {
                justUrl = m.group(1);
                if (!TextUtils.isEmpty(m.group(2)))
                    pageNum = Integer.parseInt(m.group(2));
            }
        }
        pageNum = (int) Math.ceil(listInfo.getFrom() / NEWS_PER_PAGE) + pageNum;
        String requestUrl = justUrl + "/page/" + pageNum + "/" + params;

        ArrayList<News> res = new ArrayList<>();
        String dailyNewsPage = httpClient.performGet(UrlExtensions.removeDoubleSplitters(requestUrl));

        Pattern articlesPattern = Pattern.compile("(<article class=\"post\"[^>]*>[\\s\\S]*?href=\"([^\"]*)\" title[\\s\\S]*?src=\"([^\"]*)\" alt=\"([^\"]*?)\"[\\s\\S]*?<\\/article>)|(<li itemscope[^>]*>[\\s\\S]*?itemprop=\"url\" href=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\" alt=\"([^\"]*?)\"[\\s\\S]*?<\\/div>[^<]*<\\/li>)");
        Pattern descriptionPattern = Pattern.compile("(<div itemprop=\"description\">[\\s\\S]*?<p [^>]*>([\\s\\S]*)<\\/p>[^<]*)|(<div itemprop=\"description\">([\\s\\S]*?)<\\/div>)");
        Pattern labelPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"label[^>]*>([\\s\\S]*?)<\\/a>");
        Pattern countPattern = Pattern.compile("class=\"v-count\"[^>]*>(\\d*)</a>");
        Pattern datePattern = Pattern.compile("<meta itemprop=\"datePublished\" content=\"(\\d+-\\d+-\\d+)[\\s\\S]*?\"\\/>");
        Pattern authorPattern = Pattern.compile("(<span class=\"autor\"><a [^>]*>([^<]*)</a>)|(<meta itemprop=\"author\" content=\"([^\"]*)\"/>)");

        m = articlesPattern.matcher(dailyNewsPage);
        Matcher matcher = null;

        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        News news;
        int group, childGroup;
        while (m.find()) {
            news = new News();
            group = 0;
            childGroup = 0;
            if (m.group(1) == null) group = 4;

            news.setId(m.group(group + 2).replace("http://4pda.ru", ""));
            news.setTitle(Html.fromHtml(m.group(group + 4).replaceAll("&amp;","&")).toString());
            news.setImgUrl(m.group(group + 3));


            if (matcher == null)
                matcher = descriptionPattern.matcher(m.group(group + 1));
            else
                matcher.usePattern(descriptionPattern).reset(m.group(group + 1));
            if (matcher.find()) {
                if (matcher.group(1) == null) childGroup = 2;
                news.setDescription(Html.fromHtml(matcher.group(childGroup + 2).replaceAll("<a [^>]*>([^<]*)</a>", "$1")).toString().trim());
            }
            childGroup = 0;

            matcher.usePattern(labelPattern).reset(m.group(group + 1));
            if (matcher.find()) {
                news.setTagLink(matcher.group(1));
                news.setTagTitle(Html.fromHtml(matcher.group(2).trim()));
            } else {
                news.setTagTitle("");
            }

            matcher.usePattern(countPattern).reset(m.group(group + 1));
            if (matcher.find()) {
                news.setCommentsCount(Integer.parseInt(matcher.group(1)));
            }

            matcher.usePattern(datePattern).reset(m.group(group + 1));
            if (matcher.find()) {
                news.setNewsDate(matcher.group(1));
            }

            matcher.usePattern(authorPattern).reset(m.group(group + 1));
            if (matcher.find()) {
                if (matcher.group(1) == null) childGroup = 2;
                news.setAuthor(matcher.group(childGroup + 2));
            }
            res.add(news);
        }
        if (res.size() == 0 && pageNum == 1 && listInfo.getFrom() == 0)
            return getNewsFromRss(httpClient, UrlExtensions.removeDoubleSplitters(url + "/feed/"));
        int lastPageNum = lastPageNum(dailyNewsPage);
        listInfo.setOutCount(res.size() * lastPageNum);
        return res;
    }

    private static String normalizeRss(String body) {
        return body.replaceAll("&(?!.{1,4};)", "&amp;");
    }

    public static ArrayList<News> getNewsFromRss(IHttpClient httpClient, String url) throws Exception {
        ArrayList<News> res = new ArrayList<>();
        try {


            String body = httpClient.performGet(url);
            if (TextUtils.isEmpty(body))
                throw new NotReportException("Сервер вернул пустую страницу!");


            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = dbf.newDocumentBuilder();

            body = normalizeRss(body);

            org.w3c.dom.Document document = db.parse(new InputSource(new StringReader(body)));

            Element element = document.getDocumentElement();

            NodeList nodeList = element.getElementsByTagName("item");

            if (nodeList.getLength() > 0) {

                for (int i = 0; i < nodeList.getLength(); i++) {

                    Element entry = (Element) nodeList.item(i);

                    Element _titleE = (Element) entry.getElementsByTagName("title").item(0);

                    Element _descriptionE = (Element) entry.getElementsByTagName("description").item(0);

                    Element _pubDateE = (Element) entry.getElementsByTagName("pubDate").item(0);

                    Element _linkE = (Element) entry.getElementsByTagName("link").item(0);


                    StringBuilder _title = new StringBuilder();
                    NodeList nodes = _titleE.getChildNodes();
                    int nodesLength = nodes.getLength();
                    for (int c = 0; c < nodesLength; c++) {
                        _title.append(nodes.item(c).getNodeValue());
                    }


                    //String _description = _descriptionE.getFirstChild().getNodeValue();
                    StringBuilder _description = new StringBuilder();
                    nodes = _descriptionE.getChildNodes();
                    nodesLength = nodes.getLength();
                    for (int c = 0; c < nodesLength; c++) {
                        _description.append(nodes.item(c).getNodeValue().replace("\n", " "));
                    }

                    Date _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());

                    String _link = _linkE.getFirstChild().getNodeValue();

                    String author = entry.getElementsByTagName("dc:creator").item(0).getChildNodes().item(0).getNodeValue();

                    News news = new News(Uri.parse(_link).getPath(), _title.toString());
                    news.setNewsDate(DateTimeExternals.getDateString(_pubDate));
                    news.setAuthor(author);
                    news.setDescription(_description.toString().replaceAll("(<img.*?/>)", ""));

                    res.add(news);

                }

            }

        } catch (Throwable ex) {
            Log.e("NewsApi", ex.toString());
        }
        return res;

    }

    private static int lastPageNum(String pagebody) {
        Matcher m = Pattern.compile("<ul class=\"page-nav\">.*href=\"[\\s\\S]*/+page/+(\\d+)/+\">\\d+.*?</ul>").matcher(pagebody);

        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }
}
