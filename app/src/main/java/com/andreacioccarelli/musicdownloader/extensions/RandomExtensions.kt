package com.andreacioccarelli.musicdownloader.extensions

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.constants.Keys

/**
 * Created by andrea on 2018/Aug.
 * Part of the package com.andreacioccarelli.musicdownloader.extensions
 */

fun onceOutOf4(code: () -> Unit) {
    val value = App.prefs.get(Keys.oneOf4, 3)
    if (value == 3) {
        App.prefs.put(Keys.oneOf4, 0)
        code()
    } else App.prefs.put(Keys.oneOf4, value + 1)
}