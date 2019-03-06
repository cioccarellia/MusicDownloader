package com.andreacioccarelli.musicdownloader

import android.app.Application
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.musicdownloader.constants.FILE
import com.andreacioccarelli.musicdownloader.constants.KEY
import com.andreacioccarelli.musicdownloader.extensions.Delegates

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class App : Application() {

    companion object {
        var context by Delegates.singleValue<Application>()
        val prefs by lazy { CryptoPrefs(context.applicationContext, FILE, KEY, false) }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}