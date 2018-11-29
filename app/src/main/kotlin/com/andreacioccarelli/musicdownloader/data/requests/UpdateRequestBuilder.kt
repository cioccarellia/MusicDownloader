package com.andreacioccarelli.musicdownloader.data.requests

import com.andreacioccarelli.musicdownloader.constants.UPDATE_URL
import okhttp3.Request

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object UpdateRequestBuilder {
    fun get(): Request = Request.Builder()
            .url(UPDATE_URL)
            .build()
}