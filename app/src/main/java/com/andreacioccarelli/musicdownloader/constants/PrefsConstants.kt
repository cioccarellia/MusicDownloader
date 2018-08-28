package com.andreacioccarelli.musicdownloader.constants

import android.os.Environment

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.constants
 */

const val KEY = "c29maWFuZHJl"
const val FILE = "settings"

val SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath!!
val DOWNLOAD_PATH = Environment.DIRECTORY_DOWNLOADS!!

val DEFAULT_PATH = "$SDCARD_PATH/$DOWNLOAD_PATH/"


object Keys {
    const val folder = "folder"
    const val list = "list"
}