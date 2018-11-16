package com.andreacioccarelli.musicdownloader.util

import android.content.Context
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.musicdownloader.constants.FILE
import com.andreacioccarelli.musicdownloader.constants.KEY
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object ChecklistUtil {
    private const val separator = "\\?a#s!\\"

    fun isEmpty(context: Context): Boolean = get(context).isEmpty()

    fun get(context: Context): List<Pair<String, String>> {
        val prefs = CryptoPrefs(context, FILE, KEY)
        val raw = prefs.get(Keys.list, "")

        if (raw == "") return emptyList()
        val list = raw.removeSuffix(separator).split(separator)
        return list.map { it to prefs.get(it, "") }
    }
    
    fun add(context: Context, item: String, link: String) {
        val prefs = CryptoPrefs(context, FILE, KEY)
        prefs.put(Keys.list, prefs.get(Keys.list, "")
                .plus("$item$separator")
        )
        prefs.put(item, link)
    }

    fun contains(context: Context, item: String): Boolean {
        val prefs = CryptoPrefs(context, FILE, KEY)

        val raw = prefs.get(Keys.list, "")
        return raw.contains(item)
    }

    fun remove(context: Context, item: String) {
        val prefs = CryptoPrefs(context, FILE, KEY)
        val raw = prefs.get(Keys.list, "")

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

            prefs.remove(item)
        }
    }
}