package com.andreacioccarelli.musicdownloader.data.checklist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Dao
interface ChecklistDao {
    @Insert
    fun add(entry: ChecklistEntry)

    @Query("SELECT * FROM checklist")
    fun getAll(): List<ChecklistEntry>

    @Query("SELECT * FROM checklist WHERE link = :link")
    fun findByLink(link: String): List<ChecklistEntry>

    @Query("DELETE from checklist WHERE link = :link")
    fun remove(link: String)

    @Delete
    fun remove(entry: ChecklistEntry)
}