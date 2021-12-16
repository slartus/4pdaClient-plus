package org.softeg.slartus.forpdaplus.core_db

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.ForumPreferences
import org.softeg.slartus.forpdaplus.core.db.ForumTable
import org.softeg.slartus.forpdaplus.core_db.forum.ForumDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppDbModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, AppDatabase.NAME)
            //.addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideForumDao(db: AppDatabase): ForumDao {
        return db.forumDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface CoreImplementationsModule {
    @Binds
    @Singleton
    fun bindForumTableImpl(forumTableImpl: ForumTableImpl): ForumTable
}