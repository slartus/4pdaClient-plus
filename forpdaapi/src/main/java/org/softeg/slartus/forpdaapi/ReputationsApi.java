package org.softeg.slartus.forpdaapi;/*
 * Created by slinkin on 23.04.2014.
 */

import android.text.Html;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReputationsApi {
    /**
     * Загружает историю репутации пользователя
     *
     * @param httpClient
     * @param self       - действия пользователя с репутацией других пользователей
     * @throws java.io.IOException
     */
    public static ArrayList<ReputationEvent> loadReputation(IHttpClient httpClient, String userId, Boolean self, ListInfo listInfo) throws IOException, URISyntaxException {


        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "rep"));
        qparams.add(new BasicNameValuePair("type", "history"));
        qparams.add(new BasicNameValuePair("mid", userId));
        qparams.add(new BasicNameValuePair("st", Integer.toString(listInfo.getFrom())));
        if (self)// свои действия
            qparams.add(new BasicNameValuePair("mode", "from"));


        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);

        String body = httpClient.performGet(uri.toString());

        Pattern pattern;
        Matcher m = Pattern.compile("<div class=(?:'|\")maintitle(?:'|\")>(.*)</div>", Pattern.CASE_INSENSITIVE).matcher(body);

        if (m.find()) {
            Matcher userMatcher=Pattern.compile("История репутации участника (.*?) \\[(\\+\\d+/-\\d+)\\]",
                    Pattern.CASE_INSENSITIVE).matcher(m.group(1));
            if(userMatcher.find()){
                listInfo.setTitle(Html.fromHtml(userMatcher.group(1)).toString());
                listInfo.getParams().put("USER_NICK",Html.fromHtml(userMatcher.group(1)).toString());
                listInfo.getParams().put("USER_REP",userMatcher.group(2));
            }
            else
                listInfo.setTitle(Html.fromHtml(m.group(1)).toString().trim().replace("История репутации участника ",""));
        }

        if (listInfo.getOutCount() == 0) {
            pattern = Pattern.compile("parseInt\\((\\d+)/\\d+\\)");
            m = pattern.matcher(body);
            if (m.find())
                listInfo.setOutCount(Integer.parseInt(m.group(1)));
        }

        pattern = Pattern.compile(new StringBuilder()
                        .append("<td class='row2' align='left'><b><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a></b></td>")
                        .append("\\s*<td class='row2' align='left'>(?:<b>)?(?:<a href='([^']*)'>)?(.*?)(?:</a>)?(?:</b>)?</td>")
                        .append("\\s*<td class='row2' align='left'>(.*?)</td>")
                        .append("\\s*<td class='row1' align='center'><img border='0' src='style_images/1/([^']*?).gif' /></td>")
                        .append("\\s*<td class='row1' align='center'>(.*?)</td>").toString(),
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
        );
        m = pattern.matcher(body);

        ArrayList<ReputationEvent> res = new ArrayList<>();
        while (m.find()) {
            ReputationEvent rep = new ReputationEvent();
            rep.setUserId(m.group(1));
            rep.setUser(Html.fromHtml(m.group(2)).toString());
            rep.setSourceUrl(m.group(3));
            rep.setSource(Html.fromHtml(m.group(4)).toString());
            rep.setDescription(Html.fromHtml(m.group(5)).toString());
            rep.setState("up".equals(m.group(6)) ? IListItem.STATE_GREEN : IListItem.STATE_RED);
            rep.setDate(m.group(7));
            res.add(rep);
        }
        return res;

    }

    /**
     * Изменение репутации пользователя
     *
     * @param httpClient
     * @param postId     Идентификатор поста, за который поднимаем репутацию. 0 - "в профиле"
     * @param userId
     * @param type       "add" - поднять, "minus" - опустить
     * @param message
     * @return Текст ошибки или пустая строка в случае успеха
     * @throws IOException
     */
    public static Boolean changeReputation(IHttpClient httpClient, String postId, String userId, String type, String message,
                                           Map<String, String> outParams) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "rep");
        additionalHeaders.put("p", postId);
        additionalHeaders.put("mid", userId);
        additionalHeaders.put("type", type);
        additionalHeaders.put("message", message);

        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(res);
        if (m.find()) {
            if (m.group(1) != null && m.group(1).contains("Ошибка")) {
                p = Pattern.compile("<div class='maintitle'>(.*?)</div>");
                m = p.matcher(res);
                if (m.find()) {
                    outParams.put("Result", "Ошибка изменения репутации: " + m.group(1));
                } else {
                    outParams.put("Result", "Ошибка изменения репутации: " + Html.fromHtml(res));
                }

                return false;
            }
            outParams.put("Result", "Репутация: " + m.group(1));
            return true;
        }
        outParams.put("Result", "Репутация изменена");
        return true;

    }
}
