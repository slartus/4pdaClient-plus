package org.softeg.slartus.forpdaplus.devdb.helpers;

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
        Document document = Jsoup.parse(page);
        parseTitle(document);
        parseFirmware(document);
        parseComments(document);
        parseReviews(document);
        parseDiscussions(document);
        parsePrices(document);
    }

    private void parseTitle(Document document){
        DevDbUtils.saveTitle(App.getContext(), document.select(".product-name").first().text());
    }

    private void parseDiscussions(Document document) {
        String link, title, time, description;
        List<DiscussionModel> cache = new ArrayList<>();
        String link1 = null;
        Elements elements = document.getElementById("discussions").getElementsByClass("article-list").select("li");
        for (Element element : elements) {
            link = element.getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            title = element.getElementsByClass("title").text();
            time = element.getElementsByClass("upd").text();
            description = element.getElementsByClass("description").text();

            DiscussionModel model = new DiscussionModel(description, time, link1, title);
            cache.add(model);
        }
        DevDbUtils.saveDiscussion(App.getContext(), cache);
    }

    private void parseComments(Document document) {
        String comment, link, userName, date, ratingNum, ratingText;
        List<CommentsModel> cache = new ArrayList<>();
        ArrayList<String> dr = new ArrayList<>();
        CommentsModel commentsModel;
        Elements elements = document.getElementById("comments").select("li");
        if (elements != null) {
            for (Element element1 : elements) {
                if (!element1.getElementsByClass("text-box").text().isEmpty()) {
                    /**
                     * Тут короче если текст бокс не нуль, то и все остальное не нуль.
                     */

                    comment = element1.getElementsByClass("text-box").text();
                    link = element1.select("div.name a").attr("href");
                    userName = element1.select("div.name a").attr("title");
                    date = element1.select("div.date").text();
                    ratingNum = element1.select("span.num").text();
                    ratingText = element1.select("span.text").text();

                    // for detail dialog
                    Elements elements1 = element1.getElementsByClass("reviews-list");
                    if (elements1 != null)
                        for (Element element2 : elements1)
                            dr.add(element2.select("div.line").text());

                    commentsModel = new CommentsModel(date, ratingNum, ratingText, comment, link, userName, dr);
                    cache.add(commentsModel);
                }
            }
            DevDbUtils.saveComments(App.getContext(), cache);
        }
    }


    private void parsePrices(Document document) {
        String link, title, time, description;
        List<PricesModel> cache = new ArrayList<>();
        PricesModel model;
        String link1 = null;
        Elements elements = document.getElementById("prices").getElementsByClass("article-list").select("li");
        for(Element element:elements){
            link = element.getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            title = element.getElementsByClass("title").text();
            time = element.getElementsByClass("upd").text();
            description = element.getElementsByClass("description").text();

            model = new PricesModel(time, description, link1, title);
            cache.add(model);
        }
        DevDbUtils.savePrices(App.getContext(), cache);
    }

    private void parseFirmware(Document document) {
        String link, title, time, description;
        List<FirmwareModel> cache = new ArrayList<>();
        FirmwareModel model;
        String link1 = null;
        Elements elements = document.getElementById("firmware").getElementsByClass("article-list").select("li");

        for(Element element:elements){
            link = element.getElementsByClass("title").select("a[href]").attr("href");
            if (link.contains("http")) {
                link1 = link;
            }
            title = element.getElementsByClass("title").text();
            time = element.getElementsByClass("upd").text();
            description = element.getElementsByClass("description").text();

            model = new FirmwareModel(time, description, link1, title);
            cache.add(model);
        }
        DevDbUtils.saveFirmware(App.getContext(), cache);
    }

    private void parseReviews(Document document) {
        String url, imgLink, title, date, description;
        List<ReviewsModel> cache = new ArrayList<>();
        ReviewsModel model;
        String imgLink1 = null;
        Elements elements = document.getElementById("reviews").getElementsByClass("article-list").select("li");

        for(Element element:elements){
            url = "http://4pda.ru" + element.select("a[href]").first().attr("href");
            imgLink = element.getElementsByClass("article-img").select("img[src]").attr("src");
            if (imgLink.contains("http")) {
                imgLink1 = imgLink;
            }
            title = element.getElementsByClass("title").text();
            date = element.getElementsByClass("upd").text();
            description = element.getElementsByClass("description").text();

            model = new ReviewsModel(date, imgLink1, url, description, title);
            cache.add(model);
        }
        DevDbUtils.saveReviews(App.getContext(), cache);
    }
}
