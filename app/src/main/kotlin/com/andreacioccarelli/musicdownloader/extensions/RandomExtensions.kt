package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun onceFor3(code: () -> Unit) {
    val value = App.prefs.get(Keys.oneOf3, 2)
    if (value == 2) {
        App.prefs.put(Keys.oneOf3, 0)
        code()
    } else App.prefs.put(Keys.oneOf3, value + 1)
}