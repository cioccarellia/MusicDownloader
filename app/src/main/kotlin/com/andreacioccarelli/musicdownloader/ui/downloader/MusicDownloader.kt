package com.andreacioccarelli.musicdownloader.ui.downloader

import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.extensions.getVideoId
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsyncResult

/**
 * Designed and developed by Andrea Cioccarelli
 */
class MusicDownloader {

    private val data: MutableList<String>
    private val downloadFormat: Format
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    constructor(url: String, format: Format) {
        data = mutableListOf(url)
        downloadFormat = format
    }

    constructor(urls: List<String>, format: Format) {
        data = urls.toMutableList()
        downloadFormat = format
    }

    public fun exec(format: Format) {
        when {
            data.size > 1 -> startFilesDownload(format)
            data.size == 1 -> startFileDownload(format)
            else -> throw IllegalStateException()
        }
    }

    private fun fetchMeta(url: String): DirectLinkResponse {
        val videoId = url.getVideoId()
        if (videoId == "") throw IllegalStateException()

        return uiScope.doAsyncResult {
            val requestBuilder = DownloadLinkRequestsBuilder.get(videoId, downloadFormat)
            val request = OkHttpClient().newCall(requestBuilder).execute()
            val gson = Gson()

            gson.fromJson(request.body()!!.string(), DirectLinkResponse::class.java)
        }.get()
    }

    public fun startFileDownload(format: Format) {
        val meta = fetchMeta(data[0])
    }

    public fun startFilesDownload(format: Format) {
        val meta = data.map { fetchMeta(it) }

    }


}