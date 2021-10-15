package org.softeg.slartus.forpdaplus.feature_notes.data

import android.net.Uri
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import org.softeg.slartus.forpdaplus.feature_notes.network.NotesService
import retrofit2.Response
import java.util.*
import javax.inject.Inject

open class NotesRepositoryImpl @Inject constructor(
    private val notesService: NotesService,
    private val notesDao: NotesDao,
    private val notesPreferences: NotesPreferences
) : NotesRepository {
    override val notes = notesDao.getAll()
    protected open val local: Boolean
        get() {
            return notesPreferences.isLocal
        }

    private val apiUrl: String
        get() {
            return notesPreferences.remoteUrl ?: ""
        }

    override suspend fun checkUrl(
        baseUrl: String,
    ) {

        val url = getUrl(baseUrl, "get")
        requestOrError(url)
    }

    private fun getUrl(action: String): String {
        return getUrl(apiUrl, action)
    }

    private fun getUrl(baseUrl: String, action: String): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        uriBuilder.appendQueryParameter("action", action)
        return uriBuilder.build().toString()
    }

    override suspend fun load() {
        if (!local) {
            val url = getUrl("get")
            val notes = requestOrError(url)
            notesDao.merge(notes)
        }
    }

    override suspend fun delete(id: String) {
        if (local) {
            notesDao.delete(id.toInt())
        } else {
            val uriBuilder = Uri.parse(apiUrl).buildUpon()
            uriBuilder.appendQueryParameter("action", "del")
            uriBuilder.appendQueryParameter("id", id)
            val url = uriBuilder.build().toString()
            notesDao.merge(requestOrError(url))
        }
    }

    private suspend fun requestOrError(url: String): List<Note> {
        return notesService.request(url).notesOrError()
    }

    override suspend fun getNote(id: Int): Note? {
        if (!local) {
            load()
        }
        return notesDao.get(id)
    }

    override suspend fun createNote(
        title: String?, body: String?, url: String?, topicId: String?, topic: String?,
        postId: String?, userId: String?, user: String?
    ) {
        val note = Note(
            title = title,
            body = body,
            url = url,
            topicId = topicId,
            topicTitle = topic,
            postId = postId,
            userId = userId,
            userName = user,
            date = GregorianCalendar.getInstance().time
        )

        if (local) {
            notesDao.insert(note)
        } else {
            val requestUrl = getUrl("ins")

            notesDao.merge(notesService.post(requestUrl, note).notesOrError())
        }
    }
}

private fun Response<List<Note>>.notesOrError(): List<Note> {
    val response = this
    return if (response.isSuccessful && response.body() != null) {
        response.body()!!
    } else {
        @Suppress("BlockingMethodInNonBlockingContext")
        throw NotReportException(
            response.errorBody()?.string() ?: response.message()
            ?: "Произошла ошибка при запросе"
        )
    }
}
