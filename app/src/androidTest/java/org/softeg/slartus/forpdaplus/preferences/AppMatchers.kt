package org.softeg.slartus.forpdaplus.preferences

import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withProgress(expectedProgress: Int): Matcher<View?> {
    return object : BoundedMatcher<View?, SeekBar?>(SeekBar::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("expected: ")
            description.appendText("" + expectedProgress)
        }

        override fun matchesSafely(seekBar: SeekBar?): Boolean {
            return seekBar?.progress == expectedProgress
        }
    }
}

fun hasValueEqualTo(content: String): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun describeTo(description: Description) {
            description.appendText("Has EditText/TextView the value:  $content")
        }

        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextView && view !is EditText) {
                return false
            }
            val text: String = if (view is TextView) {
                view.text.toString()
            } else {
                (view as EditText).text.toString()
            }
            return text.equals(content, ignoreCase = true)
        }
    }
}