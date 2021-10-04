package org.softeg.slartus.forpdaplus.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.softeg.slartus.forpdaplus.feature_preferences.preferences
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity
import android.app.Activity
import android.app.Instrumentation

import androidx.test.espresso.intent.Intents.intending

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import org.hamcrest.CoreMatchers.*
import java.io.File

import androidx.test.espresso.intent.Intents

import org.junit.After
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences

@RunWith(AndroidJUnit4::class)
@LargeTest
class PreferencesScreenTests {
    @get:Rule
    var activityRule: ActivityScenarioRule<PreferencesActivity> =
        ActivityScenarioRule(PreferencesActivity::class.java)

    private val context: Context by lazy {
        getInstrumentation().targetContext
    }

    @Test
    fun overallTest() {
        appearanceClick(R.string.appearance)

        chooseThemeTests()

        pencilColorTest()

        accentColorTests()

        webViewFontTest()

        checkBoxPreferenceTest(
            context,
            R.string.toolbar_text_scrolling,
            "titleMarquee",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.square_avatars,
            "isSquareAvarars",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.statusbar_fake_title,
            "statusbarFake",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.old_indicator_title,
            "oldIndicator",
            false
        )

        checkBoxPreferenceTest(
            context,
            R.string.colored_nav_bar_title,
            "coloredNavBar",
            true
        )

        checkBoxPreferenceTest(
            context,
            R.string.pencil_in_actionbar,
            "pancilInActionBar",
            false
        )

        appearanceClick(R.string.user_background_title)
        onView(withText(R.string.reset)).perform(click())
        pressBack()
    }

    @Test
    fun sidePanelTest() {
        appearanceClick(R.string.appearance)

        userBackgroundTest()
    }

    @Test
    fun pencilColorTests() {
        appearanceClick(R.string.appearance)
        pencilColorTest()
    }

    private fun pencilColorTest() {
        val initAccentColor = Preferences.Common.Overall.accentColor
        val initAccentColorEdited = Preferences.Common.Overall.accentColorEdited
        val initAccentColorPressed = Preferences.Common.Overall.accentColorPressed

        appearanceClick(R.string.pick_color_with_pencil)

        val editors = listOf(
            Pair(R.id.red, R.id.redText),
            Pair(R.id.green, R.id.greenText),
            Pair(R.id.blue, R.id.blueText)
        )

        editors.forEach { (_, editText) ->
            onView(withId(editText)).perform(replaceText("-1"))
            onView(withId(editText)).check(matches(hasValueEqualTo("0")))

            onView(withId(editText)).perform(replaceText("256"))
            onView(withId(editText)).check(matches(hasValueEqualTo("0")))

            onView(withId(editText)).perform(replaceText("asd"))
            onView(withId(editText)).check(matches(hasValueEqualTo("0")))
        }

        val values = 0..255
        values.forEach { v ->
            editors.forEach { (seekBar, editText) ->
                onView(withId(editText))
                    .perform(replaceText(v.toString()))
                onView(withId(seekBar)).check(matches(withProgress(v)))
            }
        }
        values.forEach { v ->
            editors.forEach { (seekBar, editText) ->
                onView(withId(seekBar)).perform(setProgress(v))
                onView(withId(editText)).check(matches(hasValueEqualTo(v.toString())))
            }
        }

        val red = values.random()
        val green = values.random()
        val blue = values.random()
        onView(withId(R.id.red)).perform(setProgress(red))
        onView(withId(R.id.greenText)).perform(replaceText(green.toString()))
        onView(withId(R.id.blueText)).perform(replaceText(blue.toString()))


        onView(withText(R.string.accept)).perform(click())
        assertEquals(Preferences.Common.Overall.accentColor, Color.rgb(red, green, blue))

        appearanceClick(R.string.pick_color_with_pencil)
        onView(withText(R.string.reset)).perform(click())
        assertEquals(
            Preferences.Common.Overall.accentColor,
            Preferences.Common.Overall.DEFAULT_ACCENT_COLOR
        )

        Preferences.Common.Overall.accentColor = initAccentColor
        Preferences.Common.Overall.accentColorEdited = initAccentColorEdited
        Preferences.Common.Overall.accentColorPressed = initAccentColorPressed

        assertEquals(Preferences.Common.Overall.accentColor, initAccentColor)
        assertEquals(Preferences.Common.Overall.accentColorEdited, initAccentColorEdited)
        assertEquals(Preferences.Common.Overall.accentColorPressed, initAccentColorPressed)
    }

