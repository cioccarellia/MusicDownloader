package com.andreacioccarelli.musicdownloader.extensions

import android.net.Uri
import android.text.Editable

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.extensions
 */

fun String.toUri(): Uri = Uri.parse(this)

val String.isUrl: Boolean
    get() = this.contains("http://") || this.contains("https://") || this.contains("youtu.be") || this.contains("youtube.com")

fun String.toEditable() = Editable.Factory.getInstance().newEditable(this)!!

fun String.sanitize() = replace("\"", "")
        .replace("\"", "")
        .replace("\\/", "/")

fun String.renameIfEqual(pattern: String, renaming: String) {
    if (this == pattern) replace(this, renaming)
}