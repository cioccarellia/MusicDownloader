package com.andreacioccarelli.musicdownloader.ui.downloader

import android.app.Activity
import androidx.annotation.UiThread
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.data.enums.KnownError
import com.andreacioccarelli.musicdownloader.data.model.DownloadInfo
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.ui.gradients.GradientGenerator
import com.tapadoo.alerter.Alerter

/**
 * Designed and Developed by Andrea Cioccarelli
 */

object UiController {

    @UiThread
    fun displayProgressMetaRetrievingSingle(activity: Activity?, download: DownloadInfo)
            = displayRetrievingVideoInformation(activity, listOf(download))

    @UiThread
    fun displayRetrievingVideoInformation(activity: Activity?, downloads: List<DownloadInfo>) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        Alerter.create(activity)
                .enableProgress(true)
                .setDismissable(false)
                .setIcon(R.drawable.download)
                .setTitle("Initializing conversion")
                .setText("Retrieving download information for video ${downloads[0]}")
                .setBackgroundDrawable(GradientGenerator.random())
                .enableInfiniteDuration(true)
                .show()
    }

    @UiThread
    fun displayRetrievingListInformation(activity: Activity?, downloads: List<DownloadInfo>) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        Alerter.create(activity)
                .enableProgress(true)
                .setDismissable(false)
                .setIcon(R.drawable.download)
                .setTitle("Initializing conversions")
                .setText("Retrieving download information for ${downloads.size} videos")
                .setBackgroundDrawable(GradientGenerator.random())
                .enableInfiniteDuration(true)
                .show()
    }

    @UiThread fun displayDownloadStarted(activity: Activity?, downloads: DirectLinkResponse) = displayDownloadStarted(activity, listOf(downloads))
    @UiThread fun displayDownloadStarted(activity: Activity?, downloads: List<DirectLinkResponse>) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        when {
            downloads.size == 1 -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.download)
                        .setTitle("Starting download")
                        .setText("For video \"${downloads[0].title}\"")
                        .setBackgroundDrawable(GradientGenerator.random())
                        .setDuration(7_000)
                        .show()
            }
            downloads.size > 1 -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.download)
                        .setTitle("Starting downloads")
                        .setText("For ${downloads.size} videos")
                        .setBackgroundDrawable(GradientGenerator.random())
                        .setDuration(7_000)
                        .show()
            }
            else -> throw IllegalStateException()
        }
    }

    @UiThread fun displayError(activity: Activity?, error: KnownError, link: String) = displayError(activity, error, listOf(link))
    @UiThread fun displayError(activity: Activity?, error: KnownError, links: List<String>) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        when(error) {
            KnownError.MALFORMED_URL -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.toast_error)
                        .setTitle("Malformed URL")
                        .setText("Cannot process the given URL because it is in an unknown format (${links[0]})")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_800, R.color.Red_A700))
                        .setDuration(7_000)
                        .show()
            }
            KnownError.UNADDRESSABLE_VIDEO -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.toast_error)
                        .setTitle("Cannot Download Video")
                        .setText("Cannot process the given video, youtube responded with an error")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_800, R.color.Red_A700))
                        .setDuration(7_000)
                        .show()
            }
            KnownError.VIDEO_LENGTH -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.toast_error)
                        .setTitle("Video Length Error")
                        .setText("Cannot process the given video because its length exceeds 3 hours")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_500, R.color.Red_A400))
                        .setDuration(7_000)
                        .show()
            }

            KnownError.UNKNOWN_ERROR -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.toast_error)
                        .setTitle("Unknown error")
                        .setText("An exception was raised by the server while converting the selected video")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_500, R.color.Red_A400))
                        .setDuration(7_000)
                        .show()
            }

            KnownError.BATCH_FAILED -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.toast_error)
                        .setTitle("Downloading error")
                        .setText("A video in the list can not be converted for length or server issues")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_500, R.color.Red_A400))
                        .setDuration(7_000)
                        .show()
            }
        }
    }
}