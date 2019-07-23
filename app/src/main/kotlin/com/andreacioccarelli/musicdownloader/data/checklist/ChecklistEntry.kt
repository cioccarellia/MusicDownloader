package com.andreacioccarelli.musicdownloader.data.checklist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andreacioccarelli.musicdownloader.data.serializers.Result

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Entity(tableName = "checklist")
data class ChecklistEntry(
        @PrimaryKey val videoId: String,
        val title: String,
        val thumbnailLink: String
) {
    constructor(result: Result) : this(result.id.videoId, result.snippet.title, result.snippet.thumbnails.medium.url)
}