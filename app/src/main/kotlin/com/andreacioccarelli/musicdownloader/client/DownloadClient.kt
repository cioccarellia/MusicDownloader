package com.andreacioccarelli.musicdownloader.client

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.App.Companion.checklist
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.enums.FailedConversionError
import com.andreacioccarelli.musicdownloader.data.enums.Format
import com.andreacioccarelli.musicdownloader.data.model.DownloadInfo
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.extensions.*
import com.andreacioccarelli.musicdownloader.ui.toast.ToastUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import kotlin.random.Random

/**
 * Designed and Developed by Andrea Cioccarelli
 */

class DownloadClient {

    private var activity: Activity?
    private val data: MutableList<DownloadInfo>
    private lateinit var format: Format

    constructor(activity: Activity?, info: DownloadInfo) {
        data = mutableListOf(info)
        this.activity = activity
    }

    constructor(activity: Activity?, urls: List<DownloadInfo>) {
        data = urls.toMutableList()
        this.activity = activity
    }

    @UiThread
    fun exec(outputFormat: Format) {
        format = outputFormat
        when {
            data.size > 1 -> startFilesDownload(outputFormat)
            data.size == 1 -> startFileDownload(outputFormat)
            else -> throw IllegalStateException("Negative data size")
        }
    }

    private fun getFileDownloadPath(fileName: String, trimForSdCard: Boolean = false): String {
        val path = "MusicDownloader/$format/$fileName"

        return if (trimForSdCard) path else Environment.getExternalStorageDirectory().absolutePath + "/" + path
    }

    @WorkerThread
    private suspend fun fetchVideoDownloadInformation(downloadInfo: DownloadInfo, format: Format): DirectLinkResponse {
        val videoId = downloadInfo.url.attemptExtractingVideoId()

        val requestBuilder = DownloadLinkRequestsBuilder.get(videoId, format)
        val request = OkHttpClient().newCall(requestBuilder).execute()

        val response = Gson().fromJson(request.body!!.string(), DirectLinkResponse::class.java)

        logd(response)

        return when (response.state) {
            RESPONSE_OK, RESPONSE_ERROR -> {
                response.apply {
                    fileName = downloadInfo.fileName
                }
            }
            RESPONSE_WAIT, RESPONSE_PROCESSING -> {
                // If the video is processing, wait until it's compleated
                delay(Random.nextLong(107, 1000))
                fetchVideoDownloadInformation(downloadInfo, format)
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

                withContext(Dispatchers.Main) {
                    downloadFile(response = response)
                }
            } catch (exception: RuntimeException) {
                loge(exception)
                withContext(Dispatchers.Main) {
                    UiController.displayError(activity, FailedConversionError.UNKNOWN_ERROR, data.map { it.fileName })
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

                withContext(Dispatchers.Main) {
                    downloadFileList(responses)
                }
            } catch (exception: RuntimeException) {
                loge(exception)
                withContext(Dispatchers.Main) {
                    UiController.displayError(activity, FailedConversionError.BATCH_FAILED, data.map { it.fileName })
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
            ToastUtil.error("$missCount file${plural(missCount)} couldn't be converted. Processing ${convertedVideos.size} file${plural(convertedVideos.size)}")
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
    private fun downloadFile(
        downloadManager: DownloadManager = App.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager,
        response: DirectLinkResponse,
        isSingle: Boolean = true
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            checklist.remove(response.videoId)
            App.checklistedIds.add(response.videoId)
        }

        if (response.isUnsuccessful()) {
            // If the conversion has failed and the download target was 1, we can print to the user the reason.
            if (isSingle) {
                when (response.reason) {
                    ERROR_LENGTH ->                 UiController.displayError(activity, FailedConversionError.VIDEO_LENGTH, response.fileName)
                    ERROR_MALFORMED ->              UiController.displayError(activity, FailedConversionError.MALFORMED_URL, response.fileName)
                    ERROR_UNADDRESSABLE_VIDEO ->    UiController.displayError(activity, FailedConversionError.UNADDRESSABLE_VIDEO, response.fileName)
                    else ->                         UiController.displayError(activity, FailedConversionError.UNKNOWN_ERROR, response.fileName)
                }
            }
            return
        }

        if (isSingle) {
            UiController.displayDownloadStarted(activity, response)
        }

        GlobalScope.launch(Dispatchers.IO) {
            val fileName = "${response.fileName}.${response.format}"
            val fileDownloadLink = response.download.sanitizeUrl()

            // Remove file if it already exists
            File(getFileDownloadPath(fileName)).delete()

            val uri = fileDownloadLink.toUri()
            logd(fileDownloadLink, fileName)

            val downloadRequest = DownloadManager.Request(uri)

            with(downloadRequest) {
                setAllowedOverRoaming(true)
                setVisibleInDownloadsUi(true)
                setTitle(response.fileName)
                setDescription(fileName)
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir("", getFileDownloadPath(fileName, trimForSdCard = true))
            }

            delay(Random.nextLong(1000, 2000))

            downloadManager.enqueue(downloadRequest)
            checklist.remove(response.videoId)
            App.checklistedIds.add(response.videoId)
        }
    }
}