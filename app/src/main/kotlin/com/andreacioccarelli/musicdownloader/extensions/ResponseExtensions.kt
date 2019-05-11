@file:Suppress("unused")

package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.enums.FailedConversionError
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse

/**
 * Designed and Developed by Andrea Cioccarelli
 */

fun DirectLinkResponse.isSuccessful() = state == RESPONSE_OK
fun DirectLinkResponse.isProcessing() = state == RESPONSE_PROCESSING || state == RESPONSE_WAIT
fun DirectLinkResponse.isUnsuccessful() = state == RESPONSE_ERROR

fun DirectLinkResponse.getConversionError(): FailedConversionError {
    if (!isUnsuccessful()) return FailedConversionError.NO_ERROR

    return when (reason) {
        ERROR_LENGTH -> FailedConversionError.VIDEO_LENGTH
        ERROR_MALFORMED -> FailedConversionError.MALFORMED_URL
        ERROR_UNADDRESSABLE_VIDEO -> FailedConversionError.UNADDRESSABLE_VIDEO
        else -> FailedConversionError.UNKNOWN_ERROR
    }
}