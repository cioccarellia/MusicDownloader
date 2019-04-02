package com.andreacioccarelli.musicdownloader.data.checklist

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Entity(tableName = "checklist")
data class ChecklistEntry(
        @PrimaryKey val videoId: String,
        val title: String,
        val thumbnailLink: String)