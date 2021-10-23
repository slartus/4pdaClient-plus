package org.softeg.slartus.forpdaplus.core_api.newslist

import org.junit.Assert.*
import org.junit.Test
import org.softeg.slartus.forpdaplus.core_api.converters.NewsListConverter
import java.util.*
import java.util.concurrent.TimeUnit

class NewsListTests {
    @Test
    fun parseDateTests() {
        val offset = TimeUnit.HOURS.convert(
            (Calendar.getInstance() as GregorianCalendar).timeZone.rawOffset.toLong(),
            TimeUnit.MILLISECONDS
        ).toInt()

        var date = NewsListConverter.parseDate("2021-10-23T07:33:13+05:00")
        assertNotNull(date)
        var calendar = Calendar.getInstance().apply {
            time = date!!
        }

        assertEquals(calendar.get(Calendar.YEAR), 2021)
        assertEquals(calendar.get(Calendar.MONTH), 9)
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 23)
        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 7 + (offset - 5))
        assertEquals(calendar.get(Calendar.MINUTE), 33)
        assertEquals(calendar.get(Calendar.SECOND), 13)

        date = NewsListConverter.parseDate("2021-10-23T14:33:13+05:00")
        assertNotNull(date)
        calendar = Calendar.getInstance().apply {
            time = date!!
        }

        assertEquals(calendar.get(Calendar.YEAR), 2021)
        assertEquals(calendar.get(Calendar.MONTH), 9)
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 23)
        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 14 + (offset - 5))
        assertEquals(calendar.get(Calendar.MINUTE), 33)
        assertEquals(calendar.get(Calendar.SECOND), 13)

        date = NewsListConverter.parseDate("2021-10-23T14:33:13+00:00")
        assertNotNull(date)
        calendar = Calendar.getInstance().apply {
            time = date!!
        }

        assertEquals(calendar.get(Calendar.YEAR), 2021)
        assertEquals(calendar.get(Calendar.MONTH), 9)
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 23)
        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 14 + (offset - 0))
        assertEquals(calendar.get(Calendar.MINUTE), 33)
        assertEquals(calendar.get(Calendar.SECOND), 13)
    }

    @Test
    fun allNewsPageTest() {
        testPage("news_list/all.html", 62)
        testPage("news_list/tech_news.html", 62)
        testPage("news_list/reviews.html", 63)
        testPage("news_list/games.html", 62)
    }

    private fun testPage(resourcePath: String, newsCount: Int) {
        val page =
            javaClass.classLoader?.getResource(resourcePath)?.readText() ?: ""
        val newsList = NewsListConverter.parseBody(page)
        assertEquals(newsList.size, newsCount)

        newsList.forEach {
            assertNotNullOrEmpty(it.id)
            assertNotNullOrEmpty(it.url)
            assertNotNullOrEmpty(it.title)
            assertNotNullOrEmpty(it.description)
            assertNotNullOrEmpty(it.authorId)
            assertNotNullOrEmpty(it.author)
            assertNotNull(it.date)
            assertNotNullOrEmpty(it.imgUrl)
            assertNotNull(it.commentsCount)
        }
    }

    private fun assertNotNullOrEmpty(text: String?) {
        assertNotNull(text)
        assertNotEquals(text, "")
    }
}