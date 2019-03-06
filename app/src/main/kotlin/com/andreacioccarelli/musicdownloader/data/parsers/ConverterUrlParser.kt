package com.andreacioccarelli.musicdownloader.data.parsers

import android.util.Base64
import com.andreacioccarelli.musicdownloader.constants.DIRECT_LINK_GENERATOR_URL
import com.andreacioccarelli.musicdownloader.data.model.Format
import com.andreacioccarelli.musicdownloader.data.model.Format.MP3
import com.andreacioccarelli.musicdownloader.data.model.Format.MP4

/**
 *  Designed and developed by Andrea Cioccarelli
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