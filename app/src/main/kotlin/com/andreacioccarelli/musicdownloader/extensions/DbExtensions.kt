package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDao

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun ChecklistDao.isEmpty() = getAll().isEmpty()