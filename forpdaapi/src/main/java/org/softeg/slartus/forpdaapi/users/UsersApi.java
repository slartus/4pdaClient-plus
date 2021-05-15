package org.softeg.slartus.forpdaapi.users;/*
 * Created by slinkin on 10.04.2014.
 */


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersApi {
    /**
     * Администрация: Админы, суперы,модеры
     *
     */
    public static ArrayList<LeadUser> getLeaders(IHttpClient client) throws IOException {
        String page = client.performGet("https://"+ HostHelper.getHost() +"/forum/index.php?act=Stats&CODE=leaders").getResponseBody();

        Document doc = Jsoup.parse(page);
        ArrayList<LeadUser> res = new ArrayList<>();
        Pattern p = Pattern.compile("showuser=(\\d+)", Pattern.CASE_INSENSITIVE);
        for (Element groupElement : doc.select("div.borderwrap")) {
            String group = groupElement.select("div.maintitle").first().text().trim();

            for (Element trElement : groupElement.select("table.ipbtable").first().select("tr")) {

                Elements tds = trElement.select("td.row1");
                if (tds.size() == 0) continue;

                Element el = tds.get(0).select("a").first();
                Matcher m = p.matcher(el.attr("href"));
                if (m.find()) {
                    LeadUser user = new LeadUser(m.group(1), el.text());
                    user.setGroup(group);

                    Elements forumElements = tds.get(1).select("option");

                    if (forumElements.size() == 0 && "Все форумы".equals(tds.get(1).text())) {
                        user.getForums().add(new Forum("-1", "Все форумы"));
                    } else {
                        for (Element forumEl : forumElements) {
                            if ("-1".equals(forumEl.attr("value"))) continue;
                            user.getForums().add(new Forum(forumEl.attr("value"), forumEl.text()));
                        }
                    }
                    res.add(user);
                }


            }


        }
        return res;
    }
}
