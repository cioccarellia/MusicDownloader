package com.andreacioccarelli.musicdownloader.ui.downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import androidx.annotation.UiThread
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.extensions.*
import com.andreacioccarelli.musicdownloader.util.ChecklistStore
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File

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

        logd(response)
        return when (response.state) {
            RESPONSE_OK, RESPONSE_ERROR -> response
            RESPONSE_WAIT, RESPONSE_PROCESSING -> fetchMeta(url, format)
            else -> {
                // Unknown response
                loge(response)
                com.andreacioccarelli.musicdownloader.extensions.error("Illegal server response (${response.state}) while downloading video ${response.videoId} [${response.reason}]")
                throw IllegalStateException()
            }
        }
    }

    @UiThread
    private fun downloadFiles(responses: List<DirectLinkResponse>) {
        val successful = responses.filter { it.isSuccessful() }
        if (successful.size != responses.size) {
            val missed = responses.size - successful.size
            error("$missed file${plural(missed)} couldn't be converted. Processing ${successful.size} file${plural(successful.size)}")
        }

        UiController.displayDownloadStarted(activity, successful)
        val downloadManager = App.instance.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        for (response in responses) {
            downloadFile(downloadManager = downloadManager, response = response, isSingle = false)
        }
    }

    @UiThread
    private fun downloadFile(downloadManager: DownloadManager = App.instance.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager,
                             response: DirectLinkResponse,
                             isSingle: Boolean = true) {

        ChecklistStore.remove(response.videoId)

        if (response.isUnsuccessful()) {
            if (isSingle) {
                when (response.reason) {
                    ERROR_LENGTH -> UiController.displayError(activity, KnownError.VIDEO_LENGTH, response.videoId)
                    ERROR_MALFORMED -> UiController.displayError(activity, KnownError.MALFORMED_URL, response.videoId)
                    ERROR_UNADDRESSABLE_VIDEO -> UiController.displayError(activity, KnownError.UNADDRESSABLE_VIDEO, response.videoId)
                    else -> UiController.displayError(activity, KnownError.UNKNOWN_ERROR, response.videoId)
                }
            }

            return
        }

        if (isSingle) {
            UiController.displayDownloadStarted(activity, response)
        }

        GlobalScope.launch(Dispatchers.IO) {
            val fileName = "${response.title.toFileName()}.${response.format}"
            val fileDownloadLink = response.download.sanitize()

            File(fileName).delete()

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
            ChecklistStore.remove(response.videoId)
        }
    }

    private fun startFileDownload(format: Format) {
        UiController.displayProgressMetaRetrievingSingle(activity, data)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = fetchMeta(data[0], format)

                delay(1000)
                withContext(Dispatchers.Main) {
                    downloadFile(response = response)
                }
            } catch (exception: RuntimeException) {
                loge(exception)
                withContext(Dispatchers.Main) {
                    UiController.displayError(activity, KnownError.UNKNOWN_ERROR, data)
                }
            }
        }
    }

    private fun startFilesDownload(format: Format) {
        UiController.displayProgressMetaRetrievingList(activity, data)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val responses = data.map { fetchMeta(it, format) }

                delay(1000)

                withContext(Dispatchers.Main) {
                    downloadFiles(responses)
                }
            } catch (exception: RuntimeException) {
                loge(exception)
                withContext(Dispatchers.Main) {
                    UiController.displayError(activity, KnownError.BATCH_FAILED, data)
                }
            }
        }
    }
}
