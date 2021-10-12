package org.softeg.slartus.forpdaplus.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.listfragments.BaseGeneralContainerFragment

class NoteFragment : BaseGeneralContainerFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun closeTab(): Boolean {
        return false
    }

    override fun getFragmentInstance(savedInstanceState: Bundle?): Fragment {
        val noteId = arguments?.getInt(ARG_NOTE_ID)
            ?: throw NotReportException("parameter $ARG_NOTE_ID not found")
        return org.softeg.slartus.forpdaplus.feature_notes.ui.note.NoteFragment.newInstance(noteId)
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    companion object {
        private const val ARG_NOTE_ID = "NoteFragment.NOTE_ID"
        fun newInstance(noteId: Int) = NoteFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_NOTE_ID, noteId)
            }
        }

        fun showNote(noteId: Int) {
            MainActivity.addTab(
                App.getContext().getString(R.string.note),
                ARG_NOTE_ID + noteId,
                newInstance(noteId)
            )
        }
    }
}