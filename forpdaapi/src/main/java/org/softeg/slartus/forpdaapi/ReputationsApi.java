package org.softeg.slartus.forpdaapi;/*
 * Created by slinkin on 23.04.2014.
 */


import android.net.Uri;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.classes.ReputationsListData;

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
    public static ReputationsListData loadReputation(IHttpClient httpClient, String userId, Boolean self,
                                                     ListInfo listInfo, String plusImage)
            throws IOException, URISyntaxException {


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

        Document doc = Jsoup.parse(body);
        Element el = doc.select("div.maintitle").first();

        ReputationsListData res = new ReputationsListData();

        if (el != null) {
            Matcher userMatcher = Pattern.compile("\\(.*?\\)\\s*(.*?)\\s*(\\S*)\\s*\\[(\\+\\d+\\/-\\d+)\\]",
                    Pattern.CASE_INSENSITIVE).matcher(el.text());
            if (userMatcher.find()) {
                res.setTitle(userMatcher.group(1));
                res.setUser(userMatcher.group(2));
                res.setRep(userMatcher.group(3));
            }
        }

        el = doc.select("div.pagination").first();
        if (el != null) {
            Element pel = el.select("a[href=#]").first();
            if (pel != null) {
                Matcher m = Pattern.compile("(\\d+)").matcher(pel.text());
                if (m.find())
                    res.setPagesCount(Integer.parseInt(m.group(1)));
            }
            pel = el.select("span.pagecurrent").first();
            if (pel != null) {
                res.setCurrentPage(Integer.parseInt(pel.text()));
            }
        }


        for (Element trElement : doc.select("table.ipbtable").first().select("tr")) {
            Elements tdElements = trElement.select("td");
            if (tdElements.size() != 5) continue;
            ReputationEvent rep = new ReputationEvent();

            Element tdElement = tdElements.get(0);
            Element l = tdElement.select("a").first();
            if (l != null) {
                Uri ur = Uri.parse(l.attr("href"));
                rep.setUserId(ur.getQueryParameter("showuser"));
                rep.setUser(l.text());
            }

            tdElement = tdElements.get(1);
            l = tdElement.select("a").first();
            if (l != null) {
                rep.setSourceUrl(l.attr("href"));
                rep.setSource(l.text());
            } else {
                rep.setSourceUrl(null);
                rep.setSource(tdElement.text());
            }

            tdElement = tdElements.get(2);
            rep.setDescription(tdElement.text());

            tdElement = tdElements.get(3);
            rep.setState(tdElement.html().contains(plusImage) ? IListItem.STATE_GREEN : IListItem.STATE_RED);

            tdElement = tdElements.get(4);
            rep.setDate(tdElement.text());
            res.getItems().add(rep);

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
                Document doc = Jsoup.parse(res);
                Element element = doc.select("div.content").first();

                if (element != null) {
                    outParams.put("Result", element.text());
                } else {
                    outParams.put("Result", doc.text());
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
