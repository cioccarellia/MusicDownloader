package com.andreacioccarelli.musicdownloader

import android.app.Application
import com.andreacioccarelli.musicdownloader.extensions.Delegates

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader
 */
class App : Application() {

    companion object {
        var instance by Delegates.singleValue<Application>()
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }

}