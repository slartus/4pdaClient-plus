package org.softeg.slartus.forpdaapi.parsers

import android.text.TextUtils
import org.softeg.slartus.forpdaapi.vo.MentionsResult
import java.io.Serializable
import java.net.URI
import java.util.regex.Pattern

/**
 * Парсер кол-ва упоминаний
 */
class MentionsParser private constructor() {
    private object Holder {
        val INSTANCE = MentionsParser()
    }

    private val countPattern = Pattern.compile("\\Wact=mentions[^\"]*\"[^\"]*\\sdata-count=\"(\\d+)\"", Pattern.CASE_INSENSITIVE)
    private val mentionsPattern = Pattern
            .compile("<div[^>]*\\sclass=\"[^\"]*topic_title_post[^\"]*\"[^>]*>.*?<a[^>]*href=\"([^\"]*)\"[^>]*>([\\s\\S]*?)<\\/a><\\/div>[\\s\\S]*?<span[^>]*\\sclass=\"[^\"]*post_date[^\"]*\"[^>]*><a[^>]*>([^\\|&]*)<\\/a>[\\s\\S]*?<font color=\"([^\"]*?)\"[\\s\\S]*?showuser=(\\d+)\"[^>]*?>([\\s\\S]*?)(?:<i[\\s\\S]*?\\/i>)?<\\/a>[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div[^>]*\\sclass=\"[^\"]*topic_title_post[^\"]*\"[^>]*>|<div><div[^>]*\\sclass=\"[^\"]*pagination[^\"]*\"[^>]*>)", Pattern.CASE_INSENSITIVE)

    companion object {

        const val TAG = "MentionsParser"
        val instance by lazy { Holder.INSTANCE }
    }

    fun parseCount(page: String): Int? {
        val matcher = countPattern.matcher(page)
        if (matcher.find())
            return matcher.group(1)?.toString()?.toIntOrNull()
        return null
    }

    fun parseMentions(page: String): MentionsResult {
        val mentionsResult = parsePages(page)

        mentionsResult.mentions = parseBody(page)
        return mentionsResult
    }

    private fun parseBody(body: String): List<MentionItem> {
        val matcher = mentionsPattern.matcher(body)
        val items = ArrayList<MentionItem>()
        while (matcher.find()) {
            val topicUrl = matcher.group(1)
            val topicTitle = matcher.group(2)
            val dateTime = matcher.group(3)
            val userState = if (matcher.group(4) == "red") "" else "online"
            val userId = matcher.group(5)
            val userName = matcher.group(6)
            val postBody = matcher.group(7)
            items.add(MentionItem(topicUrl, topicTitle, dateTime, userState, userId, userName, postBody))
        }


        return items
    }

    private fun parsePages(page: String): MentionsResult {
        val pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);")
        // http://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        //final Pattern paginationPattern = Pattern.compile("<div class=\"pagination\">([\\s\\S]*?)<\\/div><\\/div><br");

        val lastPageStartPattern = Pattern.compile("act=mentions[^\"]*st=(\\d+)")
        val currentPagePattern = Pattern.compile("<span[^>]*\\sclass=\"[^\"]*pagecurrent[^\"]*\"[^>]*>(\\d+)</span>")

        val searchResult = MentionsResult()

        var m = pagesCountPattern.matcher(page)
        if (m.find()) {
            searchResult.setPagesCount(m.group(1))
        }

        m = lastPageStartPattern.matcher(page)
        while (m.find()) {
            searchResult.setLastPageStartCount(m.group(1))
        }

        m = currentPagePattern.matcher(page)
        if (m.find()) {
            searchResult.setCurrentPage(m.group(1))
        } else
            searchResult.setCurrentPage("1")
        return searchResult
    }
}

class MentionItem(
        val postUrl: String,
        val topicTitle: String,
        val dateTime: String,
        val userState: String,
        val userId: String,
        val userName: String,
        val body: String) : Serializable
