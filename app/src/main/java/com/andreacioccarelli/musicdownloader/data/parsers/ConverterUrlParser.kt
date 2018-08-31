package com.andreacioccarelli.musicdownloader.data.parsers

import android.util.Base64
import com.andreacioccarelli.musicdownloader.constants.DIRECT_LINK_GENERATOR_URL
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.formats.Format.MP3
import com.andreacioccarelli.musicdownloader.data.formats.Format.MP4

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.parsers
 */
object ConverterUrlParser {
    fun parse(id: String, format: Format): String = Base64.decode(DIRECT_LINK_GENERATOR_URL, Base64.DEFAULT)
            .toString(Charsets.UTF_8)+ "?"
            .plus("v=$id")
            .plus("&f=${when(format) {
                MP3 -> "mp3"
                MP4 -> "mp4"
            }}")

}