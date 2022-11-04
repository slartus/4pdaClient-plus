package org.softeg.slartus.forpdaplus.forum.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppDbModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): ForumDatabase {
        return Room
            .databaseBuilder(app, ForumDatabase::class.java, ForumDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideForumDao(db: ForumDatabase): ForumDao = db.forumDao()
}