package org.softeg.slartus.forpdaplus.core_api.newslist

import org.junit.Assert.assertEquals
import org.junit.Test
import org.softeg.slartus.forpdaplus.core_api.converters.NewsListConverter

class NewsListTests {
    @Test
    fun allNewsPageTest() {
        val count = 62
        val page =
            javaClass.classLoader?.getResource("news_list/all.html")?.readText() ?: ""
        val newsList = NewsListConverter.parseBody(page)
        assertEquals(newsList.size, count)
    }
}