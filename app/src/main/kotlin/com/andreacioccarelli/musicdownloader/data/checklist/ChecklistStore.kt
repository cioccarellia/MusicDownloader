package com.andreacioccarelli.musicdownloader.data.checklist

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object ChecklistStore {
    private const val separator = "\\?a#s!\\"

    fun isEmpty(): Boolean = get().isEmpty()
    fun clear() = App.prefs.put(Keys.list, "")

    fun get(): List<ChecklistEntry> {
        val raw = App.prefs.get(Keys.list, "")

        if (raw == "") return emptyList()
        val list = raw.removeSuffix(separator).split(separator)
        return list.map { ChecklistEntry(it, App.prefs.get("link->$it", ""), App.prefs.get("thumb->$it", "")) }
    }

    fun add(checklistEntry: ChecklistEntry) = add(checklistEntry.link, checklistEntry.link, checklistEntry.thumbnailLink)
    fun add(item: String, link: String, thumb: String) {
        App.prefs.put(Keys.list, App.prefs.get(Keys.list, "")
                .plus("$item$separator")
        )

        App.prefs.put("thumb->$item", thumb)
        App.prefs.put("link->$item", link)
    }

    fun contains(link: String): Boolean {
        val raw = App.prefs.allPrefsList
        return raw.map { it.second }.contains(link)
    }

    fun remove(checklistEntry: ChecklistEntry) = remove(checklistEntry.link)
    fun remove(item: String) {
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