package com.andreacioccarelli.musicdownloader.util

import android.content.Context
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 * Designed and developed by Andrea Cioccarelli
 */
object QueueStore {

    private const val separator = "\\?s#a!\\"

    fun isEmpty(context: Context): Boolean = get(context).isEmpty()

    fun get(context: Context): List<Triple<String, String, Boolean>> {
        val raw = App.prefs.get(Keys.queue, "")

        if (raw == "") return emptyList()
        val list = raw.removeSuffix(separator).split(separator)
        return list.map { Triple(it, App.prefs.get("link->$it", ""), App.prefs.get("isChecked->$it", false))  }
    }

    fun add(context: Context, item: String, link: String, isChecked: Boolean) {
        App.prefs.put(Keys.queue, App.prefs.get(Keys.queue, "")
                .plus("$item$separator")
        )

        App.prefs.put("link->$item", link)
        App.prefs.put("isChecked->$item", isChecked)
    }

    fun setChecked(key: String, state: Boolean) {
        App.prefs.put("isChecked->$key", state)
    }

    fun contains(context: Context, item: String): Boolean {
        val raw = App.prefs.get(Keys.queue, "")
        return raw.contains(item)
    }

    fun remove(context: Context, item: String) {
        val raw = App.prefs.get(Keys.queue, "")

        if (raw.contains(item)) {
            if (raw == "$item$separator" || raw == item) {
                App.prefs.put(Keys.queue, "")
                return
            }

            when {
                raw.startsWith(item) -> App.prefs.put(Keys.queue, raw.removePrefix("$item$separator"))
                raw.endsWith(item + separator) -> App.prefs.put(Keys.queue, raw.removeSuffix("$item$separator"))
                else -> App.prefs.put(Keys.queue, raw.replace("$separator$item", ""))
            }

            App.prefs.remove(item)
        }
    }
}