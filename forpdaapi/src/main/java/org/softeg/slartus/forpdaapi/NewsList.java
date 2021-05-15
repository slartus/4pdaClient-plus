package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slinkin on 20.02.14.
 */
public class NewsList extends ArrayList<News> {
    private final IHttpClient mClient;
    private final CharSequence mSearchTag;

    private CharSequence mLastNewsUrl;
    private int mLastNewsPage;
    private int newsCountInt;

    /**
     * Сколько всего новостей на сайте
     *
     * @return
     */
    public int getNewsCount() {
        return newsCountInt;

    }

    /**
     * Возвращает из url тэг
     *
     * @param url
     * @return news, articles, software и тд. или пусто для "Все"
     */
    private static String getSearchTag(String url) {
        Matcher m = Pattern.compile(HostHelper.getHost() + "/tag/(.*?)(/|$)").matcher(url);
        if (m.find()) {
            return "tag/" + m.group(1) + "/";
        }
        return url;
    }

    /**
     * @param client
     * @param newsUrl - урл страницы новостей
     */
    public NewsList(IHttpClient client, String newsUrl) {

        mClient = client;
        mSearchTag = getSearchTag(newsUrl);
    }


    public void loadNextNewsPage() throws IOException, ParseException {

        if (size() == 0) {
            getPage(1, "https://" + HostHelper.getHost() + "/" + mSearchTag);
            return;
        }
        mLastNewsUrl = size() > 0 ? get(size() - 1).getId() : "";
        mLastNewsPage = size() > 0 ? get(size() - 1).getPage() : 0;
        CharSequence url = mLastNewsUrl;

        if (TextUtils.isEmpty(mSearchTag)) {
            Matcher m = Pattern.compile(HostHelper.getHost() + "/(\\d+)/(\\d+)/(\\d+)/(\\d+)").matcher(url);
            m.find();

            int year = Integer.parseInt(m.group(1));
            int nextPage = mLastNewsPage + 1;
            loadPage(year, nextPage, 0);
        } else {
            int nextPage = mLastNewsPage + 1;
            getPage(nextPage, "https://" + HostHelper.getHost() + "/" + mSearchTag + "page/" + nextPage);
        }


    }

    private void loadPage(int year, int nextPage, int iteration) throws IOException, ParseException {

        String dailyNewsUrl = "https://" + HostHelper.getHost() + "/" + year + "/page/" + nextPage;

        String dailyNewsPage = getPage(nextPage, dailyNewsUrl);

        if (size() == 0) {
            if (iteration > 0) return;
            if (dailyNewsPage.contains("По указанным параметрам не найдено ни одного поста"))
                loadPage(year - 1, 1, iteration + 1);
            else
                loadPage(year, nextPage + 1, iteration + 1);
        }
    }

    private int lastPageNum(String pagebody, int curPage) {
        Matcher m = Pattern.compile("<div class=\"wp-pagenavi\">.*<a href=\".*?/page/(\\d+)/\"\\s+class=\"page\".*?</div>").matcher(pagebody);

        if (m.find()) {
            int newsPerPage = size() / curPage;
            return Integer.parseInt(m.group(1)) * newsPerPage;
        }
        return getNewsCount();
    }

    private String getPage(int page, String newsUrl) throws IOException, ParseException {
        String dailyNewsPage = mClient.performGet(newsUrl).getResponseBody();
        Matcher postsMatcher = Pattern.compile("<div class=\"post\" id=\"post-\\d+\">([\\s\\S]*?)<span id=\"ka_\\d+_0_n\"></span></div><br /></div></div>")
                .matcher(dailyNewsPage);
        Boolean someUnloaded = false;// одна из новостей незагружена - значит и остальные
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");


        Pattern mPattern = Pattern.compile("<a href=\"(/\\d+/\\d+/\\d+/(\\d+))/\" rel=\"bookmark\" title=\"(.*?)\" alt=\"\">.*?</a></h2>");

        Pattern textPattern = Pattern.compile("<div class=\"entry\" id=\"[^\"]*\">" +
                "(?:<a href=\"([^\"]*)\" class=\"oprj ([^\"]*)\"><div>(.*?)</div>)?" +
                "([\\s\\S]*?)" +
                "(?:<noindex><span class=\"mb_source\">Источник:&nbsp;<a href=\"([^\"]*)\" target=\"[^\"]*\">(.*?)</a></span></noindex>)?" +
                "<br /><br /></div><div class=\"postmetadata\" id=\"ka_meta_\\d+_0\"><span id=\"ka_\\d+_0\"></span>&nbsp;\\|&nbsp;" +
                "<strong>(.*?)</strong>&nbsp;\\|\\s*(\\d+\\.\\d+\\.\\d+)\\s*\\|\\s*" +
                "<a href=\"/\\d+/\\d+/\\d+/\\d+/#comments\" title=\"[^\"]*\"><b class=\"spr pc\"></b>\\s*(\\d+)\\s*</a>");
        Pattern imagePattern = Pattern.compile("<center><img[^>]*?src=\"(.*?)\"");
        while (postsMatcher.find()) {
            String postData = postsMatcher.group(1);
            Matcher m = mPattern.matcher(postData);

            if (m.find()) {
                String id = "https://" + HostHelper.getHost() + m.group(1);

                if (!someUnloaded && findByTitle(id) != null) continue;
                someUnloaded = true;

                News news = new News(id, Html.fromHtml(m.group(3)).toString());

                Matcher textMatcher = textPattern.matcher(postData);
                if (textMatcher.find()) {
                    if (textMatcher.group(1) != null) {
                        news.setTagLink(textMatcher.group(1));
                        news.setTagName(textMatcher.group(2));
                        news.setTagTitle(textMatcher.group(3));
                    }
                    if (textMatcher.group(5) != null) {
                        news.setSourceUrl(textMatcher.group(5));
                        news.setSourceTitle(textMatcher.group(6));
                    }
                    news.setDescription(Html.fromHtml(removeDescriptionTrash(textMatcher.group(4))).toString());
                    news.setAuthor(Html.fromHtml(textMatcher.group(7)));
                    Date _pubDate = dateFormat.parse(textMatcher.group(8));
                    news.setNewsDate(Functions.getForumDateTime(_pubDate));
                    news.setCommentsCount(Integer.parseInt(textMatcher.group(9)));
                }

                Matcher imageMatcher = imagePattern.matcher(postData);
                if (imageMatcher.find()) {
                    news.setImgUrl(imageMatcher.group(1));
                }

                news.setPage(page);
                add(news);
            }
        }

        newsCountInt = Math.max(getNewsCount(), lastPageNum(dailyNewsPage, page));
        return dailyNewsPage;
    }

    /**
     * Удалить из краткого текста новости ссылки "читать дальше" и картинки
     *
     * @return
     */
    private static String removeDescriptionTrash(CharSequence description) {
        return Pattern
                .compile("<p style=\"[^\"]*\"><a href=\"/\\d+/\\d+/\\d+/\\d+/#more-\\d+\" class=\"more-link\">читать дальше</a></p>|<img[^>]*?/>")
                .matcher(description)
                .replaceAll("").trim();
    }

    public News findByTitle(String title) {
        title = title.toLowerCase().replace(" ", "");
        for (int i = 0; i < size(); i++) {
            News topic = get(i);
            if (topic.getTitle().toString().replace(" ", "").equalsIgnoreCase(title))
                return topic;
        }
        return null;
    }
}
