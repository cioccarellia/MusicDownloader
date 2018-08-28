package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.tapadoo.alerter.Alerter

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.extensions
 */

fun Alerter.updateState(response: DirectLinkResponse): Alerter {
    when (response.state) {
        "wait" -> when (response.reason) {
            "checking video info" -> setText("Checking video info...")
            else -> setText("Waiting server to process file...")
        }
        "processing" -> setText("Converting video...")
        "ok" -> setText("Downloading file...")
        else -> response.state
    }

    return this
}