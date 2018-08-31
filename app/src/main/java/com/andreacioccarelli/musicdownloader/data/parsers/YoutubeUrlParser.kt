package com.andreacioccarelli.musicdownloader.data.parsers

import android.util.Base64
import com.andreacioccarelli.musicdownloader.constants.AUTH_KEY
import com.andreacioccarelli.musicdownloader.constants.YOUTUBE_API_SEARCH_URL
import com.andreacioccarelli.musicdownloader.extensions.isUrl

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.parser
 */
object YoutubeUrlParser {
    fun parse(search: String): String = YOUTUBE_API_SEARCH_URL + "?"
            .plus("part=snippet")
            .plus("&maxResults=${if (search.isUrl) 1 else 50}")
            .plus("&safeSearch=none")
            .plus("&type=video")
            .plus("&key=${Base64.decode(AUTH_KEY, Base64.DEFAULT).toString(Charsets.UTF_8)}")
            .plus("&q=$search")
}