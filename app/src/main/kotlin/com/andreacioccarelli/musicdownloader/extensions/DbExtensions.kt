package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDao
import com.andreacioccarelli.musicdownloader.data.model.DownloadInfo

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun ChecklistDao.isEmpty() = getAll().isEmpty()
fun ChecklistDao.toDownloadInfoList() = getAll().map { DownloadInfo(it.link, it.title) }