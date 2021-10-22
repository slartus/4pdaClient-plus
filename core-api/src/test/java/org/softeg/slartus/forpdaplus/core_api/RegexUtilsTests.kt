package org.softeg.slartus.forpdaplus.core_api

import org.junit.Assert.assertEquals
import org.junit.Test
import org.softeg.slartus.forpdaplus.core_api.utils.elementRegex
import org.softeg.slartus.forpdaplus.core_api.utils.regex
import timber.log.Timber
import java.util.regex.Pattern

class RegexUtilsTests {
    @Test
    fun buildHtmlElementRegexTest() {
        var regex = elementRegex("article", mapOf("class" to "post[^\"]*"))
        var testText =
            "<article class=\"post HAMBz0g\" itemscope=\"\" itemtype=\"http://schema.org/Article\" itemid=\"392036\">"
        assertEquals(Pattern.compile(regex).matcher(testText).find(), true)

        regex = elementRegex("article", mapOf("class" to "post[^\"]*", "itemscope" to null))
        testText =
            "<article class=\"post HAMBz0g\" itemscope=\"\" itemtype=\"http://schema.org/Article\" itemid=\"392036\">"
        assertEquals(Pattern.compile(regex).matcher(testText).find(), true)

        regex = elementRegex("article", mapOf(
            "class" to "post[^\"]*",
            "itemtype" to "[^\"]*Article",
            "itemid" to "(\\d+)"
        ))
        testText =
            "<article class=\"post HAMBz0g\" itemscope=\"\" itemtype=\"http://schema.org/Article\" itemid=\"392036\">"
        assertEquals(Pattern.compile(regex).matcher(testText).find(), true)

        regex = elementRegex("article", mapOf("class" to "post[^\"]*", "itemtype" to "[^\"]*Article"))
        testText =
            "<article class=\"post HAMBz0g\" itemscope=\"\" itemid=\"392036\">"
        assertEquals(Pattern.compile(regex).matcher(testText).find(), false)
    }


    @Test
    fun dslTest() {
        val html = regex {
            htmlElement("a") {
                tag("href", "([^\\\"]+)")
                tag("title", "([^\\\"]+)")
            }
            multilinePattern()
            htmlElement("img") {
                tag("itemprop", "image")
                tag("src", "([^\"]+)")
            }
            multilinePattern()
            htmlElement("a") {
                tag("href", "[^\"]+showuser=(\\d+)[^\"]*")

            }
            multilinePattern()
            htmlElement("div") {
                tag("itemprop", "description")

            }
        }
    }

}