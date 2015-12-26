package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;
import org.softeg.slartus.forpdaplus.devdb.model.DiscussionModel;
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;
import org.softeg.slartus.forpdaplus.devdb.model.PricesModel;
import org.softeg.slartus.forpdaplus.devdb.model.ReviewsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isanechek on 23.11.15.
 */
public class ParseHelper {

    public void parseHelper(String page) {
        parseFirmware(page);
        parseComments(page);
        parseReviews(page);
        parseDiscussions(page);
        parsePrices(page);
    }

    private void parseDiscussions(String page) {
        List<DiscussionModel> cache = new ArrayList<>();
        String link1 = null;
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementById("discussions");
        Elements elements = element.getElementsByClass("article-list");
        Elements elements1 = elements.select("li");
        for (int i = 0; i < elements1.size(); i++) {
            String link = elements1.get(i).getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            String title = elements1.get(i).getElementsByClass("title").text();
            String time = elements1.get(i).getElementsByClass("upd").text();
            String description = elements1.get(i).getElementsByClass("description").text();

            DiscussionModel model = new DiscussionModel(description, time, link1, title);
            cache.add(model);
        }
        DevDbUtils.saveDiscussion(App.getContext(), cache);
    }

    private void parseComments(String page) {
        List<CommentsModel> cache = new ArrayList<>();
        ArrayList<String> dr = new ArrayList<>();
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementById("comments");
        Elements elements = element.select("li");
        if (elements != null) {
            for (Element element1 : elements) {
                if (!element1.getElementsByClass("text-box").text().isEmpty()) {
                    /**
                     * Тут короче если текст бокс не нуль, то и все остальное не нуль.
                     */

                    String comment = element1.getElementsByClass("text-box").text();
                    String link = element1.select("div.name a").attr("href");
                    String userName = element1.select("div.name a").attr("title");
                    String date = element1.select("div.date").text();
                    String ratingNum = element1.select("span.num").text();
                    String ratingText = element1.select("span.text").text();

                    // for detail dialog
                    Elements elements1 = element1.getElementsByClass("reviews-list");
                    if (elements1 != null) {
                        for (Element element2 : elements1) {
                            String ratingLine = element2.select("div.line").text();
                            dr.add(ratingLine);
                        }
                    }

                    CommentsModel commentsModel = new CommentsModel(date, ratingNum, ratingText, comment, link, userName, dr);
                    cache.add(commentsModel);
                }
            }
            DevDbUtils.saveComments(App.getContext(), cache);
        }
    }


    private void parsePrices(String page) {
        List<PricesModel> cache = new ArrayList<>();
        String link1 = null;
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementById("prices");
        Elements elements = element.getElementsByClass("article-list");
        Elements elements1 = elements.select("li");
        for (int i = 0; i < elements1.size(); i++) {
            String link = elements1.get(i).getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            String title = elements1.get(i).getElementsByClass("title").text();
            String time = elements1.get(i).getElementsByClass("upd").text();
            String description = elements1.get(i).getElementsByClass("description").text();

            PricesModel model = new PricesModel(time, description, link1, title);
            cache.add(model);
        }
        DevDbUtils.savePrices(App.getContext(), cache);
    }

    private void parseFirmware(String page) {
        List<FirmwareModel> cache = new ArrayList<>();
        String link1 = null;
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementById("firmware");
        Elements elements = element.getElementsByClass("article-list");
        Elements elements1 = elements.select("li");
        for (int i = 0; i < elements1.size(); i++) {
            String link = elements1.get(i).getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            String title = elements1.get(i).getElementsByClass("title").text();
            String time = elements1.get(i).getElementsByClass("upd").text();
            String description = elements1.get(i).getElementsByClass("description").text();

            FirmwareModel model = new FirmwareModel(time, description, link1, title);
            cache.add(model);
        }
        DevDbUtils.saveFirmware(App.getContext(), cache);
    }

    private void parseReviews(String page) {
        List<ReviewsModel> cache = new ArrayList<>();
        String imgLink1 = null;
        Document doc = Jsoup.parse(page);
        Element element = doc.getElementById("reviews");
        Elements elements = element.getElementsByClass("article-list");
        Elements elements1 = elements.select("li");
        for (int i = 0; i < elements1.size(); i++) {

            Element el = elements1.get(i).select("a[href]").first();
            String url = "http://4pda.ru" + el.attr("href");
            String imgLink = elements1.get(i).getElementsByClass("article-img").select("img[src]").attr("src");
            if (imgLink.contains("http")) {
                imgLink1 = imgLink;
            }
            String title = elements1.get(i).getElementsByClass("title").text();
            String date = elements1.get(i).getElementsByClass("upd").text();
            String description = elements1.get(i).getElementsByClass("description").text();

            ReviewsModel model = new ReviewsModel(date, imgLink1, url, description, title);
            cache.add(model);
        }
        DevDbUtils.saveReviews(App.getContext(), cache);
    }
}
