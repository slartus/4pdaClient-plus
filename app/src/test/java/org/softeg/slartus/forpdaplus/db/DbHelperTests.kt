package org.softeg.slartus.forpdaplus.db

import org.junit.Test

class DbHelperTests {

    @Test
    fun parseCorrect() {
        assert(DbHelper.parseDateOrNull("2021.06.06 03:16:37") != null)
        assert(DbHelper.parseDateOrNull("2020.09.18 23:28:01") != null)
    }
}