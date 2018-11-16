package com.andreacioccarelli.musicdownloader.extensions

import android.net.Uri

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun String.toUri(): Uri = Uri.parse(this)

val String.isUrl: Boolean
    get() = this.contains("http://") || this.contains("https://") || this.contains("youtu.be") || this.contains("youtube.com")

fun String.sanitize() = replace("\"", "")
        .replace("\"", "")
        .replace("\\/", "/")

fun String.renameIfEqual(pattern: String, renaming: String): String {
    if (this == pattern) replace(this, renaming)
    return this
}