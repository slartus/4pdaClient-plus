package org.softeg.slartus.forpdaplus.core_di.implementations

import org.softeg.slartus.forpdaplus.core_db.note.Note
import org.softeg.slartus.forpdaplus.core_db.note.NoteDao
import javax.inject.Inject
import org.softeg.slartus.forpdaplus.feature_notes.Note as FeatureNote
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao as FeatureNoteDao

class NoteDaoImpl @Inject constructor(
    private val noteDao: NoteDao
) :
    FeatureNoteDao {
    override suspend fun merge(notes: List<org.softeg.slartus.forpdaplus.feature_notes.Note>) =
        noteDao.merge(notes.map { it.map() })

    override suspend fun getAll(): List<FeatureNote> = noteDao.getAll().map { it.map() }
    override suspend fun getByTopicId(topicId: String) =
        noteDao.getByTopicId(topicId).map { it.map() }

    override suspend fun get(id: Int) = noteDao.get(id)?.map()

    override suspend fun delete(id: Int) = noteDao.delete(id)

    override suspend fun insert(note: FeatureNote) = noteDao.insert(note.map())

    override suspend fun update(note: FeatureNote) = noteDao.update(note.map())

    override suspend fun insertAll(vararg notes: FeatureNote) =
        noteDao.insertAll(*notes.map { it.map() }.toTypedArray())

    override suspend fun deleteAll() = noteDao.deleteAll()

    override suspend fun delete(note: FeatureNote) = noteDao.delete(note.map())
}

private fun Note.map(): FeatureNote {
    return FeatureNote(
        id = this.id,
        title = this.title,
        body = this.body,
        url = url,
        topicId = topicId,
        topicTitle = topicTitle,
        postId = postId,
        userId = userId,
        userName = userName,
        date = date
    )
}

private fun FeatureNote.map(): Note {
    return Note(
        id = this.id,
        title = this.title,
        body = this.body,
        url = url,
        topicId = topicId,
        topicTitle = topicTitle,
        postId = postId,
        userId = userId,
        userName = userName,
        date = date
    )
}