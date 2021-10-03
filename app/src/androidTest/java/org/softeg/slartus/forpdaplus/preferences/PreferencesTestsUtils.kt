package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.preference.R
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert
import org.softeg.slartus.forpdaplus.feature_preferences.preferences

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
