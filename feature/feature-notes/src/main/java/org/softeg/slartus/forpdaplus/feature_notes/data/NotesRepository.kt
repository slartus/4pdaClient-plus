package org.softeg.slartus.forpdaplus.feature_notes.data

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.feature_notes.Note

interface NotesRepository {
    suspend fun load()
    suspend fun getNote(id: Int): Note?
    suspend fun createNote(
        title: String?, body: String?, url: String?, topicId: String?, topic: String?,
        postId: String?, userId: String?, user: String?
    )

    suspend fun delete(id: String)
    suspend fun checkUrl(baseUrl: String)

    val notes: Flow<List<Note>>
}