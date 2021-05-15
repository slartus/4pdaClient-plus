package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import org.softeg.slartus.forpdaapi.post.PostAttach;
import org.softeg.slartus.forpdaapi.users.Users;
import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.forpdacommon.NameValuePair;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.URIUtils;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by slartus on 25.02.14.
 */
public class TopicApi {

    /**
     * не уведомлять
     */
    public static final String TRACK_TYPE_NONE = "none";
    /**
     * Первый раз
     */
    public static final String TRACK_TYPE_DELAYED = "delayed";
    /**
     * Каждый раз
     */
    public static final String TRACK_TYPE_IMMEDIATE = "immediate";
    /**
     * Каждый день
     */
    public static final String TRACK_TYPE_DAILY = "daily";
    /**
     * Каждую неделю
     */
    public static final String TRACK_TYPE_WEEKLY = "weekly";
    /**
     * Удалить
     */
    private static final String TRACK_TYPE_DELETE = "delete";

    /**
     * закрепить
     */
    public static final String TRACK_TYPE_PIN = "pin";
    /**
     * открепить
     */
    public static final String TRACK_TYPE_UNPIN = "unpin";

    public static String changeFavorite(IHttpClient httpClient, CharSequence topicId, String trackType) throws IOException {
        boolean exists;

        FavTopic favTopic = findTopicInFav(topicId);

        exists = favTopic != null;
        if (favTopic == null && TRACK_TYPE_DELETE.equals(trackType)) {
            return "Тема не найдена в избранном";
        }
        if (favTopic != null && trackType.equals(favTopic.getTrackType())) {
            return "Тема уже в избранном с этим типом подписки";
        }

        List<NameValuePair> qparams = new ArrayList<>();
        if (exists) {
            qparams.add(new BasicNameValuePair("act", "fav"));
            qparams.add(new BasicNameValuePair("selectedtids", favTopic.getTid()));
            qparams.add(new BasicNameValuePair("tact", trackType));
        } else {
            qparams.add(new BasicNameValuePair("act", "fav"));
            qparams.add(new BasicNameValuePair("type", "add"));
            qparams.add(new BasicNameValuePair("t", topicId.toString()));
            qparams.add(new BasicNameValuePair("track_type", trackType));
        }

        String uri = URIUtils.createURI("http", HostHelper.getHost(), "/forum/index.php",
                qparams, "UTF-8");
        httpClient.performGet(uri);
        favTopic = findTopicInFav(topicId);
        if (favTopic != null && trackType.equals(favTopic.getTrackType())) {
            if (exists) {
                if (TRACK_TYPE_NONE.equals(trackType))
                    return "Подписка на тему отменена";
                return "Подписка на тему изменена";
            }
            if (TRACK_TYPE_NONE.equals(trackType))
                return "Тема успешно добавлена в избранное";
            return "Подписка на тему оформлена";
        }

        if (favTopic != null && !trackType.equals(favTopic.getTrackType())) {
            if (exists)
                throw new NotReportException("Что-то пошло не так. Подписка на тему не была изменена!");
            throw new NotReportException("Что-то пошло не так. Подписка на тему не применена!");
        }
        if (favTopic == null && TRACK_TYPE_DELETE.equals(trackType)) {
            return "Тема удалена из избранного";
        }
        throw new NotReportException("Неизвестная ошибка добавления темы в избранное");
    }

    private static FavTopic findTopicInFav( CharSequence topicId) throws IOException{
        if (topicId == null)
            return null;
        ListInfo listInfo = new ListInfo();
        listInfo.setFrom(0);
        int topicsCount = 0;
        while (true) {
            ArrayList<FavTopic> topics = TopicsApi.getFavTopics( listInfo);

            for (FavTopic topic : topics) {
                if (!topicId.equals(topic.getId())) continue;
                return topic;
            }
            topicsCount += topics.size();
            if (listInfo.getOutCount() <= topicsCount)
                break;
            listInfo.setFrom(topicsCount);
        }
        return null;
    }

    public static String deleteFromFavorites(IHttpClient httpClient, String id) throws IOException{
        return changeFavorite(httpClient, id, TRACK_TYPE_DELETE);
    }


