package org.softeg.slartus.forpdaplus

import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException

fun setProgress(progress: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(SeekBar::class.java)
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


fun nothing(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(SeekBar::class.java)
        }

        override fun getDescription(): String {
            return "Do nothing with view"
        }

        override fun perform(uiController: UiController?, view: View) {
            Log.d("nothingActions","nothing do with $view")
        }
    }
}


@Suppress("unused")
fun waitForView(viewId: Int, timeout: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view with id $viewId; during $timeout millis."
        }

        override fun perform(uiController: UiController, rootView: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val viewMatcher = ViewMatchers.withId(viewId)

            do {
                // Iterate through all views on the screen and see if the view we are looking for is there already
                for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                    // found view with required ID
                    if (viewMatcher.matches(child)) {
                        return
                    }
                }
                // Loops the main thread for a specified period of time.
                // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -&gt; test fails
            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(rootView))
                .build()
        }
    }
}

class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        ViewMatchers.assertThat(adapter!!.itemCount, CoreMatchers.`is`(expectedCount))
    }
}