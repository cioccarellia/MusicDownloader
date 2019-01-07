package com.andreacioccarelli.musicdownloader.ui.downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.extensions.getVideoIdOrThrow
import com.andreacioccarelli.musicdownloader.extensions.sanitize
import com.andreacioccarelli.musicdownloader.extensions.toUri
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * Designed and Developed by Andrea Cioccarelli
 */
class MusicDownloader {

    private var activity: Activity?
    private val data: MutableList<String>

    constructor(activity: Activity?, url: String) {
        data = mutableListOf(url)
        this.activity = activity
    }

    constructor(activity: Activity?, urls: List<String>) {
        data = urls.toMutableList()
        this.activity = activity
    }

    fun exec(format: Format) {
        when {
            data.size > 1 -> startFilesDownload(format)
            data.size == 1 -> startFileDownload(format)
            else -> throw IllegalStateException()
        }
    }

    private fun fetchMeta(url: String, format: Format): DirectLinkResponse {
        val videoId = url.getVideoIdOrThrow()

        val requestBuilder = DownloadLinkRequestsBuilder.get(videoId, format)
        val request = OkHttpClient().newCall(requestBuilder).execute()

        val response = Gson().fromJson(request.body()!!.string(), DirectLinkResponse::class.java)

        when (response.state) {
            RESPONSE_OK -> return response
            RESPONSE_WAIT, RESPONSE_PROCESSING -> return fetchMeta(url, format)
            RESPONSE_ERROR -> {
                when (response.state) {
                    ERROR_LENGTH -> {
                        UiController.displayError(activity, KnownError.VIDEO_LENGTH, response, url)
                    }
                    ERROR_MALFORMED -> {
                        UiController.displayError(activity, KnownError.MALFORMED_URL, response, url)
                    }
                    ERROR_UNADDRESSABLE_VIDEO -> {
                        UiController.displayError(activity, KnownError.UNADDRESSABLE_VIDEO, response, url)
                    }
                }

                throw IllegalStateException()
            }
            else -> {
                // Unknown response
                loge(response)
                com.andreacioccarelli.musicdownloader.extensions.error("Illegal server response (${response.state}) while downloading video ${response.videoId} [${response.reason}]")
                throw IllegalStateException()
            }
        }
    }

    private fun downloadFiles(responses: List<DirectLinkResponse>) {
        val downloadManager = App.instance.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        UiController.displayDownloadStarted(activity, responses)

        for (response in responses) {
            val fileName = "${response.title}.${response.format}"
            val fileDownloadLink = response.download.sanitize()

            val uri = fileDownloadLink.toUri()
            logd(fileDownloadLink, fileName)

            val downloadRequest = DownloadManager.Request(uri)

            with(downloadRequest) {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(true)
                setVisibleInDownloadsUi(true)
                setTitle("Downloading ${response.title}")
                setDescription(fileName)
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                setDestinationInExternalPublicDir("", "MusicDownloader/$fileName")
            }

            downloadManager.enqueue(downloadRequest)
        }
    }

    private fun startFileDownload(format: Format) {
        UiController.displayProgressMetaRetrievingSingle(activity, data[0])
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val meta = fetchMeta(data[0], format)
                downloadFiles(listOf(meta))
            } catch (ignored: IllegalStateException) {}
        }
    }

    private fun startFilesDownload(format: Format) {
        UiController.displayProgressMetaRetrievingList(activity, data)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val meta = data.map { fetchMeta(it, format) }
                downloadFiles(meta)
            } catch (ignored: IllegalStateException) {}
        }
    }
}