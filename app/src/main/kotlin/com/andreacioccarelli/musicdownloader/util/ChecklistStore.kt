package com.andreacioccarelli.musicdownloader.util

import android.content.Context
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object ChecklistStore {
    private const val separator = "\\?a#s!\\"

    fun isEmpty(context: Context): Boolean = get(context).isEmpty()

    fun get(context: Context): List<Pair<String, String>> {
        val raw = App.prefs.get(Keys.list, "")

        if (raw == "") return emptyList()
        val list = raw.removeSuffix(separator).split(separator)
        return list.map { it to App.prefs.get(it, "") }
    }
    
    fun add(context: Context, item: String, link: String) {
        App.prefs.put(Keys.list, App.prefs.get(Keys.list, "")
                .plus("$item$separator")
        )
        App.prefs.put(item, link)
    }

    fun contains(context: Context, item: String): Boolean {

        val raw = App.prefs.get(Keys.list, "")
        return raw.contains(item)
    }

    fun remove(context: Context, item: String) {
        val raw = App.prefs.get(Keys.list, "")

        if (raw.contains(item)) {
            if (raw == "$item$separator" || raw == item) {
                App.prefs.put(Keys.list, "")
                return
            }

            when {
                raw.startsWith(item) -> App.prefs.put(Keys.list, raw.removePrefix("$item$separator"))
                raw.endsWith(item + separator) -> App.prefs.put(Keys.list, raw.removeSuffix("$item$separator"))
                else -> App.prefs.put(Keys.list, raw.replace("$separator$item", ""))
            }

            App.prefs.remove(item)
        }
    }
}