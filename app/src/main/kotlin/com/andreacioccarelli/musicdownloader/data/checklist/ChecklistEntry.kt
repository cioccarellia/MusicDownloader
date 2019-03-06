package com.andreacioccarelli.musicdownloader.data.checklist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Entity(tableName = "checklist")
data class ChecklistEntry(
        @PrimaryKey                         val link: String,
        @ColumnInfo(name = "title")         val title: String,
        @ColumnInfo(name = "thumb")         val thumbnailLink: String)