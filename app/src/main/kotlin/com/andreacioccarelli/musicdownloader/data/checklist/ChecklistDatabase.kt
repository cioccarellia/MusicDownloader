package com.andreacioccarelli.musicdownloader.data.checklist

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 *  Designed and developed by Andrea Cioccarelli
 */

@Database(entities = [ChecklistEntry::class], version = 2)
abstract class ChecklistDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao
}