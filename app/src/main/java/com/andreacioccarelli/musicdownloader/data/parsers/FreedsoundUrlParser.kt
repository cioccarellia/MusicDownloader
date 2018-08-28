package com.andreacioccarelli.musicdownloader.data.parsers

import com.andreacioccarelli.musicdownloader.constants.DIRECT_LINK_GENERATOR_URL
import com.andreacioccarelli.musicdownloader.constants.QUERY_STRING_BEGIN
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.formats.Format.MP3
import com.andreacioccarelli.musicdownloader.data.formats.Format.MP4

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.parsers
 */
object FreedsoundUrlParser {
    fun parse(id: String, format: Format): String = DIRECT_LINK_GENERATOR_URL + QUERY_STRING_BEGIN
            .plus("v=$id")
            .plus("&f=${when(format) {
                MP3 -> "mp3"
                MP4 -> "mp4"
            }}")

}