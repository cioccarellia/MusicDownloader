package com.andreacioccarelli.musicdownloader.data.requests

import com.andreacioccarelli.musicdownloader.data.parsers.YoutubeUrlParser
import okhttp3.Request

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object YoutubeRequestBuilder {
    fun get(search: CharSequence): Request = Request.Builder()
            .url(YoutubeUrlParser.parse(search.toString()))
            .build()
}