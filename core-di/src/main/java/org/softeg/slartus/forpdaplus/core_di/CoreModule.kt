package org.softeg.slartus.forpdaplus.core_di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core_di.implementations.AppDatabaseImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.NoteDaoImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.NotesManagerImpl

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Binds
    abstract fun bindNotesManager(notesManagerImpl: NotesManagerImpl): org.softeg.slartus.forpdaplus.feature_preferences.di.NotesManager

    @Binds
    abstract fun bindNoteDao(noteDaoImpl: NoteDaoImpl): org.softeg.slartus.forpdaplus.feature_notes.NotesDao

    @Binds
    abstract fun bindAppDatabase(appDatabaseImpl: AppDatabaseImpl): org.softeg.slartus.forpdaplus.feature_notes.AppDatabase
}