    private fun webViewFontTest() {
        val initFont = Preferences.Common.Overall.webViewFont
        val initFontName = Preferences.Common.Overall.webViewFontName
        appearanceClick(R.string.webview_font)
        onView(withText(R.string.font_from_style)).perform(click())
        onView(withText(R.string.accept)).perform(click())
        assertEquals(Preferences.Common.Overall.webViewFont, 0)
        assertEquals(Preferences.Common.Overall.webViewFontName, "")

        appearanceClick(R.string.webview_font)
        onView(withText(R.string.system_font)).perform(click())
        onView(withText(R.string.accept)).perform(click())
        assertEquals(Preferences.Common.Overall.webViewFont, 1)
        assertEquals(Preferences.Common.Overall.webViewFontName, "inherit")

        appearanceClick(R.string.webview_font)
        onView(withText(R.string.enter_font_name)).perform(click())
        onView(withHint(R.string.font_name))
            .perform(clearText())
            .perform(typeTextIntoFocusedView("some_test_text"))
        onView(withText(R.string.ok)).perform(click())
        onView(withText(R.string.accept)).perform(click())
        assertEquals(Preferences.Common.Overall.webViewFont, 2)
        assertEquals(Preferences.Common.Overall.webViewFontName, "some_test_text")

        Preferences.Common.Overall.webViewFont = initFont
        Preferences.Common.Overall.webViewFontName = initFontName

        assertEquals(Preferences.Common.Overall.webViewFont, initFont)
        assertEquals(Preferences.Common.Overall.webViewFontName, initFontName)
    }

    private fun chooseThemeTests() {
        val names = context.resources
            .getStringArray(R.array.appthemesArray)

        val values = context.resources
            .getStringArray(R.array.appthemesValues)

        fun chooseThemeTest(themeName: String) {
            appearanceClick(R.string.theme)
            onView(withText(themeName)).perform(click())
            onView(withText(R.string.AcceptStyle)).perform(click())
            assertEquals(
                context.preferences.getString("appstyle", null),
                values[names.indexOf(themeName)].toString()
            )
        }
        names
            .forEach {
                chooseThemeTest(it)
            }
        chooseThemeTest(names[0])
    }

    private fun accentColorTests() {
        val names = listOf(
            context.getString(R.string.pink),
            context.getString(R.string.blue),
            context.getString(R.string.gray)
        )
        val values = listOf("pink", "blue", "gray")
        fun accentColorTest(colorName: String) {
            appearanceClick(R.string.pick_accent_color)

            onView(withText(colorName)).perform(click())
            onView(withText(R.string.accept)).perform(click())
            assertEquals(
                context.preferences.getString("mainAccentColor", null),
                values[names.indexOf(colorName)]
            )
        }
        names
            .forEach {
                accentColorTest(it)
            }
        accentColorTest(names[0])
    }

    @Before
    fun stubCameraIntent() {
        // https://github.com/android/testing-samples/blob/main/ui/espresso/IntentsAdvancedSample/app/src/androidTest/java/com/example/android/testing/espresso/intents/AdvancedSample/ImageViewerActivityTest.java
        Intents.init()
        val result = createImageCaptureActivityResultStub()

        intending(allOf(hasAction(Intent.ACTION_GET_CONTENT))).respondWith(result)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun userBackgroundTest() {
        appearanceClick(R.string.user_background_title)
        onView(withText(R.string.choose)).perform(click())

    }

    private fun createImageCaptureActivityResultStub(): Instrumentation.ActivityResult {
        val resultData = Intent()
        resultData.data =
            Uri.fromFile(File("/storage/emulated/0/save/current_avatar-1001673458.png"))

        // Create the ActivityResult with the Intent.
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }
}
