package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.util.Log;

import com.google.gson.Gson;

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
import org.softeg.slartus.forpdaplus.devdb.model.SpecModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by isanechek on 23.11.15.
 */
public class ParseHelper {
    private ParsedModel parsed = new ParsedModel();

    public ParsedModel parseHelper(String page) {
        Element main = Jsoup.parse(page).select(".device-frame").first();
        parseTitle(main);
        parseSpec(main);
        parseFirmware(main);
        parseComments(main);
        parseReviews(main);
        parseDiscussions(main);
        parsePrices(main);
        return parsed;
    }

    private void parseTitle(Element main){
        parsed.setTitle(main.select(".product-name").first().text());
    }

    private void parseSpec(Element main){
        SpecModel specModel = new SpecModel();
        Element spec = main.select("#specification").first();

        for(Element element:spec.select(".item-visual .item-gallery a")){
            specModel.getGalleryLinks().add(element.attr("href"));
            specModel.getGalleryImages().add(element.select("img").first().attr("src"));
        }
        Elements temp = spec.select(".item-main .price-box .price strong");
        if(!temp.text().isEmpty())
            specModel.setPrice(temp.first().text());
        specModel.setSpecTable(spec.select(".item-content .content .specifications-list"));
        parsed.setSpecModel(specModel);
    }
    private void parseDiscussions(Element main) {
        String link, title, time, description;
        List<DiscussionModel> cache = new ArrayList<>();
        DiscussionModel model;
        String link1 = null;
        Elements elements = main.select("#discussions .article-list li");
        for (Element element : elements) {
            link = element.select(".title a").attr("href");
            /*if (link.contains("http")) {
                link1 = link;
            }*/
            title = element.select(".title").text();
            time = element.select(".upd").text();
            description = element.select(".description").text();

            model = new DiscussionModel(description, time, link, title);
            cache.add(model);
        }
        //DevDbUtils.saveDiscussion(App.getContext(), cache);
        parsed.setDiscussionModels(new Gson().toJson(cache));
    }

    private void parseComments(Element main) {
        String comment, link, userName, date, ratingNum, ratingText;
        List<CommentsModel> cache = new ArrayList<>();
        ArrayList<String> dr = new ArrayList<>();
        CommentsModel commentsModel;
        Elements elements = main.select("#comments li");
        if (elements != null) {
            for (Element element1 : elements) {
                if (!element1.select(".text-box").text().isEmpty()) {
                    /**
                     * Тут короче если текст бокс не нуль, то и все остальное не нуль.
                     */

                    comment = element1.select(".text-box").text();
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
            //DevDbUtils.saveComments(App.getContext(), cache);
            parsed.setCommentsModels(new Gson().toJson(cache));
        }
    }


    private void parsePrices(Element main) {
        String link, title, time, description;
        List<PricesModel> cache = new ArrayList<>();
        PricesModel model;
        String link1 = null;
        Elements elements = main.select("#prices .article-list li");
        for(Element element:elements){
            link = element.select(".title a").attr("href");
            /*if (link.contains("http")) {
                link1 = link;
            }*/
            title = element.select(".title").text();
            time = element.select(".upd").text();
            description = element.select(".description").text();

            model = new PricesModel(time, description, link, title);
            cache.add(model);
        }
        //DevDbUtils.savePrices(App.getContext(), cache);
        parsed.setPricesModels(new Gson().toJson(cache));
    }

    private void parseFirmware(Element main) {
        String link, title, time, description;
        List<FirmwareModel> cache = new ArrayList<>();
        FirmwareModel model;
        String link1 = null;
        Elements elements = main.select("#firmware .article-list li");

        for(Element element:elements){
            link = element.select(".title a").attr("href");
            /*if (link.contains("http")) {
                link1 = link;
            }*/
            title = element.select(".title").text();
            time = element.select(".upd").text();
            description = element.select(".description").text();

            model = new FirmwareModel(time, description, link, title);
            cache.add(model);
        }
        //DevDbUtils.saveFirmware(App.getContext(), cache);
        parsed.setFirmwareModels(new Gson().toJson(cache));
    }

    private void parseReviews(Element main) {
        String url, imgLink, title, date, description;
        List<ReviewsModel> cache = new ArrayList<>();
        ReviewsModel model;
        String imgLink1 = null;
        Elements elements = main.select("#reviews .article-list li");

        for(Element element:elements){
            url = "http://4pda.ru" + element.select("a").first().attr("href");
            imgLink = element.select(".article-img img").attr("src");
            /*if (imgLink.contains("http")) {
                imgLink1 = imgLink;
            }*/
            title = element.select(".title").text();
            date = element.select(".upd").text();
            description = element.select(".description").text();

            model = new ReviewsModel(date, imgLink, url, description, title);
            cache.add(model);
        }
        //DevDbUtils.saveReviews(App.getContext(), cache);
        parsed.setReviewsModels(new Gson().toJson(cache));
    }
}
