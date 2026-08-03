package com.reader.alldocviewer.pdfeditor.di

import android.content.Context
import androidx.room.Room
import com.reader.alldocviewer.pdfeditor.data.local.AppDatabase
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            FileConst.DB_NAME,
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideFileDao(database: AppDatabase): FileDao = database.fileDao()
}
