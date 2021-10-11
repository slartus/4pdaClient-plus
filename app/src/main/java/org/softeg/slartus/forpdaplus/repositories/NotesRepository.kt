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
import org.softeg.slartus.forpdaplus.feature_preferences.Preferences
import java.util.*
import javax.inject.Inject

class NotesRepository @Inject constructor() {
    private object Holder {
        val INSTANCE = NotesRepository()
    }

    companion object {

        @JvmStatic
        val instance by lazy { Holder.INSTANCE }

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
            return Preferences.Notes.remoteUrl
        }

    private fun getUrl(action: String): String {
        return getUrl(apiUrl, action)
    }

    private val notesSubject: BehaviorSubject<List<Note>> = BehaviorSubject.createDefault(emptyList())
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

    fun getNote(id: String): Note? {
        return if (local) {
            NotesTable.getNote(id)
        } else {
            notesSubject.value?.firstOrNull { it.id == id }
        }
    }

    init {
        load()
    }
}