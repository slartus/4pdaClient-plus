package org.softeg.slartus.forpdaapi.appsgamescatalog;/*
 * Created by slinkin on 17.03.14.
 */

import android.net.Uri;
import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppsGamesCatalogApi {
    private static final String APPS_PAGE_ID = "112220";
    private static final String GAMES_PAGE_ID = "117270";
    private static final String APPS_CATALOG_URL = "https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + APPS_PAGE_ID;
    private static final String GAMES_CATALOG_URL = "https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + GAMES_PAGE_ID;

    public static ArrayList<AppGameCatalog> getCatalog(IHttpClient client, AppGameCatalog parent) throws IOException {
        ArrayList<AppGameCatalog> res = new ArrayList<>();
        AppGameCatalog appCatalog = new AppGameCatalog(APPS_PAGE_ID, "Программы")
                .setLevel(AppGameCatalog.LEVEL_TYPE);
        appCatalog.setParent(parent);
        Apps.loadCatalog(client, appCatalog, res);
        res.add(appCatalog);

        AppGameCatalog gameCatalog = new AppGameCatalog(GAMES_PAGE_ID, "Игры")
                .setLevel(AppGameCatalog.LEVEL_TYPE);
        Games.loadCatalog(client, gameCatalog, res);
        gameCatalog.setParent(parent);
        res.add(gameCatalog);

        return res;
    }

    public static ArrayList<Topic> loadTopics(IHttpClient client, AppGameCatalog catalog) throws IOException {
        ArrayList<Topic> res = catalog.getType() == AppGameCatalog.TYPE_APPLICATIONS ?
                Apps.loadTopics(client, catalog) : Games.loadTopics(client, catalog);
        sort(res);
        return res;
    }

    private static void sort(ArrayList<Topic> apps) {
        Collections.sort(apps, new Comparator<Topic>() {
            public int compare(Topic topic, Topic topic1) {

                return topic.getTitle().toUpperCase().compareTo(topic1.getTitle().toUpperCase());
            }
        });
    }

    public static class Apps {

        private static void loadCatalog(IHttpClient client, AppGameCatalog catalog, ArrayList<AppGameCatalog> res) throws IOException {
            String pageBody = client.performGet(APPS_CATALOG_URL).getResponseBody();

            Matcher contentMatcher = Pattern.compile("<div class=\"[^\"]*post_body[^\"]*\"[^>]*?>([\\s\\S]*?)<a name=\"entry\\d+\"></a>",
                    Pattern.CASE_INSENSITIVE).matcher(pageBody);
            if (!contentMatcher.find()) {
                throw new IOException("Не найден пост с содержанием каталога приложений");

            }

            Document doc = Jsoup.parse(contentMatcher.group(1));


            for (Element categoryElement : doc.select("ol[type=1]>li")) {
                Elements elements = categoryElement.select("a");
                if (elements.size() == 0) continue;

                Element element = elements.get(0);
                Uri uri = Uri.parse(element.attr("href"));
                String title = element.text();
                AppGameCatalog category = new AppGameCatalog(uri.getQueryParameter("p"), title)
                        .setLevel(AppGameCatalog.LEVEL_CATEGORY);
                category.setParent(catalog);
                res.add(category);

                AppGameCatalog subCategory = new AppGameCatalog(category.getId().toString(), category.getTitle() + " @ темы")
                        .setLevel(AppGameCatalog.LEVEL_CATEGORY);
                subCategory.setParent(category);
                res.add(subCategory);
                for (Element subCategoryElement : categoryElement.select("ul>li>a")) {

                    uri = Uri.parse(subCategoryElement.attr("href"));
                    title = subCategoryElement.text();
                    AppGameCatalog subcategory = new AppGameCatalog(uri.getQueryParameter("anchor"), title)
                            .setLevel(AppGameCatalog.LEVEL_SUBCATEGORY);
                    subcategory.setParent(category);
                    res.add(subcategory);
                }
            }
        }

        public static ArrayList<Topic> loadTopics(IHttpClient client, AppGameCatalog catalog) throws IOException {
            if (catalog.getLevel() == AppGameCatalog.LEVEL_CATEGORY || (catalog.getParent().getId().equals(catalog.getId()))) {
                return loadCategoryThemes(client, catalog.getId().toString());
            } else if (catalog.getLevel() == AppGameCatalog.LEVEL_SUBCATEGORY) {
                return loadSubCategoryThemes(client, catalog.getParent().getId(), catalog.getId());
            }
            return new ArrayList<>();
        }

        public static ArrayList<Topic> loadCategoryThemes(IHttpClient client, String catalogId) throws IOException {
            String pageBody = client.performGet(APPS_CATALOG_URL).getResponseBody();
            ArrayList<Topic> res = new ArrayList<>();

            Pattern pattern = Pattern.compile("<a name=\"entry" + catalogId + "\">([\\s\\S]*?)</div>(?:<!--Begin Msg Number|<!-- TABLE FOOTER)", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(pageBody);
            if (!m.find()) return res;

            Document doc = Jsoup.parse(m.group(1));
            Elements subCategoryElements = doc.select("ol[type=1]");
            for (Element subCategoryElement : subCategoryElements) {
                String subCategoryTitle = "";
                Elements elements = subCategoryElement.select("span");
                if (elements.size() > 0) {
                    subCategoryTitle = elements.first().text();
                }
                Elements topicElements = subCategoryElement.select("li");
                for (Element topicElement : topicElements) {
                    elements = topicElement.select("a");
                    if (elements.size() == 0) continue;

                    Element element = elements.get(0);
                    Uri uri = Uri.parse(element.attr("href"));

                    Topic topic = new Topic(uri.getQueryParameter("showtopic"), element.text());
                    m = Pattern.compile("</a>(?:\\s*</b>\\s*-\\s*)(.*)?(?:<br\\s*/>|$)", Pattern.CASE_INSENSITIVE).matcher(topicElement.html());
                    if (m.find())
                        topic.setDescription(m.group(1));
                    topic.setForumTitle(subCategoryTitle);
                    res.add(topic);
                }
            }


            return res;
        }

        private static ArrayList<Topic> loadSubCategoryThemes(IHttpClient client, CharSequence categoryId, CharSequence subCategoryId) throws IOException {
            String pageBody = client.performGet(APPS_CATALOG_URL).getResponseBody();
            ArrayList<Topic> res = new ArrayList<>();

            Pattern pattern = Pattern.compile("<a name=\"entry" + categoryId + "\">([\\s\\S]*?)</div>(?:<!--Begin Msg Number|<!-- TABLE FOOTER)", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(pageBody);
            if (!m.find()) return res;

            Document doc = Jsoup.parse(m.group(1));
            Elements subCategoryElements = doc.select("ol[type=1]");
            for (Element subCategoryElement : subCategoryElements) {
                Elements elements = subCategoryElement.select(String.format("a[name=%s]", subCategoryId));
                if (elements.size() == 0) continue;
                String subCategoryTitle = "";
                elements = subCategoryElement.select("span");
                if (elements.size() > 0) {
                    subCategoryTitle = elements.first().text();
                }
                Elements topicElements = subCategoryElement.select("li");
                for (Element topicElement : topicElements) {
                    elements = topicElement.select("a");
                    if (elements.size() == 0) continue;

                    Element element = elements.get(0);
                    Uri uri = Uri.parse(element.attr("href"));

                    Topic topic = new Topic(uri.getQueryParameter("showtopic"), element.text());
                    m = Pattern.compile("</a>(?:\\s*</b>\\s*-\\s*)(.*)?(?:<br\\s*/>|$)", Pattern.CASE_INSENSITIVE).matcher(topicElement.html());
                    if (m.find())
                        topic.setDescription(m.group(1));
                    topic.setForumTitle(subCategoryTitle);
                    res.add(topic);
                }
                break;
            }
            return res;
        }
    }

    public static class Games {

        private static void loadCatalog(IHttpClient client, AppGameCatalog catalog, ArrayList<AppGameCatalog> res) throws IOException {

            String pageBody = client.performGet(GAMES_CATALOG_URL).getResponseBody();

            Pattern pattern = Pattern.compile("<div class=\"[^\"]*post_body[^\"]*\">(.*?)(?:<!--Begin Msg Number|<!-- TABLE FOOTER)",
                    Pattern.CASE_INSENSITIVE);

            Matcher m = pattern.matcher(pageBody);
            if (!m.find()) return;

            Document doc = Jsoup.parse(m.group(1));


            for (Element categoryElement : doc.select("ol[type=1]>li")) {
                Elements elements = categoryElement.select("a");
                if (elements.size() == 0) continue;

                Element element = elements.get(0);
                Uri uri = Uri.parse(element.attr("href"));
                String title = element.text();
                AppGameCatalog category = new AppGameCatalog(uri.getQueryParameter("p"), title)
                        .setLevel(AppGameCatalog.LEVEL_CATEGORY).setGames();
                category.setParent(catalog);
                res.add(category);
            }
        }

        public static ArrayList<Topic> loadTopics(IHttpClient client, AppGameCatalog catalog) throws IOException {
            if (catalog.getLevel() == AppGameCatalog.LEVEL_CATEGORY || (catalog.getParent().getId().equals(catalog.getId()))) {
                return loadCategoryThemes(client, catalog.getId().toString());
            } else if (catalog.getLevel() == AppGameCatalog.LEVEL_SUBCATEGORY) {
                return loadSubCategoryThemes(client, catalog.getParent().getId().toString(), catalog);
            }
            return new ArrayList<>();
        }

        public static ArrayList<Topic> loadCategoryThemes(IHttpClient client, String catalogId) throws IOException {
            String pageBody = client.performGet(GAMES_CATALOG_URL).getResponseBody();
            ArrayList<Topic> res = new ArrayList<>();

            Pattern pattern = Pattern.compile("<a name=\"entry" + catalogId + "\">([\\s\\S]*?)</div>(?:<!--Begin Msg Number|<!-- TABLE FOOTER)", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(pageBody);

            if (!m.find())
                return res;
            Document doc = Jsoup.parse(m.group(1));
            Elements subCategoryElements = doc.select("ol[type=1]>li");
            for (Element topicElement : subCategoryElements) {
                Elements elements = topicElement.select("a");
                if (elements.size() == 0) continue;

                Element element = elements.get(0);
                Uri uri = Uri.parse(element.attr("href"));

                Topic topic = new Topic(uri.getQueryParameter("showtopic"), element.text());
                m = Pattern.compile("</a>(?:\\s*</b>\\s*-\\s*)(.*)?(?:<br\\s*/>|$)", Pattern.CASE_INSENSITIVE).matcher(topicElement.html());
                if (m.find())
                    topic.setDescription(m.group(1));
                res.add(topic);
            }

            return res;
        }

        private static ArrayList<Topic> loadSubCategoryThemes(IHttpClient client, String catalogId, AppGameCatalog subCategory) throws IOException {
            String pageBody = client.performGet(GAMES_CATALOG_URL).getResponseBody();
            ArrayList<Topic> res = new ArrayList<>();

            Pattern pattern = Pattern.compile("<div class=\"post_body(?:\\s|\\w)*?\">(<div align=.center.>)?(?:<!--coloro:coral-->)?<span style=.color:coral.>(?:<!--/coloro-->)?<b>(<div align=.center.>)?" + catalogId + "\\..*?</div>(.*?)</div>");
            Matcher m = pattern.matcher(pageBody);
            Pattern subCategoryPattern = Pattern.compile("(?<!<div align='center'>)<span style=\"color:coral\"><b>" + Pattern.quote(subCategory.getHtmlTitle()) + "<(.*?)(?:<ol type=.1.>|</div>)");
            Pattern themesPattern = Pattern.compile("<li><b><a href=\"https?://"+ HostHelper.getHost() +"/forum/index.php\\?showtopic=(\\d+)[^\"]*\" target=\"_blank\">(.*?)</a>(.*?)</li>");

            if (m.find()) {
                Matcher m1 = subCategoryPattern.matcher(m.group(3) + "</div>");
                if (m1.find()) {
                    Matcher m2 = themesPattern.matcher(m1.group(1));
                    while (m2.find()) {

                        Topic topic = new Topic(m2.group(1), m2.group(2));
                        topic.setDescription(Html.fromHtml(m2.group(3)).toString());
                        topic.setForumTitle(subCategory.getTitle().toString());
                        res.add(topic);
                    }
                }
            }

            return res;
        }
    }


}
