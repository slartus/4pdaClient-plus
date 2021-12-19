package org.softeg.slartus.forpdacommon

import org.junit.Assert.assertEquals
import org.junit.Test

class UriUtilsTests {
    @Test
    fun escapeHTMLTest(){
        assertEquals(URIUtils.escapeHTML("▸Scream◂"),"&#9656;Scream&#9666;")
    }
}