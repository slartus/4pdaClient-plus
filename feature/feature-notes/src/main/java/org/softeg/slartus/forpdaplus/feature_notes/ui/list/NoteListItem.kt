package org.softeg.slartus.forpdaplus.feature_notes.ui.list

import org.softeg.slartus.forpdaplus.feature_notes.Note

data class NoteListItem(val note: Note, val inProgress: Boolean = false)