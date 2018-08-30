package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 * Created by andrea on 2018/Aug.
 * Part of the package com.andreacioccarelli.musicdownloader.extensions
 */

fun onceOutOf2(code: () -> Unit) {
    val value = App.prefs.getInt(Keys.oneOf2, 1)
    if (value == 1) {
        App.prefs.put(Keys.oneOf2, 0)
        code()
    } else App.prefs.put(Keys.oneOf2, value + 1)
}

fun onceOutOf3(code: () -> Unit) {
    val value = App.prefs.getInt(Keys.oneOf3, 2)
    if (value == 2) {
        App.prefs.put(Keys.oneOf3, 0)
        code()
    } else App.prefs.put(Keys.oneOf3, value + 1)
}

fun onceOutOf4(code: () -> Unit) {
    val value = App.prefs.getInt(Keys.oneOf4, 3)
    if (value == 3) {
        App.prefs.put(Keys.oneOf4, 0)
        code()
    } else App.prefs.put(Keys.oneOf4, value + 1)
}