package com.andreacioccarelli.musicdownloader.util

import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.BuildConfig
import com.andreacioccarelli.musicdownloader.constants.Keys
import com.andreacioccarelli.musicdownloader.data.serializers.UpdateCheck
import java.io.File

/**
 * Created by andrea on 2018/Aug.
 * Part of the package com.andreacioccarelli.musicdownloader.util
 */
object UpdateUtil {

    private const val SUBFOLDER = "MusicDownloader"

    fun getNotificationTitle(check: UpdateCheck) = "MusicDownloader (${BuildConfig.VERSION_NAME} -> ${check.versionName})"
    fun getNotificationContent() = "Downloading update package...."


    fun getDestinationSubpath(check: UpdateCheck): String {
        val subPath = "$SUBFOLDER/music-downloader-${check.versionName}.apk"
        App.prefs.put(Keys.updateSubpath, subPath)

        cleanDuplicatedInstallationPackage()

        return subPath
    }

    fun cleanDuplicatedInstallationPackage() {
        File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                "${Environment.DIRECTORY_DOWNLOADS}/" + App.prefs.getString(Keys.updateSubpath, "")).delete()
    }

    private fun getPackagePath(): File = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/" +
                    "${Environment.DIRECTORY_DOWNLOADS}/" +
                    App.prefs.getString(Keys.updateSubpath, ""))

    fun hasPackageBeenDownloaded() = File("${Environment.getExternalStorageDirectory().absolutePath}/" +
            "${Environment.DIRECTORY_DOWNLOADS}/" + App.prefs.getString(Keys.updateSubpath, "")).exists()

    fun openUpdateInPackageManager() {
        val mostRecentDownload = UpdateUtil.getPackagePath()
        logd(mostRecentDownload)

        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        installIntent.setDataAndType(Uri.fromFile(mostRecentDownload),
                "application/vnd.android.package-archive")

        installIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            App.instance.applicationContext.startActivity(installIntent)
        } catch (e: RuntimeException) {
            loge("Cannot start package manager, $e")
        }
    }
}