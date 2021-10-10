package org.softeg.slartus.forpdaplus.feature_notes.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.softeg.slartus.forpdaplus.feature_notes.Note
import org.softeg.slartus.forpdaplus.feature_notes.NotesDao
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import org.softeg.slartus.forpdaplus.feature_notes.network.NotesService
import java.util.*
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val notesService: NotesService,
    private val notesDao: NotesDao,
    private val notesPreferences: NotesPreferences
) {
    private val _notes: MutableStateFlow<List<Note>> = MutableStateFlow(emptyList())
    val notes: StateFlow<List<Note>> = _notes
    private val local: Boolean
        get() {
            return notesPreferences.isLocal
        }

    private val apiUrl: String
        get() {
            return notesPreferences.remoteUrl ?: ""
        }

    suspend fun checkUrl(
        baseUrl: String,
    ) {

        val url = getUrl(baseUrl, "get")
        val response = notesService.request(url)
        if (!response.isSuccessful) {
            throw error(response.errorBody()?.string() ?: response.message())
        }
    }

    private fun getUrl(action: String): String {
        return getUrl(apiUrl, action)
    }

    private fun getUrl(baseUrl: String, action: String): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        uriBuilder.appendQueryParameter("action", action)
        return uriBuilder.build().toString()
    }

    suspend fun load() {
        if (!local) {
            val url = getUrl("get")
            val response = notesService.request(url)
            val notes = response.body() ?: emptyList()
            notesDao.merge(notes)
        }
    }

    suspend fun delete(id: String) {
        if (local) {
            notesDao.delete(id.toInt())
        } else {
            val uriBuilder = Uri.parse(apiUrl).buildUpon()
            uriBuilder.appendQueryParameter("action", "del")
            uriBuilder.appendQueryParameter("id", id)
            val url = uriBuilder.build().toString()
            notesService.request(url)
        }
        load()
    }

    suspend fun getNote(id: String): Note? {
        return if (local) {
            notesDao.get(id.toInt())
        } else {
            _notes.value.firstOrNull { it.id == id.toInt() }
        }
    }

    suspend fun insertRow(
        title: String?, body: String?, url: String?, topicId: CharSequence, topic: String?,
        postId: String?, userId: String?, user: String?, action: () -> Unit
    ) {
        if (local) {
            val note = Note(
                title = title,
                body = body,
                url = url,
                topicId = topicId.toString(),
                topicTitle = topic,
                postId = postId,
                userId = userId,
                userName = user,
                date = GregorianCalendar.getInstance().time
            )
            notesDao.insert(note)
            action()

        } else {
//            val uriBuilder = Uri.parse(apiUrl).buildUpon()
//            uriBuilder.appendQueryParameter("action", "ins")
//
//            val gson = GsonBuilder()
//                .setDateFormat("yyyy.MM.dd HH:mm:ss")
//                .create()
//            val note = Note().apply {
//                Title = title
//                Body = body
//                Url = url
//                TopicId = topicId.toString()
//                Topic = topic
//                PostId = postId
//                UserId = userId
//                User = user
//                Date = Date()
//            }
//
//            val requestUrl = uriBuilder.build().toString()
//            val response = Client.getInstance()
//                .performPost(requestUrl, gson.toJson(note)).responseBody
//            notesSubject.onNext(parseNotes(response))
//
//            action()
        }
    }
}