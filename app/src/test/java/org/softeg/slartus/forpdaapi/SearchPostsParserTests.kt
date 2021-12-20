package org.softeg.slartus.forpdaapi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.softeg.slartus.forpdaplus.fragments.search.SearchPostsParser
import ru.slartus.http.AppResponse
import java.nio.charset.Charset

class SearchPostsParserTests {
    @Test
    fun parseOneResultPage() {
        val page =
            javaClass.classLoader?.getResource("searchapi/posts_single_page.html")
                ?.readText(Charset.forName("windows-1251")) ?: ""
        val appResponse = AppResponse(
            redirectUrl = "http://4pda.to/forum/index.php?act=search&query=&username=%26%239656%3BScream%26%239666%3B&forums[]=all&topics[]=271502&subforums=1&source=pst&sort=dd&result=posts&noform=1&st=0",
            requestUrl = "https://4pda.to/forum/index.php?act=search&query=&username=%26%239656%3BScream%26%239666%3B&forums[]=all&topics[]=271502&subforums=1&source=pst&sort=dd&result=posts&noform=1&st=0",
            responseBody = page
        )

        val posts = SearchPostsParser.parsePosts(appResponse.responseBody)
        assertEquals(posts.size, 1)
        val post = posts.first()
        assertEquals(post.userId, "5445702")
        assertEquals(post.userName, "&#9656;Scream&#9666;")
        assertTrue(!post.titleHtml.isNullOrBlank())
        assertTrue(post.titleHtml!!.endsWith("4pda.ru</a>"))
        assertTrue(post.titleHtml!!.startsWith("<a href="))

        assertTrue(post.dateTimeHtml!!.contains("23:29"))
        assertTrue(!post.postBodyHtml.isNullOrBlank())
        assertTrue(post.postBodyHtml!!.endsWith("01:20</span>"))
        assertTrue(post.postBodyHtml!!.startsWith("<a href="))
        assertTrue(post.userState == "")
    }

    @Test
    fun parseMultiplePagesFirstPage() {
        val page =
            javaClass.classLoader?.getResource("searchapi/posts_multiple_pages_first_page.html")
                ?.readText(Charset.forName("windows-1251")) ?: ""
        val appResponse = AppResponse(
            "http://4pda.to/forum/index.php?act=search&query=&username=slartus&forums[]=all&topics[]=271502&subforums=1&source=pst&sort=dd&result=posts&noform=1&st=0",
            "https://4pda.to/forum/index.php?act=search&query=&username=slartus&forums[]=all&topics[]=271502&subforums=1&source=pst&sort=dd&result=posts&noform=1&st=0",
            page
        )

        val posts = SearchPostsParser.parsePosts(appResponse.responseBody)
        assertEquals(posts.size, 20)
        posts.forEach { post ->
            assertEquals(post.userId, "236113")
            assertEquals(post.userName, "slartus")
            assertTrue(!post.titleHtml.isNullOrBlank())
            assertTrue(!post.dateTimeHtml.isNullOrBlank())
            assertTrue(!post.postBodyHtml.isNullOrBlank())
            assertTrue(post.userState == "online")
        }
    }
}