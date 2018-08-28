package com.andreacioccarelli.musicdownloader.data.requests

import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.parsers.FreedsoundUrlParser
import okhttp3.Request

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.generator
 */

object DownloadLinkRequestsBuilder {
    fun get(id: String, format: Format): Request = Request.Builder()
            .url(FreedsoundUrlParser.parse(id, format))
            .header(HEADER_ACCEPT, "*/*")
            .header(HEADER_DNT, "1")
            .header(HEADER_ORIGIN, BASE_URL)
            .header(HEADER_REFERER, BASE_URL)
            .header(HEADER_USER, USER_AGENT)
            .build()
}