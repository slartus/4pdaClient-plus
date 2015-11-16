package org.softeg.slartus.forpdaplus.classes;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:30
 *
 * Да!, я знаю, за такое нужно ломать руки. Я бы тоже сломал бы увидев такое, наверное.
 * Блин, ну а чего делать!?! Может быть когда нибудь и перепишется, но я бы не стал на это надеется. ))) by iSanechek
 */


public class DevDbDevice {
    private static final String TAG = "devDbDevice";
    private Info m_Info;

    private ArrayList<String> m_ScreenshotUrls;
    private ArrayList<String> m_Controls;
    private ArrayList<String> m_KeyInfo;
    private ArrayList<String> m_ValueInfo;
//    private ArrayList<String> m_Comments;

    public List<String> getScreenshotUrls() {
        return m_ScreenshotUrls;
    }

    public List<String> getControls() {
        return m_Controls;
    }

    public List<String> getKeyInfo() {
        return m_KeyInfo;
    }

    public List<String> getValueInfo() {
        return m_ValueInfo;
    }

//    public List<String> getComments() {
//        return m_Comments;
//    }

    public void parse(String page) {
        parseScreenshots(page);
        parseDetailInfo(page);
        parseControls(page);
        parseModels(page);
    }

    public Info getInfo() {
        return m_Info;
    }

    private void parseScreenshots(String page) {
        m_ScreenshotUrls = new ArrayList<>();

        Document doc = Jsoup.parse(page);
        Elements imgLinks = doc.getElementsByClass("item-gallery");
        Elements elements = imgLinks.select("a[href]");
        for (Element element : elements) {
            String iLinks = element.attributes().get("href");
            if (iLinks.contains("http")) {
                m_ScreenshotUrls.add(iLinks.replace("p.jpg", "n.jpg"));
            }
        }
    }

    //Detail Info
    private void parseDetailInfo(String page) {
        m_Info = new Info(); // <- Нужно для
//        parseRating(page); // <- этого
        parsePrice(page); // <- и этого

        m_KeyInfo = new ArrayList<>();
        m_ValueInfo = new ArrayList<>();

        Document doc = Jsoup.parse(page);
        Element content = doc.getElementsByClass("item-content").first();

        // KeyInfo
        Elements elements = content.select("dd");
        for (int i = 0; i < elements.size(); i++) {
            String e = elements.get(i).text();
            m_KeyInfo.add(e);
        }

        // ValueInfo
        Elements elements1 = content.select("dt");
        for (int i = 0; i < elements1.size(); i++) {
            String e1 = elements1.get(i).text();
            m_ValueInfo.add(e1);
        }
    }

    private void parseModels(String page) {
        Document doc = Jsoup.parse(page);
        String title = doc.getElementsByTag("a").attr("data-lightbox");
        if (title != null) {
            m_Info.Model = title;
        }
    }

//    private void parseRatin(String page) {
//        Pattern modelPattern = Pattern.compile("Рейтинг популярности: (.*?)\"");
//        Matcher m = modelPattern.matcher(page);
//        if (m.find()) {
//            m_Info.Rating = Double.parseDouble(m.group(1));
//        }
//    }

//    private void parseRating(String page) {
//        Document doc = Jsoup.parse(page);
//        Element a = doc.getElementsByClass("item-rating").first();
//        Log.i(TAG, "rating ===>> " + a.text());
//
//    }

    private void parsePrice(String page) {
        Document doc = Jsoup.parse(page);
        Element price = doc.getElementsByClass("price").first();
        if (price != null) {
            m_Info.Price = price.select("strong").text();
        } else {
            m_Info.Price = "цена отсутствует";
        }


//        Pattern pattern = Pattern.compile("<span class='cost_label_center'>(.*?)</span>");
//        Matcher m = pattern.matcher(page);
//        if (m.find()) {
//            m_Info.Price = m.group(1).trim();
//        }
    }

    private void parseControls(String page) {
        m_Controls = new ArrayList<>();
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementsByClass("tab-control").first();
        Elements elements = element.select("li");
        Elements elements1 = elements.select("a[href]");
        for (Element element1 : elements1) {
            String control = element1.text();
            m_Controls.add(control);
        }

    }

//    private void parseComments(String page) {
//        m_Comments = new ArrayList<>();
//        Document doc = Jsoup.parse(page);
//    }

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
