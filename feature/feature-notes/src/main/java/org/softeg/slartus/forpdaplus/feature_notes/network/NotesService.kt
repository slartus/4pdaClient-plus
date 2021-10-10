package org.softeg.slartus.forpdaplus.feature_notes.network

import org.softeg.slartus.forpdaplus.feature_notes.Note
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface NotesService {
    @GET
    suspend fun request(@Url url:String): Response<List<Note>>
}