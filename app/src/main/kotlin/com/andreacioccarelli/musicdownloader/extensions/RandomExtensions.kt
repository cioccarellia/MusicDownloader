package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 *  Designed and developed by Andrea Cioccarelli
 */

fun onceFor4(code: () -> Unit) {
    val value = App.prefs.get(Keys.oneOf3, 3)
    if (value == 3) {
        App.prefs.put(Keys.oneOf3, 0)
        code()
    } else App.prefs.put(Keys.oneOf3, value + 1)
}