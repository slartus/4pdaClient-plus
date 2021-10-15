package org.softeg.slartus.forpdaplus.preferences

import android.content.res.Resources
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

class RecyclerViewMatcher(private val recyclerViewId: Int) {

    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, -1)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null

            override fun describeTo(description: Description) {
                var idDescription = recyclerViewId.toString()
                if (this.resources != null) {
                    idDescription = try {
                        this.resources!!.getResourceName(recyclerViewId)
                    } catch (var4: Resources.NotFoundException) {
                        "$recyclerViewId (resource name not found)"
                    }
                }

                description.appendText("RecyclerView with id: $idDescription at position: $position")
            }

            public override fun matchesSafely(view: View): Boolean {

                this.resources = view.resources

                if (childView == null) {
                    val recyclerView = view.rootView.findViewById<RecyclerView>(recyclerViewId)
                    if (recyclerView?.id == recyclerViewId) {

                        val viewHolder =  recyclerView.findViewHolderForAdapterPosition(position)
                        childView = viewHolder?.itemView
                    } else {
                        return false
                    }
                }

                return if (targetViewId == -1) {
                    view === childView
                } else {
                    val targetView = childView?.findViewById<View>(targetViewId)
                    view === targetView
                }
            }
        }
    }
}