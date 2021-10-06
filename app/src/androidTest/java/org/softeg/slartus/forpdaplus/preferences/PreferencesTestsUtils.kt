package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.preference.R
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert
import org.softeg.slartus.forpdaplus.feature_preferences.preferences

// https://android.github.io/android-test/downloads/espresso-cheat-sheet-2.1.0.pdf
fun appearanceClick(@StringRes title: Int) {
    onView(withId(R.id.recycler_view))
        .perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(title)), click()
            )
        )
}

fun checkBoxPreferenceTest(context: Context, @StringRes title: Int, key: String, default: Boolean) {
    val initValue = context.preferences.getBoolean(key, default)

    appearanceClick(title)
    Assert.assertEquals(context.preferences.getBoolean(key, default), !initValue)

    appearanceClick(title)
    Assert.assertEquals(context.preferences.getBoolean(key, default), initValue)
}

fun listPreferenceTest(
    context: Context,
    @StringRes title: Int,
    key: String,
    @ArrayRes entriesResId: Int,
    @ArrayRes entryValuesResId: Int,
    default: String?
) {
    val initValue = context.preferences.getString(key, default)

    val entries = context.resources.getStringArray(entriesResId)
    val entryValues = context.resources.getStringArray(entryValuesResId)

    val initTitle = if (initValue == null) null else entries[entryValues.indexOf(initValue)]

    entries.forEachIndexed { index, s ->
        onView(withText(title)).perform(click())
        onView(withText(s)).perform(click())
        Assert.assertEquals(context.preferences.getString(key, default), entryValues[index])
    }

    if (initValue != null) {
        onView(withText(title)).perform(click())
        onView(withText(initTitle)).perform(click())
        Assert.assertEquals(context.preferences.getString(key, default), initValue)
    } else {
        context.preferences.edit().putString(key, null).apply()
    }
}

fun setProgress(progress: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(SeekBar::class.java)
        }

        override fun getDescription(): String {
            return "Set a progress on a SeekBar"
        }

        override fun perform(uiController: UiController?, view: View) {
            val seekBar = view as SeekBar
            seekBar.progress = progress
        }
    }
}

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