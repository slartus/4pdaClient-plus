package org.softeg.slartus.forpdaplus.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:30
 */
public class DevDbDevice {
    private ArrayList<String> m_ScreenshotUrls;
    private Info m_Info;

    public List<String> getScreenshotUrls() {
        return m_ScreenshotUrls;
    }

    public void parse(String page) {
        parseScreenshots(page);
        parseInfo(page);
    }

    public Info getInfo() {
        return m_Info;
    }


    private void parseScreenshots(String page) {
        m_ScreenshotUrls = new ArrayList<String>();
        Pattern pattern = Pattern.compile("a href=\"http://devdb.ru/data/img/img.*?\\.jpg\" title=.*?target=\"_blank\" rel=\"lytebox\\[\\d+]\"><img src=\"http://devdb.ru/data/img/img(.*?)\\.jpg\"");
        Matcher m = pattern.matcher(page);
        while (m.find()) {
            m_ScreenshotUrls.add("http://devdb.ru/data/img/img" + m.group(1) + ".jpg");
        }
    }

    private void parseInfo(String page) {
        m_Info = new Info();

        parseModel(page);

        parseRating(page);

        parsePrice(page);
        Pattern pattern = Pattern.compile("(><td class=\"ct_group\" colspan=2>(.*?)</td></tr>)?<tr( class=\"ct_\\d+\"><td width=\"35%\">(.*?)</td><td class=b width=\"65%\">(.*?)</td></tr>)?");
        Matcher m = pattern.matcher(page);

        InfoGroup infoGroup = null;
        while (m.find()) {
            if (m.group(2) != null) {
                infoGroup = new InfoGroup(m.group(2));
                m_Info.add(infoGroup);
            }
            if (m.group(4) == null) continue;
            if(infoGroup==null){
                infoGroup=new InfoGroup("");
                m_Info.add(infoGroup);
            }
            infoGroup.add(new InfoItem(m.group(4), m.group(5)));
        }
    }

    private void parseModel(String page) {
        Pattern modelPattern = Pattern.compile("<h1[^>]*?itemprop=\"name\">(.*?)</h1>");
        Matcher m = modelPattern.matcher(page);
        if (m.find()) {
            m_Info.Model = m.group(1);
        }
    }

    private void parseRating(String page) {
        Pattern modelPattern = Pattern.compile("Рейтинг популярности: (.*?)\"");
        Matcher m = modelPattern.matcher(page);
        if (m.find()) {
            m_Info.Rating = Double.parseDouble(m.group(1));
        }
    }

    private void parsePrice(String page) {
        Pattern pattern = Pattern.compile("<span class='cost_label_center'>(.*?)</span>");
        Matcher m = pattern.matcher(page);
        if (m.find()) {
            m_Info.Price = m.group(1).trim();
        }
    }

    public class Info extends ArrayList<InfoGroup> {
        public String Model = "";
        public double Rating = -1;
        public String Price = "";
    }

    public class InfoGroup extends ArrayList<InfoItem> {
        public String Title = "";

        public InfoGroup(String title) {
            Title = title;
        }

    }

    public class InfoItem {
        public InfoItem(String title, String value) {
            Title = title;
            Value = value;

        }

        public String Title = "";
        public String Value = "";
    }
}
