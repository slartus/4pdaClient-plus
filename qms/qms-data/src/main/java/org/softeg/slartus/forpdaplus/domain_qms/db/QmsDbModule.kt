package org.softeg.slartus.forpdaplus.domain_qms.db

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
class QmsDbModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext app: Context): QmsDatabase {
        return Room
            .databaseBuilder(app, QmsDatabase::class.java, QmsDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQmsContactsDao(db: QmsDatabase): QmsContactsDao = db.qmsContactsDao()
}