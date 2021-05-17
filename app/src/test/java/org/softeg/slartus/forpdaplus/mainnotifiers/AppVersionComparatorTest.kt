package org.softeg.slartus.forpdaplus.mainnotifiers

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionComparatorTest {

    @Test
    fun compare() {
        val tests = ArrayList<Triple<String?, String?, Int>>()
        tests.add(Triple(null, null, 0))
        tests.add(Triple(null, "", 0))
        tests.add(Triple("1", "1", 0))
        tests.add(Triple("1.2.3.4", "1.2.3.4", 0))
        tests.add(Triple("1...2...3...4...", "1.2.3.4", 0))

        tests.add(Triple("1.2", "1", 1))
        tests.add(Triple("1.2", "1.2.1", -1))
        tests.add(Triple("1.3", "1.2.1", 1))

        tests.add(Triple("1.3beta", "1.3", -1))
        tests.add(Triple("1.3beta", "1.3release", -1))
        tests.add(Triple("1.3", "1.3release", 0))

        val comparator = AppVersionComparator()
        tests.forEach {
            assertEquals(AppVersionComparator.compare(it.first, it.second), it.third)
            assertEquals(AppVersionComparator.compare(it.second, it.first), -1 * it.third)

            assertEquals(comparator.compare(
                    AppVersion().apply { ver = it.first },
                    AppVersion().apply { ver = it.second }), it.third)
            assertEquals(comparator.compare(
                    AppVersion().apply { ver = it.second },
                    AppVersion().apply { ver = it.first }), -1 * it.third)

        }

        assertEquals(comparator.compare(
                AppVersion().apply {
                    name = null
                    ver = null
                },
                AppVersion().apply {
                    name = null
                    ver = null
                }), 0)

        assertEquals(comparator.compare(
                AppVersion().apply {
                    name = null
                    ver = null
                },
                AppVersion().apply {
                    name = ""
                    ver = ""
                }), 0)

        assertEquals(comparator.compare(
                AppVersion().apply {
                    ver = "1.2"
                    name = ""
                },
                AppVersion().apply {
                    ver = "1.2"
                    name = "release"
                }), 0)

        assertEquals(comparator.compare(
                AppVersion().apply {
                    ver = "1.2"
                    name = "release"
                },
                AppVersion().apply {
                    ver = "1.2"
                    name = null
                }), 0)
        assertEquals(comparator.compare(
                AppVersion().apply {
                    ver = "1.2"
                    name = "release"
                },
                AppVersion().apply {
                    ver = "1.2"
                    name = "beta"
                }), 1)

        assertEquals(comparator.compare(
                AppVersion().apply {
                    ver = "1.2"
                    name = ""
                },
                AppVersion().apply {
                    ver = "1.2"
                    name = "beta"
                }), 1)
    }
}