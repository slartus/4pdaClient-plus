package org.softeg.slartus.forpdaapi.digest;/*
 * Created by slinkin on 18.03.14.
 */

import android.text.Html;

import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigestApi {
    private static final String APP_DIGEST_ID = "127361";
    private static final String GAME_DIGEST_ID = "381335";

    public static ArrayList<DigestCatalog> getCatalog(IHttpClient client, DigestCatalog parent) throws IOException {
        ArrayList<DigestCatalog> res = new ArrayList<>();

        DigestCatalog appsDigestForum = new DigestCatalog(APP_DIGEST_ID, "Программы");
        appsDigestForum.setParent(parent);
        res.add(appsDigestForum);
        res.addAll(Apps.getCatalog(client, appsDigestForum));

        appsDigestForum = new DigestCatalog(GAME_DIGEST_ID, "Игры").setGames();
        appsDigestForum.setParent(parent);
        res.add(appsDigestForum);
        res.addAll(Games.getCatalog(client, appsDigestForum));

        return res;
    }

    public static ArrayList<Topic> loadTopics(IHttpClient client, DigestCatalog catalog) throws IOException {
        ArrayList<Topic> res;
        if (catalog.getType() == DigestCatalog.TYPE_APPLICATIONS) {
            res = Apps.loadTopics(client, catalog);
        } else {
            res = Games.loadTopics(client, catalog);
        }
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

        public static ArrayList<Topic> loadTopics(IHttpClient client, DigestCatalog catalog) throws IOException {

            if (catalog.getId().equals(catalog.getParent().getId()))
                return Apps.getAppDigestCategoryThemes(client, catalog, (DigestCatalog) catalog.getParent().getParent(), (DigestCatalog) catalog.getParent());
            return Apps.getAppDigestSubCategoryThemes(client, catalog, (DigestCatalog) catalog.getParent().getParent(), (DigestCatalog) catalog.getParent());

        }

        public static ArrayList<DigestCatalog> getCatalog(IHttpClient client, DigestCatalog parent) throws IOException {
            ArrayList<DigestCatalog> res = new ArrayList<>();

            String body = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + APP_DIGEST_ID + "&view=getlastpost").getResponseBody();

            final String[] messages = body.split("<!--Begin Msg Number");

            final int[] msgId = {messages.length};
            final int[] fId = {Integer.MIN_VALUE};
            final Pattern postPattern = Pattern.compile("(\\d\\d? \\w+ \\d{4} - \\d\\d? \\w+ \\d{4})[\\s\\S]*?" +
                    "Новые программы, ранее не публиковавшиеся на форуме:([\\s\\S]*?)" +
                    "Обновление ранее опубликованных на форуме программ:([\\s\\S]*?)$");

            while (true) {
                String msg;
                msgId[0]--;
                if (msgId[0] < 0) break;
                msg = messages[msgId[0]];

                Matcher m = postPattern.matcher(msg);
                if (!m.find()) continue;

                DigestCatalog dateForum = new DigestCatalog(Integer.toString(fId[0]++), m.group(1));
                dateForum.setParent(parent);
                res.add(dateForum);

                res.addAll(addNewOrUpdCatalog("Новые программы, ранее не публиковавшиеся на форуме", "Новые программы", fId, m, dateForum));
                fId[0]++;
                res.addAll(addNewOrUpdCatalog("Обновление ранее опубликованных на форуме программ", "Обновление программ", fId, m, dateForum));
            }

            return res;
        }

        private static ArrayList<DigestCatalog> addNewOrUpdCatalog(String title, String topicsTitle, int[] fId, Matcher m, DigestCatalog dateForum) {
            ArrayList<DigestCatalog> res = new ArrayList<>();
            DigestCatalog newAppsForum = new DigestCatalog(Integer.toString(fId[0]++), title);
            newAppsForum.setParent(dateForum);
            res.add(newAppsForum);

            DigestCatalog topicsForum = new DigestCatalog(newAppsForum.getId().toString(), topicsTitle + "@Темы")
                    .setLevel(DigestCatalog.LEVEL_TOPICS_NEXT);
            topicsForum.setParent(newAppsForum);
            res.add(topicsForum);
            res.addAll(fillAppNewUpdForum(newAppsForum, m.group(2), fId));
            return res;
        }

        private static ArrayList<DigestCatalog> fillAppNewUpdForum(DigestCatalog newUpdForum, String body, int[] fId) {
            final Pattern digestPieceOfPiecePattern = Pattern.compile("<span style=\"color:coral\"><b>(.*?)</b>");

            ArrayList<DigestCatalog> res = new ArrayList<>();
            Matcher m = digestPieceOfPiecePattern.matcher(body);
            while (m.find()) {

                DigestCatalog pieceOfPiece = new DigestCatalog(Integer.toString(fId[0]++), m.group(1))
                        .setLevel(DigestCatalog.LEVEL_TOPICS_NEXT);
                pieceOfPiece.setParent(newUpdForum);

                res.add(pieceOfPiece);
            }
            return res;

        }

        public static ArrayList<Topic> getAppDigestCategoryThemes(IHttpClient client, DigestCatalog category,
                                                                  DigestCatalog grandCategory, DigestCatalog parentCategory) throws IOException {
            ArrayList<Topic> res = new ArrayList<>();
            String body = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + APP_DIGEST_ID + "&view=getlastpost").getResponseBody();
            Matcher mtchr = Pattern.compile(grandCategory.getTitle() + "[\\s\\S]*?"
                    + Pattern.quote(parentCategory.getTitle().toString()) + "([\\s\\S]*?)" +
                    "(?:<!--Begin Msg Number|<!-- TABLE FOOTER -->|Обновление ранее опубликованных на форуме программ)").matcher(body);

            if (!mtchr.find())
                return res;

            Matcher topicmatcher = Pattern.compile("<a[^>]*href=\"[^\"]*showtopic=(\\d+)[^\"]*\"[^>]*>(.*?)</a>\\s*<span[^>]*>(.*?)</span>(.*?)<br").matcher(mtchr.group(1));
            while (topicmatcher.find()) {
                Topic topic = new Topic(topicmatcher.group(1), Html.fromHtml(topicmatcher.group(2)).toString());
                topic.setForumTitle(category.getTitle().toString());
                topic.setLastMessageAuthor(topicmatcher.group(3));

                topic.setDescription(Html.fromHtml(topicmatcher.group(4)).toString());
                res.add(topic);
            }
            return res;
        }

        public static ArrayList<Topic> getAppDigestSubCategoryThemes(IHttpClient client, DigestCatalog category,
                                                                     DigestCatalog grandCategory, DigestCatalog parentCategory) throws IOException {
            String body = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + APP_DIGEST_ID + "&view=getlastpost").getResponseBody();
            Matcher mtchr = Pattern.compile(grandCategory.getTitle() + "[\\s\\S]*?"
                    + Pattern.quote(parentCategory.getTitle().toString()) + "[\\s\\S]*?"
                    + "<span style=.color:coral.><b>\\Q" + category.getTitle() + "\\E</b>([\\s\\S]*?)" +
                    "(?:(?:</div>)|(?:<span style=.color:coral.><b>.*?</b>))").matcher(body);

            ArrayList<Topic> res = new ArrayList<>();
            if (!mtchr.find()) return res;

            Matcher topicmatcher = Pattern.compile("<a[^>]*href=\"[^\"]*showtopic=(\\d+)[^\"]*\"[^>]*>(.*?)</a>\\s*<span[^>]*>(.*?)</span>(.*?)<br").matcher(mtchr.group(1));
            while (topicmatcher.find()) {
                Topic topic = new Topic(topicmatcher.group(1), Html.fromHtml(topicmatcher.group(2)).toString());
                topic.setForumTitle(category.getTitle().toString());
                topic.setLastMessageAuthor(topicmatcher.group(3));

                topic.setDescription(Html.fromHtml(topicmatcher.group(4)).toString());
                res.add(topic);
            }
            return res;


        }


    }

    public static class Games {

        public static ArrayList<Topic> loadTopics(IHttpClient client, DigestCatalog catalog) throws IOException {
            String body = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + GAME_DIGEST_ID + "&view=getlastpost").getResponseBody();
            Matcher mtchr = Pattern.compile(catalog.getParent().getTitle() + "[\\s\\S]*?"
                    + Pattern.quote(catalog.getTitle().toString()) + "([\\s\\S]*?)" +
                    "(?:<!--Begin Msg Number|<!-- TABLE FOOTER -->|Обновление ранее опубликованных на форуме игр)").matcher(body);

            ArrayList<Topic> res = new ArrayList<>();
            if (!mtchr.find())
                return res;


            Matcher m = Pattern.compile("<li>(.*?)<a[^>]*showtopic=(\\d+)[^>]*>(.*?)</a>(.*?)</li>").matcher(mtchr.group(1));


            while (m.find()) {
                Topic topic = new Topic(m.group(2), Html.fromHtml(m.group(3)).toString());

                topic.setLastMessageAuthor(Html.fromHtml(m.group(1)).toString());
                topic.setDescription(Html.fromHtml(m.group(4)).toString());

                res.add(topic);
            }
            return res;
        }

        public static Collection<? extends DigestCatalog> getCatalog(IHttpClient client, DigestCatalog parentCatalog) throws IOException {
            String body = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + GAME_DIGEST_ID + "&view=getlastpost").getResponseBody();
            final String[] messages = body.split("<!--Begin Msg Number");

            final int[] msgId = {messages.length};
            final int[] fId = {Integer.MIN_VALUE / 2};
            final Pattern postPattern = Pattern.compile("(\\d\\d? \\w+ \\d{4} - \\d\\d? \\w+ \\d{4})[\\s\\S]*?" +
                    "Новые игры, ранее не публиковавшиеся на форуме[\\s\\S]*?" +
                    "Обновление ранее опубликованных на форуме игр[\\s\\S]*?$");
            ArrayList<DigestCatalog> res = new ArrayList<>();
            while (true) {
                String msg;
                msgId[0]--;
                if (msgId[0] < 0) break;
                msg = messages[msgId[0]];

                Matcher m = postPattern.matcher(msg);
                if (!m.find()) continue;

                DigestCatalog dateForum = new DigestCatalog(Integer.toString(fId[0]++), m.group(1))
                        .setGames();
                dateForum.setParent(parentCatalog);
                res.add(dateForum);

                DigestCatalog newAppsForum = new DigestCatalog(Integer.toString(fId[0]++), "Новые игры, ранее не публиковавшиеся на форуме")
                        .setGames().setLevel(DigestCatalog.LEVEL_TOPICS_NEXT);
                newAppsForum.setParent(dateForum);
                res.add(newAppsForum);

                DigestCatalog updAppsForum = new DigestCatalog(Integer.toString(fId[0]++), "Обновление ранее опубликованных на форуме игр")
                        .setGames().setLevel(DigestCatalog.LEVEL_TOPICS_NEXT);
                updAppsForum.setParent(dateForum);
                res.add(updAppsForum);
            }
            return res;
        }
    }
}
