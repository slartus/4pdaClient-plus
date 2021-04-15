package org.softeg.slartus.forpdaplus.devdb.helpers;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;
import org.softeg.slartus.forpdaplus.devdb.model.DiscussionModel;
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;
import org.softeg.slartus.forpdaplus.devdb.model.PricesModel;
import org.softeg.slartus.forpdaplus.devdb.model.ReviewsModel;
import org.softeg.slartus.forpdaplus.devdb.model.SpecModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isanechek on 23.11.15.
 */
public class ParseHelper {
    private final ParsedModel parsed = new ParsedModel();

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
        for (Element element : main.select("#discussions .article-list li")) {
            link = element.select(".title a").first().attr("href");
            title = element.select(".title").first().text();
            time = element.select(".upd").first().text();
            description = element.select(".description").first().text();
            cache.add(new DiscussionModel(description, time, link, title));
        }
        parsed.setDiscussionModels(new Gson().toJson(cache));
    }

    private void parseComments(Element main) {
        String comment, link, userName, date, ratingNum, ratingText;
        List<CommentsModel> cache = new ArrayList<>();
        ArrayList<String> dr = new ArrayList<>();
        for (Element element1 : main.select("#comments .reviews li")) {
            if (!element1.select(".text-box").text().isEmpty()) {
                /**
                 * Тут короче если текст бокс не нуль, то и все остальное не нуль.
                 */
                Element element =  element1.select(".text-box .w-toggle").first();
                if(element==null)
                    element = element1.select(".text-box").first();
                comment = element.text();
                element = element1.select("div.name a").first();
                link = element.attr("href");
                userName = element.attr("title");
                date = element1.select("div.date").first().text();
                ratingNum = element1.select("span.num").first().text();
                ratingText = element1.select("span.text").first().text();
                // for detail dialog
                Elements elements1 = element1.getElementsByClass("reviews-list");
                if (elements1 != null)
                    for (Element element2 : elements1)
                        dr.add(element2.select("div.line").text());
                cache.add(new CommentsModel(date, ratingNum, ratingText, comment, link, userName, dr));
            }
        }
        parsed.setCommentsModels(new Gson().toJson(cache));
    }


    private void parsePrices(Element main) {
        String link, title, time, description;
        List<PricesModel> cache = new ArrayList<>();
        Elements elements = main.select("#prices .article-list li");
        for(Element element:elements){
            link = element.select(".title a").first().attr("href");
            title = element.select(".title").first().text();
            time = element.select(".upd").first().text();
            description = element.select(".description").first().text();

            cache.add(new PricesModel(time, description, link, title));
        }
        parsed.setPricesModels(new Gson().toJson(cache));
    }

    private void parseFirmware(Element main) {
        String link, title, time, description;
        List<FirmwareModel> cache = new ArrayList<>();
        for(Element element:main.select("#firmware .article-list li")){
            link = element.select(".title a").first().attr("href");
            title = element.select(".title").first().text();
            time = element.select(".upd").first().text();
            description = element.select(".description").first().text();
            cache.add(new FirmwareModel(time, description, link, title));
        }
        parsed.setFirmwareModels(new Gson().toJson(cache));
    }

    private void parseReviews(Element main) {
        String url, imgLink, title, date, description;
        List<ReviewsModel> cache = new ArrayList<>();
        for(Element element:main.select("#reviews .article-list li")){
            url = element.select("a").first().attr("href");
            imgLink = element.select(".article-img img").first().attr("src");
            title = element.select(".title").first().text();
            date = element.select(".upd").first().text();
            description = element.select(".description").first().text();
            cache.add(new ReviewsModel(date, imgLink, url, description, title));
        }
        parsed.setReviewsModels(new Gson().toJson(cache));
    }
}
