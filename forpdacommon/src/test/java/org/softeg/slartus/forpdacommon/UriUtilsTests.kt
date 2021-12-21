package org.softeg.slartus.forpdacommon

import org.junit.Assert.assertEquals
import org.junit.Test

class UriUtilsTests {
    @Test
    fun escapeHTMLTest() {
        assertEquals(URIUtils.escapeHTML("▸Scream◂"), "&#9656;Scream&#9666;")
        assertEquals(URIUtils.escapeHTML("Поиск"), "Поиск")
        assertEquals(
            URIUtils.escapeHTML("Ё!\"№;%:?*()_+ХЪ/ЖЭ,ЮБ&◂йцуqwe01"),
            "Ё!\"№;%:?*()_+ХЪ/ЖЭ,ЮБ&&#9666;йцуqwe01"
        )

    }

//    @Test
//    fun createUrlTests() {
//        val qualms: MutableList<NameValuePair> = ArrayList()
//        qualms.add(BasicNameValuePair("act", "search"))
//        qualms.add(BasicNameValuePair("query", "Поиск"))
//        qualms.add(BasicNameValuePair("username", "▸Scream◂"))
//        qualms.add(BasicNameValuePair("source", "all"))
//        qualms.add(BasicNameValuePair("sort", "rel"))
//        qualms.add(BasicNameValuePair("result", "posts"))
//
//        val uri = createURI(
//            "https", host, "/forum/index.php",
//            qualms, "windows-1251"
//        )
//
//        assertEquals(
//            uri,
//            "https://$host/forum/index.php?act=search&query=%CF%EE%E8%F1%EA&username=%26%239656%3BScream%26%239666%3B&source=all&sort=rel&result=posts"
//        )
//    }
}