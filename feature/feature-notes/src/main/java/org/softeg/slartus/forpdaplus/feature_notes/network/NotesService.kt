package org.softeg.slartus.forpdaplus.feature_notes.network

import org.softeg.slartus.forpdaplus.feature_notes.Note
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface NotesService {
    @GET
    suspend fun request(@Url url:String): Response<List<Note>>

    @POST
    suspend fun post(@Url url:String, @Body note:Note): Response<List<Note>>
}