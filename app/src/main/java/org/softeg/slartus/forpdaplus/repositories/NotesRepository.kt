package org.softeg.slartus.forpdaplus.repositories

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.google.gson.GsonBuilder
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.classes.Post
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.DbHelper
import org.softeg.slartus.forpdaplus.db.NotesDbHelper
import org.softeg.slartus.forpdaplus.db.NotesTable
import org.softeg.slartus.forpdaplus.notes.Note
import java.util.*

class NotesRepository private constructor() {
    private object Holder {
        val INSTANCE = NotesRepository()
    }

    companion object {
        private val TAG = NotesRepository::class.simpleName

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

    private val local = false
    private val apiUrl
        get() = "https://slartus.ru/4pda/notes.php?password=MY_PASSWORD"

    private fun getUrl(action: String): String {
        val uriBuilder = Uri.parse(apiUrl).buildUpon()
        uriBuilder.appendQueryParameter("action", action)
        return uriBuilder.build().toString()
    }

    val notesSubject: BehaviorSubject<List<Note>> = BehaviorSubject.createDefault(emptyList())
    private fun load() {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                val notes = NotesTable.getNotes(null)
                notesSubject.onNext(notes.toList())
            }
        } else {
            InternetConnection.instance.loadDataOnInternetConnected {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val url = getUrl("get")
                        val response = Client.getInstance().performGet(url).responseBody
                        notesSubject.onNext(parseNotes(response))
                    } catch (ex: Throwable) {
                        notesSubject.onNext(notesSubject.value ?: emptyList())
                        AppLog.e(ex)
                    }
                }
            }
        }
    }

    private fun parseNotes(response: String): List<Note> {
        val gson = GsonBuilder()
                .setDateFormat("yyyy.MM.dd HH:mm:ss")
                .create()

        return gson.fromJson(response, Array<Note>::class.java).toList()
    }

    fun delete(id: String) {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                NotesTable.delete(id)
            }
            load()
        } else {
            InternetConnection.instance.loadDataOnInternetConnected {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val uriBuilder = Uri.parse(apiUrl).buildUpon()
                        uriBuilder.appendQueryParameter("action", "del")
                        uriBuilder.appendQueryParameter("id", id)
                        val url = uriBuilder.build().toString()
                        val response = Client.getInstance().performGet(url).responseBody
                        notesSubject.onNext(parseNotes(response))
                    } catch (ex: Throwable) {
                        notesSubject.onNext(notesSubject.value ?: emptyList())
                        AppLog.e(ex)
                    }
                }
            }
        }
    }

    fun insertRow(title: String?, body: String?, url: String?, topicId: CharSequence, topic: String?,
                  postId: String?, userId: String?, user: String?, action: () -> Unit) {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                NotesTable.insertRow(title, body, url, topicId, topic, postId, userId, user)
                action()
            }

        } else {
            InternetConnection.instance.loadDataOnInternetConnected {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val uriBuilder = Uri.parse(apiUrl).buildUpon()
                        uriBuilder.appendQueryParameter("action", "ins")

                        val gson = GsonBuilder()
                                .setDateFormat("yyyy.MM.dd HH:mm:ss")
                                .create()
                        val note = Note().apply {
                            Title = title
                            Body = body
                            Url = url
                            TopicId = topicId.toString()
                            Topic = topic
                            PostId = postId
                            UserId = userId
                            User = user
                            Date = Date()
                        }

                        val requestUrl = uriBuilder.build().toString()
                        val response = Client.getInstance().performPost(requestUrl, gson.toJson(note)).responseBody
                        notesSubject.onNext(parseNotes(response))

                        action()
                    } catch (ex: Throwable) {
                        notesSubject.onNext(notesSubject.value ?: emptyList())
                        AppLog.e(ex)
                    }
                }
            }
        }
    }

    init {
        load()
    }
}