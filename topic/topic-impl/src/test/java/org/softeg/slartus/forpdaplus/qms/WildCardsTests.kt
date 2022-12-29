package org.softeg.slartus.forpdaplus.qms

import org.junit.Test

import org.junit.Assert.*
import org.softeg.slartus.forpdaplus.topic.impl.screens.attachments.TopicAttachmentsViewModel.Companion.containsWildCards

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class WildCardsTests {
    @Test
    fun addition_isCorrect() {
        assertTrue("(".containsWildCards("(", false))
        assertTrue("asd.png".containsWildCards(".png", false))
        assertTrue("asd.png".containsWildCards("a*.png", false))
        assertTrue("asd.png".containsWildCards("as?.png", false))
    }
}