package org.softeg.slartus.forpdaplus.feature_notes

import kotlinx.coroutines.flow.Flow

interface NotesDao {
    fun getAllFlow(): Flow<List<Note>>
    suspend fun merge(notes: List<Note>)
    suspend fun getByTopicId(topicId: String): List<Note>
    suspend fun get(id: Int): Note?
    suspend fun delete(id: Int)
    suspend fun insert(note: Note)
    suspend fun update(note: Note)
    suspend fun insertAll(vararg notes: Note)
    suspend fun deleteAll()
    suspend fun delete(note: Note)
}