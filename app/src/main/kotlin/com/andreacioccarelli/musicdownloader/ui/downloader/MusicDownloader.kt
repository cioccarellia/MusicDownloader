package com.andreacioccarelli.musicdownloader.ui.downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import androidx.annotation.UiThread
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.model.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.extensions.*
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistStore
import com.andreacioccarelli.musicdownloader.data.model.KnownError
import com.andreacioccarelli.musicdownloader.util.ToastUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import kotlin.random.Random

/**
 * Designed and Developed by Andrea Cioccarelli
 */
class MusicDownloader {

    private var activity: Activity?
    private val data: MutableList<String>
    private lateinit var format: Format

    constructor(activity: Activity?, url: String) {
        data = mutableListOf(url)
        this.activity = activity
    }

    constructor(activity: Activity?, urls: List<String>) {
        data = urls.toMutableList()
        this.activity = activity
    }

    fun exec(_format: Format) {
        format = _format
        when {
            data.size > 1 -> startFilesDownload(_format)
            data.size == 1 -> startFileDownload(_format)
            else -> throw IllegalStateException()
        }
    }

    private suspend fun fetchVideoDownloadInformation(url: String, format: Format): DirectLinkResponse {
        val videoId = url.getVideoIdOrThrow()

        val requestBuilder = DownloadLinkRequestsBuilder.get(videoId, format)
        val request = OkHttpClient().newCall(requestBuilder).execute()

        val response = Gson().fromJson(request.body()!!.string(), DirectLinkResponse::class.java)

        logd(response)
        return when (response.state) {
            RESPONSE_OK, RESPONSE_ERROR -> response
            RESPONSE_WAIT, RESPONSE_PROCESSING -> {
                // If the video is processing, wait until it's compleated
                delay(Random.nextLong(500, 1000))
                fetchVideoDownloadInformation(url, format)
            }
            else -> {
                // Unknown response received
                loge(response)
                ToastUtil.error("Illegal server response (${response.state}) while downloading video ${response.videoId} [${response.reason}]")
                throw IllegalStateException()
            }
        }
    }

    private fun startFileDownload(format: Format) {
        UiController.displayRetrievingVideoInformation(activity, data)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = fetchVideoDownloadInformation(data[0], format)

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
        UiController.displayRetrievingListInformation(activity, data)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val responses = data.map {
                    fetchVideoDownloadInformation(it, format)
                }

                delay(1000)
                withContext(Dispatchers.Main) {
                    downloadFileList(responses)
                }
            } catch (exception: RuntimeException) {
                loge(exception)
                withContext(Dispatchers.Main) {
                    UiController.displayError(activity, KnownError.BATCH_FAILED, data)
                }
            }
        }
    }



    @UiThread
    private fun downloadFileList(totalVideos: List<DirectLinkResponse>) {
        val convertedVideos = totalVideos.filter {
            it.isSuccessful()
        }

        if (convertedVideos.size != totalVideos.size) {
            val missCount = totalVideos.size - convertedVideos.size
            error("$missCount file${plural(missCount)} couldn't be converted. Processing ${convertedVideos.size} file${plural(convertedVideos.size)}")
        }

        UiController.displayDownloadStarted(activity, convertedVideos)
        val downloadManager = App.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        for (response in totalVideos) {
            downloadFile(downloadManager = downloadManager,
                    response = response,
                    isSingle = false)
        }
    }

    @UiThread
    private fun downloadFile(downloadManager: DownloadManager = App.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager,
                             response: DirectLinkResponse,
                             isSingle: Boolean = true) {

        ChecklistStore.remove(response.videoId)

        if (response.isUnsuccessful()) {
            // If the conversion has failed and the download target was 1, we can print to the user the reason.
            if (isSingle) {
                when (response.reason) {
                    ERROR_LENGTH ->                 UiController.displayError(activity, KnownError.VIDEO_LENGTH, response.videoId)
                    ERROR_MALFORMED ->              UiController.displayError(activity, KnownError.MALFORMED_URL, response.videoId)
                    ERROR_UNADDRESSABLE_VIDEO ->    UiController.displayError(activity, KnownError.UNADDRESSABLE_VIDEO, response.videoId)
                    else ->                         UiController.displayError(activity, KnownError.UNKNOWN_ERROR, response.videoId)
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

                setDestinationInExternalPublicDir("", "MusicDownloader/$format/$fileName")
            }

            downloadManager.enqueue(downloadRequest)
            ChecklistStore.remove(response.videoId)
        }
    }
}
