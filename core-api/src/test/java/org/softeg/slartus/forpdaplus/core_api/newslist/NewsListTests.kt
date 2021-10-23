package org.softeg.slartus.forpdaplus.core_api.newslist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.softeg.slartus.forpdaplus.core_api.converters.NewsListConverter

class NewsListTests {
    @Test
    fun parseDateTests() {
        assertNotNull(NewsListConverter.parseDate("2021-10-22T09:20:00+00:00"))
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