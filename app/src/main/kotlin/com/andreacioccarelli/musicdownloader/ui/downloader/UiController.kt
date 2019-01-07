package com.andreacioccarelli.musicdownloader.ui.downloader

import android.app.Activity
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.ui.drawables.GradientGenerator
import com.tapadoo.alerter.Alerter

/**
 * Designed and Developed by Andrea Cioccarelli
 */

object UiController {

    fun displayProgressMetaRetrievingSingle(activity: Activity?, downloads: String) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        Alerter.create(activity)
                .enableProgress(true)
                .setIcon(R.drawable.download)
                .setTitle("Initializing conversion")
                .setText("Retrieving download information for video $downloads")
                .setBackgroundDrawable(GradientGenerator.random())
                .enableInfiniteDuration(true)
                .show()
    }

    fun displayProgressMetaRetrievingList(activity: Activity?, downloads: List<String>) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        Alerter.create(activity)
                .enableProgress(true)
                .setIcon(R.drawable.download)
                .setTitle("Initializing conversions")
                .setText("Retrieving download information for ${downloads.size} videos")
                .setBackgroundDrawable(GradientGenerator.random())
                .enableInfiniteDuration(true)
                .show()
    }

    fun displayDownloadStarted(activity: Activity?, downloads: List<DirectLinkResponse>) {
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

    fun displayError(activity: Activity?, error: KnownError, response: DirectLinkResponse, link: String) {
        if (activity == null) return
        if (activity.isDestroyed || activity.isFinishing) return

        when(error) {
            KnownError.MALFORMED_URL -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.ic_error_outline_white_48dp)
                        .setTitle("Malformed URL")
                        .setText("Cannot process the given URL because it is in an unknown format ($link)")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_800, R.color.Red_A700))
                        .setDuration(7_000)
                        .show()
            }
            KnownError.UNADDRESSABLE_VIDEO -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.ic_error_outline_white_48dp)
                        .setTitle("Cannot Download Video")
                        .setText("Cannot process the given video, youtube responded with an error")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_800, R.color.Red_A700))
                        .setDuration(7_000)
                        .show()
            }
            KnownError.VIDEO_LENGTH -> {
                Alerter.create(activity)
                        .setIcon(R.drawable.ic_error_outline_white_48dp)
                        .setTitle("Video Length Error")
                        .setText("Cannot process the given video because its length exceeds 3 hours")
                        .setBackgroundDrawable(GradientGenerator.make(0F, R.color.Red_500, R.color.Red_A400))
                        .setDuration(7_000)
                        .show()
            }
        }
    }
}