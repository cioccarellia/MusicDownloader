package com.andreacioccarelli.musicdownloader.extensions

import android.net.Uri
import android.text.SpannableStringBuilder
import com.andreacioccarelli.logkit.logd

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun String.toUri(): Uri = Uri.parse(this)
fun String.toYoutubeUrl(): String = "https://www.youtube.com/watch?v=$this"

val CharSequence.isUrl: Boolean
    get() = this.startsWith("http://") || this.startsWith("https://") || this.contains("youtu.be") || this.contains("youtube.com")

fun String.sanitizeUrl() = replace("\"", "")
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
    if (this.contains("?v="))
        return split("?v=")[1].split("&")[0]
    else throw IllegalStateException()
}

fun String.escapeHtml(): String {

    var str = this
    str = str.replace("/", "")
    str = str.removeSuffix(".mp4")
    str = str.removeSuffix(".mp3")

        // Prefixes
    str = str.renameIfEqual(".", "_.")
    str = str.renameIfEqual("..", "__.")
    str = str.removePrefix(".")

    // Accents
    val replaceList = mapOf(
            "&#192;" to "À", "&#193;" to "Á", "&#194;" to "Â", "&#195;" to "Ã",
            "&#196;" to "Ä", "&#197;" to "Å", "&#198;" to "Æ", "&#199;" to "Ç",
            "&#200;" to "È", "&#201;" to "É", "&#202;" to "Ê", "&#203;" to "Ë",
            "&#204;" to "Ì", "&#205;" to "Í", "&#206;" to "Î", "&#207;" to "Ï",
            "&#208;" to "Ð", "&#209;" to "Ñ", "&#210;" to "Ò", "&#211;" to "Ó",
            "&#212;" to "Ô", "&#213;" to "Õ", "&#214;" to "Ö", "&#216;" to "Ø",
            "&#217;" to "Ù", "&#218;" to "Ú", "&#219;" to "Û", "&#210;" to "Ü",


            "&#224;" to "à", "&#225;" to "á", "&#226;" to "â", "&#227;" to "ã",
            "&#228;" to "ä", "&#229;" to "å", "&#230;" to "æ", "&#231;" to "ç",
            "&#232;" to "è", "&#233;" to "é", "&#234;" to "ê", "&#235;" to "ë",
            "&#236;" to "ì", "&#237;" to "í", "&#238;" to "î", "&#239;" to "ï",
            "&#240;" to "ð", "&#241;" to "ñ", "&#242;" to "ò", "&#243;" to "ó",
            "&#244;" to "ô", "&#245;" to "õ", "&#246;" to "ö", "&#248;" to "ø",
            "&#249;" to "ù", "&#250;" to "ú", "&#251;" to "û", "&#252;" to "ü",
            "&#253;" to "ý", "&#254;" to "þ", "&#180;" to "´", "&#39;" to "'"
    )

    replaceList.forEach {
        str = str.replace(it.key, it.value)
    }

    return str
}
