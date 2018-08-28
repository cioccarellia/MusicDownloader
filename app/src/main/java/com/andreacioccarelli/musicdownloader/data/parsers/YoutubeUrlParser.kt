package com.andreacioccarelli.musicdownloader.data.parsers

import com.andreacioccarelli.musicdownloader.constants.AUTH_KEY
import com.andreacioccarelli.musicdownloader.constants.QUERY_STRING_BEGIN
import com.andreacioccarelli.musicdownloader.constants.WEB_URL
import com.andreacioccarelli.musicdownloader.extensions.isUrl

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.parser
 */
object YoutubeUrlParser {
    fun parse(search: String): String = WEB_URL + QUERY_STRING_BEGIN
            .plus("part=snippet")
            .plus("&maxResults=${if (search.isUrl) "1" else "30"}")
            .plus("&q=$search")
            .plus("&safeSearch=none")
            .plus("&type=video")
            .plus("&key=$AUTH_KEY")
}