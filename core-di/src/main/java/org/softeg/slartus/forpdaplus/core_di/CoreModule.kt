package org.softeg.slartus.forpdaplus.core_di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core_di.implementations.AppDatabaseImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.HtmlStylePreferencesImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.NoteDaoImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.NotesPreferencesImpl
import org.softeg.slartus.forpdaplus.core_ui.html.HtmlStylePreferences
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    abstract fun bindNoteDao(noteDaoImpl: NoteDaoImpl):
            org.softeg.slartus.forpdaplus.feature_notes.NotesDao

    @Binds
    abstract fun bindAppDatabase(appDatabaseImpl: AppDatabaseImpl):
            org.softeg.slartus.forpdaplus.feature_notes.AppDatabase

    @Binds
    abstract fun bindNotesPreferences(notesPreferencesImpl: NotesPreferencesImpl):
            NotesPreferences

    @Binds
    abstract fun bindHtmlStylePreferences(htmlStylePreferencesImpl: HtmlStylePreferencesImpl):
            HtmlStylePreferences
}