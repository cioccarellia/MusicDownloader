package com.andreacioccarelli.musicdownloader.util

import com.andreacioccarelli.musicdownloader.constants.FILE
import com.andreacioccarelli.musicdownloader.constants.KEY
import com.andreacioccarelli.musicdownloader.constants.Keys
import android.content.Context
import com.andreacioccarelli.cryptoprefs.CryptoPrefs

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.util
 *
 */


object DownloadListUtil {
    private const val separator = "\\?a#s!\\"

    fun isEmpty(context: Context): Boolean = get(context).isEmpty()

    fun get(context: Context): List<String> {
        val prefs = CryptoPrefs(context, FILE, KEY)
        val raw = prefs.getString(Keys.list, "")

        if (raw == "") return emptyList()
        return raw.removeSuffix(separator).split(separator)
    }
    
    fun add(context: Context, item: String) {
        val prefs = CryptoPrefs(context, FILE, KEY)
        prefs.put(Keys.list, prefs.getString(Keys.list, "")
                .plus("$item$separator")
        )
    }

    fun contains(context: Context, item: String): Boolean {
        val prefs = CryptoPrefs(context, FILE, KEY)

        val raw = prefs.getString(Keys.list, "")
        return raw.contains(item)
    }

    fun remove(context: Context, item: String) {
        val prefs = CryptoPrefs(context, FILE, KEY)

        val raw = prefs.getString(Keys.list, "")
        if (raw.contains(item)) {
            if (raw == "$item$separator" || raw == item) {
                prefs.put(Keys.list, "")
                return
            }
            when {
                raw.startsWith(item) -> prefs.put(Keys.list, raw.removePrefix("$item$separator"))
                raw.endsWith(item + separator) -> prefs.put(Keys.list, raw.removeSuffix("$item$separator"))
                else -> prefs.put(Keys.list, raw.replace("$separator$item", ""))
            }
        }
    }
}