package com.andreacioccarelli.musicdownloader.extensions

import android.net.Uri
import android.text.SpannableStringBuilder
import com.andreacioccarelli.logkit.logd

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun String.toUri(): Uri = Uri.parse(this)

val CharSequence.isUrl: Boolean
    get() = this.startsWith("http://") || this.startsWith("https://") || this.contains("youtu.be") || this.contains("youtube.com")

fun String.sanitize() = replace("\"", "")
        .replace("\"", "")
        .replace("\\/", "/")

fun String.renameIfEqual(pattern: String, renaming: String): String {
    if (this == pattern) replace(this, renaming)
    return this
}

fun CharSequence.toEditable() = SpannableStringBuilder(this)

fun String.getVideoId(): String {
    if (this.contains("?v=")) return split("?v=")[1].split("&")[0]

    logd("Trying to parse string $this to linkId, but unsuccessful. Can't find ?v=")
    return ""
}

fun String.getVideoIdOrThrow(): String {
    if (this.contains("?v=")) return split("?v=")[1].split("&")[0]

    throw IllegalStateException()
}

fun String.correctSpecialChars(): String = this
        .replace("/", "")
        .removeSuffix(".mp4")
        .removeSuffix(".mp3")
        .renameIfEqual(".", "_.")
        .renameIfEqual("..", "__.")
        .removePrefix(".")