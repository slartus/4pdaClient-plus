package org.softeg.slartus.forpdaplus.core_api.newslist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        val count = 62
        val page =
            javaClass.classLoader?.getResource("news_list/all.html")?.readText() ?: ""
        val newsList = NewsListConverter.parseBody(page)
        assertEquals(newsList.size, count)

        newsList.forEach {
            assertNotNull(it.id)
            assertNotNull(it.url)
            assertNotNull(it.title)
            assertNotNull(it.description)
            assertNotNull(it.authorId)
            assertNotNull(it.author)
            assertNotNull(it.date)
            assertNotNull(it.imgUrl)
            assertNotNull(it.commentsCount)
        }
    }
}