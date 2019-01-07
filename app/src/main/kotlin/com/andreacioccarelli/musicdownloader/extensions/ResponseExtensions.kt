package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.ui.downloader.KnownError

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun DirectLinkResponse.isSuccessful() = state == RESPONSE_OK
fun DirectLinkResponse.isProcessing() = state == RESPONSE_PROCESSING || state == RESPONSE_WAIT
fun DirectLinkResponse.isUnsuccessful() = state == RESPONSE_ERROR

val DirectLinkResponse.error: KnownError?
    get() {
        if (!isUnsuccessful()) return null

        return when (reason) {
            ERROR_LENGTH -> KnownError.VIDEO_LENGTH
            ERROR_MALFORMED -> KnownError.MALFORMED_URL
            ERROR_UNADDRESSABLE_VIDEO -> KnownError.UNADDRESSABLE_VIDEO
            else -> KnownError.UNKNOWN_ERROR
        }
    }