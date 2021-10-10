package org.softeg.slartus.forpdaplus.core_db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core_db.note.NoteDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class AppDbModule {

    @Provides
    @Singleton
    open fun provideDb(@ApplicationContext app: Context): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    open fun provideNoteDao(db: AppDatabase): NoteDao {
        return db.noteDao()
    }
}