package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDao
import com.andreacioccarelli.musicdownloader.data.model.DownloadInfo

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun ChecklistDao.isEmpty() = getAll().isEmpty()
fun ChecklistDao.toDownloadInfoList() = getAll().map { DownloadInfo(it.videoId.toYoutubeUrl(), it.title) }
fun ChecklistDao.contains(videoId: String) = getAll().any { it.videoId == videoId }