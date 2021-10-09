package org.softeg.slartus.forpdaplus.core_di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core_di.implementations.NoteDaoImpl
import org.softeg.slartus.forpdaplus.core_di.implementations.NotesManagerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Binds
    abstract fun bindNotesManager(notesManagerImpl: NotesManagerImpl): org.softeg.slartus.forpdaplus.feature_preferences.di.NotesManager

    @Binds
    abstract fun bindNoteDao(noteDaoImpl: NoteDaoImpl): org.softeg.slartus.forpdaplus.feature_notes.NotesDao
}