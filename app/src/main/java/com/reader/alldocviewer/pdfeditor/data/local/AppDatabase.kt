package com.reader.alldocviewer.pdfeditor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.data.local.entity.RecyclerMediaFile

@Database(
    entities = [ModelFile::class, RecyclerMediaFile::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}
