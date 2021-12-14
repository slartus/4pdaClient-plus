package org.softeg.slartus.forpdaplus.repositories

import android.net.Uri
import com.google.gson.GsonBuilder
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.Client
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.db.NotesTable
import org.softeg.slartus.forpdaplus.notes.Note
import org.softeg.slartus.forpdaplus.prefs.Preferences
import java.util.*

class NotesRepository private constructor() {
    private object Holder {
        val INSTANCE = NotesRepository()
    }

    companion object {
        private val TAG = NotesRepository::class.simpleName

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }

        fun checUrlAsync(
            baseUrl: String,
            successAction: () -> Unit,
            errorAction: (ex: Throwable) -> Unit
        ) {
            InternetConnection.instance.loadDataOnInternetConnected({
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val url = getUrl(baseUrl, "get")
                        val response = Client.getInstance().performGet(url).responseBody
                        parseNotes(response)

                        withContext(Dispatchers.Main) {
                            successAction()
                        }
                    } catch (ex: Throwable) {
                        withContext(Dispatchers.Main) {
                            errorAction(ex)
                        }
                    }
                }
            })
        }

        private fun getUrl(baseUrl: String, action: String): String {
            val uriBuilder = Uri.parse(baseUrl).buildUpon()
            uriBuilder.appendQueryParameter("action", action)
            return uriBuilder.build().toString()
        }

        private fun parseNotes(response: String): List<Note> {
            val gson = GsonBuilder()
                .setDateFormat("yyyy.MM.dd HH:mm:ss")
                .create()

            return gson.fromJson(response, Array<Note>::class.java).toList()
        }
    }

    private val local: Boolean
        get() {
            return Preferences.Notes.isLocal
        }


    private val apiUrl: String
        get() {
            return Preferences.Notes.remoteUrl ?: ""
        }

    private fun getUrl(action: String): String {
        return getUrl(apiUrl, action)
    }

    val notesSubject: BehaviorSubject<List<Note>> = BehaviorSubject.createDefault(emptyList())
    fun load() {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val notes = NotesTable.getNotes(null)
                    notesSubject.onNext(notes.toList())
                } catch (ex: Throwable) {
                    notesSubject.onNext(notesSubject.value ?: emptyList())
                    withContext(Dispatchers.Main) {
                        AppLog.e(ex)
                    }
                }
            }
        } else {
            InternetConnection.instance.loadDataOnInternetConnected({
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val url = getUrl("get")
                        val response = Client.getInstance().performGet(url).responseBody
                        notesSubject.onNext(parseNotes(response))
                    } catch (ex: Throwable) {
                        notesSubject.onNext(notesSubject.value ?: emptyList())
                        withContext(Dispatchers.Main) {
                            AppLog.e(ex)
                        }
                    }
                }
            })
        }
    }

    fun delete(id: String) {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                NotesTable.delete(id)
            }
            load()
        } else {
            InternetConnection.instance.loadDataOnInternetConnected({
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
                        AppLog.e(
                            Exception(
                                "notes delete:" + (ex.localizedMessage
                                    ?: ex.message), ex
                            )
                        )
                    }
                }
            })
        }
    }

    fun getNote(id: String): Note? {
        return if (local) {
            NotesTable.getNote(id)
        } else {
            notesSubject.value?.firstOrNull { it.id == id }
        }
    }

    fun insertRow(
        title: String?, body: String?, url: String?, topicId: CharSequence, topic: String?,
        postId: String?, userId: String?, user: String?, action: () -> Unit
    ) {
        if (local) {
            GlobalScope.launch(Dispatchers.IO) {
                NotesTable.insertRow(title, body, url, topicId, topic, postId, userId, user)
                action()
            }

        } else {
            InternetConnection.instance.loadDataOnInternetConnected({
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
                        val response = Client.getInstance()
                            .performPost(requestUrl, gson.toJson(note)).responseBody
                        notesSubject.onNext(parseNotes(response))

                        action()
                    } catch (ex: Throwable) {
                        notesSubject.onNext(notesSubject.value ?: emptyList())
                        AppLog.e(
                            Exception(
                                "notes insert:" + (ex.localizedMessage
                                    ?: ex.message), ex
                            )
                        )
                    }
                }
            })
        }
    }

    init {
        load()
    }
}