    /**
     * Кто читает тему
     */
    public static TopicReadingUsers getReadingUsers(IHttpClient httpClient, String topicId) throws IOException {
        String body = httpClient.performGetFullVersion("https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + topicId).getResponseBody();

        Matcher m = Pattern.compile("<a href=\".*?/forum/index.php\\?showuser=(\\d+)\" title=\"\\(([^)]*)\\)[^\"]*\"><span style='color:(.*?)'>(.*?)</span></a>").matcher(body);
        TopicReadingUsers res = new TopicReadingUsers();
        while (m.find()) {
            OldUser user = new OldUser();
            user.setMid(m.group(1));
            user.setNick(Html.fromHtml(m.group(4)).toString());
            user.setHtmlColor(m.group(3));
            res.add(user);
        }
        m = Pattern.compile("<div class=\"formsubtitle\" style=\"padding: 4px;\"><b>\\d+</b> чел. читают эту тему \\(гостей: (\\d+), скрытых пользователей: (\\d+)\\)</div>")
                .matcher(body);
        if (m.find()) {
            res.setGuestsCount(m.group(1));
            res.setHideCount(m.group(2));
        }
        return res;
    }

    public static Users getWriters(IHttpClient httpClient, String topicId) throws IOException {
        String body = httpClient.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?s=&act=Stats&CODE=who&t=" + topicId).getResponseBody();

        Matcher m = Pattern.compile("<div[^>]*?>[^<]*<span[^>]*?><a[^>]*?showuser=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a><\\/span>[^<]*?(\\d+)<\\/div>", Pattern.CASE_INSENSITIVE).matcher(body);
        Users res = new Users();
        while (m.find()) {
            OldUser user = new OldUser();
            user.setMid(m.group(1));
            user.setNick(m.group(2));
            user.MessagesCount = m.group(3);
            res.add(user);
        }
        m = Pattern.compile("<div class=\"maintitle\" align=\"center\">(.*?)</div>").matcher(body);
        if (m.find())
            res.setTag(m.group(1));
        return res;
    }

    public static ArrayList<PostAttach> getTopicAttachment(IHttpClient httpClient, String topicId) throws IOException {
        String body = httpClient.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?act=attach&code=showtopic&tid=" + topicId).getResponseBody();

        Matcher m = Pattern.compile("<tr id=\"(\\d+)\"><td align=\"center\" class=\"row1\"><img src=\"[^\"]*/([^.]*)\\..*?\" alt=\"Прикрепленный файл\" /></td><td class=\"row2\"><a href=\"([^\"]*)\" target=\"_blank\">([^<]*)</a><div class=\"desc\">\\(([^)]*)\\)</div></td><td align=\"center\" class=\"row1\">([^<]*)</td><td class=\"row2\" align=\"center\"><a href=\"#\" onclick=\"opener.location='[^']*pid=(\\d+)';\">\\d+</a></td></tr>"
                , Pattern.CASE_INSENSITIVE).matcher(body);
        ArrayList<PostAttach> res = new ArrayList<>();
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        while (m.find()) {
            PostAttach item = new PostAttach();
            item.setId(m.group(1));

            item.setFileType(m.group(2));
            item.setUrl("https://"+HostHelper.getHost() + m.group(3));
            item.setName(m.group(4));
            item.setAdditionDate(Functions.parseForumDateTime(m.group(5).replace("Добавлено ", ""), today, yesterday));
            item.setFileSize(FileUtils.parseFileSize(m.group(6)));
            item.setPostId(m.group(7));
            res.add( item);// обратная сортировка
        }

        return res;
    }

    public static String pinFavorite(IHttpClient httpClient, String topicId, String trackType) throws IOException{
        FavTopic favTopic = findTopicInFav(topicId);

        if (favTopic == null) {
            return "Тема не найдена в избранном";
        }

        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "fav"));
        qparams.add(new BasicNameValuePair("selectedtids", favTopic.getTid()));
        qparams.add(new BasicNameValuePair("tact", trackType));
        String uri = URIUtils.createURI("http", HostHelper.getHost(), "/forum/index.php",
                qparams, "UTF-8");
        httpClient.performGet(uri);
        return TRACK_TYPE_PIN.equals(trackType) ? "Тема закреплена" : "Тема откреплена";
    }

    public static String toMobileVersionUrl(String url) {
        if (TextUtils.isEmpty(url))
            return url;

        Matcher m = PatternExtensions.compile(HostHelper.getHost()+"/forum/lofiversion/index.php\\?t(\\d+)(?:-(\\d+))?.html").matcher(url);
        if (m.find()) {
            return "https://"+ HostHelper.getHost() +"/forum/index.php?showtopic=" + m.group(1) + (TextUtils.isEmpty(m.group(2)) ? "" : ("&st=" + m.group(2)));
        }
        return url;
    }


}
