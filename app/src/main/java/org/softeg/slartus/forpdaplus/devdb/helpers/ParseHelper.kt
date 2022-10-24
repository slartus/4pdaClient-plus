package org.softeg.slartus.forpdaplus.devdb.helpers

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.softeg.slartus.forpdaplus.devdb.model.*

class ParseHelper {
    private val parsed = ParsedModel()
    fun parseHelper(page: String): ParsedModel {
        val main =
            Jsoup.parse(page).select(".device-frame").first() ?: error("device-frame not found")
        parseTitle(main)
        parseSpec(main)
        parseFirmware(main)
        parseComments(main)
        parseReviews(main)
        parseDiscussions(main)
        parsePrices(main)
        return parsed
    }

    private fun parseTitle(main: Element) {
        parsed.title = main.select(".product-name").first()?.text().orEmpty()
    }

    private fun parseSpec(main: Element) {
        val specModel = SpecModel()
        val spec = main.select("#specification").first() ?: return
        for (element in spec.select(".item-visual .item-gallery a")) {
            specModel.galleryLinks.add(element.attr("href"))
            specModel.galleryImages.add(element.select("img").first()!!.attr("src"))
        }
        val temp = spec.select(".item-main .price-box .price strong")
        if (temp.text().isNotEmpty())
            specModel.price = temp.first()?.text().orEmpty()
        specModel.specTable = spec.select(".item-content .content .specifications-list")
        parsed.specModel = specModel
    }

    private fun parseDiscussions(main: Element) {
        var link: String
        var title: String
        var time: String
        var description: String
        val cache: MutableList<DiscussionModel> = ArrayList()

        for (element in main.select("#discussions .article-list li")) {
            link = element.select(".title a").first()?.attr("href").orEmpty()
            title = element.select(".title").first()?.text().orEmpty()
            time = element.select(".upd").first()?.text().orEmpty()
            description = element.select(".description").first()?.text().orEmpty()
            cache.add(DiscussionModel(description, time, link, title))
        }
        parsed.discussionModels = Gson().toJson(cache)
    }

    private fun parseComments(main: Element) {
        var comment: String
        var link: String
        var userName: String
        var date: String
        var ratingNum: String
        var ratingText: String
        val cache: MutableList<CommentsModel> = ArrayList()
        val dr = ArrayList<String>()
        for (element1 in main.select("#comments .reviews li")) {
            if (!element1.select(".text-box").text().isEmpty()) {
                /**
                 * Тут короче если текст бокс не нуль, то и все остальное не нуль.
                 */
                var element = element1.select(".text-box .w-toggle").first()
                if (element == null) element = element1.select(".text-box").first()
                comment = element?.text().orEmpty()
                element = element1.select("div.name a").first()
                link = element?.attr("href").orEmpty()
                userName = element?.attr("title").orEmpty()
                date = element1.select("div.date").first()?.text().orEmpty()
                ratingNum = element1.select("span.num").first()?.text().orEmpty()
                ratingText = element1.select("span.text").first()?.text().orEmpty()
                // for detail dialog
                val elements1 = element1.getElementsByClass("reviews-list")
                for (element2 in elements1)
                    dr.add(element2.select("div.line").text())
                cache.add(CommentsModel(date, ratingNum, ratingText, comment, link, userName, dr))
            }
        }
        parsed.commentsModels = Gson().toJson(cache)
    }

    private fun parsePrices(main: Element) {
        var link: String
        var title: String
        var time: String
        var description: String
        val cache: MutableList<PricesModel> = ArrayList()
        val elements = main.select("#prices .article-list li")
        for (element in elements) {
            link = element.select(".title a").first()?.attr("href").orEmpty()
            title = element.select(".title").first()?.text().orEmpty()
            time = element.select(".upd").first()?.text().orEmpty()
            description = element.select(".description").first()?.text().orEmpty()
            cache.add(PricesModel(time, description, link, title))
        }
        parsed.pricesModels = Gson().toJson(cache)
    }

    private fun parseFirmware(main: Element) {
        var link: String
        var title: String
        var time: String
        var description: String
        val cache: MutableList<FirmwareModel> = ArrayList()
        for (element in main.select("#firmware .article-list li")) {
            link = element.select(".title a").first()?.attr("href").orEmpty()
            title = element.select(".title").first()?.text().orEmpty()
            time = element.select(".upd").first()?.text().orEmpty()
            description = element.select(".description").first()?.text().orEmpty()
            cache.add(FirmwareModel(time, description, link, title))
        }
        parsed.firmwareModels = Gson().toJson(cache)
    }

    private fun parseReviews(main: Element) {
        var url: String
        var imgLink: String
        var title: String
        var date: String
        var description: String
        val cache: MutableList<ReviewsModel> = ArrayList()
        for (element in main.select("#reviews .article-list li")) {
            url = element.select("a").first()?.attr("href").orEmpty()
            imgLink = element.select(".article-img img").first()?.attr("src").orEmpty()
            title = element.select(".title").first()?.text().orEmpty()
            date = element.select(".upd").first()?.text().orEmpty()
            description = element.select(".description").first()?.text().orEmpty()
            cache.add(ReviewsModel(date, imgLink, url, description, title))
        }
        parsed.reviewsModels = Gson().toJson(cache)
    }
}