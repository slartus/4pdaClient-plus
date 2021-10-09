package org.softeg.slartus.forpdaplus.feature_notes

interface NotesDao {
   suspend fun merge(notes: List<Note>)
   suspend fun getAll(): List<Note>
   suspend fun insert(note: Note)
   suspend fun update(note: Note)
   suspend fun insertAll(vararg notes: Note)
   suspend fun deleteAll()
   suspend fun delete(note: Note)
}