package org.softeg.slartus.forpdaplus.listfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.feature_notes.ui.list.NotesListFragment

class NotesListFragment : BaseBrickContainerFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeArrow()
    }

    override fun getFragmentInstance(savedInstanceState: Bundle?): Fragment {
        val topicId = args?.getString(TOPIC_ID_KEY, null)
        return NotesListFragment.newInstance(topicId)
    }

    override fun onResume() {
        super.onResume()
        removeArrow()
    }

    companion object {
        const val TOPIC_ID_KEY = "TOPIC_ID_KEY"
    }
}