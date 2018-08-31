package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.constants.STATE_CHECKING
import com.andreacioccarelli.musicdownloader.constants.STATE_OK
import com.andreacioccarelli.musicdownloader.constants.STATE_PROCESSING
import com.andreacioccarelli.musicdownloader.constants.STATE_WAIT
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.tapadoo.alerter.Alerter

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.extensions
 */

fun Alerter.updateState(response: DirectLinkResponse): Alerter {
    when (response.state) {
        STATE_WAIT -> when (response.reason) {
            STATE_CHECKING -> setText("Checking video info...")
            else -> setText("Waiting server to process file...")
        }
        STATE_PROCESSING -> setText("Converting video...")
        STATE_OK -> setText("Downloading file...")
        else -> response.state
    }

    return this
}