package com.andreacioccarelli.musicdownloader.data.requests

import com.andreacioccarelli.musicdownloader.data.parsers.YoutubeUrlParser
import okhttp3.Request

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data
 */
object YoutubeRequestBuilder {
    fun get(search: CharSequence): Request = Request.Builder()
            .url(YoutubeUrlParser.parse(search.toString()))
            .build()
}