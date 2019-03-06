package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDao

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun ChecklistDao.isEmpty() = getAll().isEmpty()
fun ChecklistDao.find(link: String) = _find("%$link%")
fun ChecklistDao.remove(link: String) = _remove("%$link%")