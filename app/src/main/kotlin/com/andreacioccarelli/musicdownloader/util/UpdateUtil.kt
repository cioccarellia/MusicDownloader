package com.andreacioccarelli.musicdownloader.util

import android.R.attr.path
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.musicdownloader.App.Companion.prefs
import com.andreacioccarelli.musicdownloader.BuildConfig
import com.andreacioccarelli.musicdownloader.constants.Keys
import com.andreacioccarelli.musicdownloader.data.serializers.UpdateCheck
import java.io.File

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object UpdateUtil {

    private const val APK_MIME = "application/vnd.android.package-archive"

    fun getNotificationTitle(check: UpdateCheck) = "${BuildConfig.VERSION_NAME} -> ${check.versionName}"
    fun getNotificationContent() = "Downloading update package"

    fun getDestinationSubpath(check: UpdateCheck): String {
        val subPath = Environment.DIRECTORY_DOWNLOADS + "/music-downloader-${check.versionName}.apk"

        clearDuplicatedInstallationPackage(subPath)

        return subPath
    }

    fun clearDuplicatedInstallationPackage(name: String ) {
        File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                "${Environment.DIRECTORY_DOWNLOADS}/" + name).delete()
    }

    private fun getPackagePath(): File = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/" +
                    "${Environment.DIRECTORY_DOWNLOADS}/")

    fun hasPackageBeenDownloaded(newVersionName: String): Boolean {
        val file = File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                "${Environment.DIRECTORY_DOWNLOADS}/music-downloader-$newVersionName.apk")

        return file.exists()
    }

    fun openUpdateInPackageManager(context: Context) {
        val cachedUpdatePackage = UpdateUtil.getPackagePath()

        logd(cachedUpdatePackage)

        if (Build.VERSION.SDK_INT >= 24) {
            val uri = FileProvider.getUriForFile(context, "com.andreacioccarelli.musicdownloader.updater",
                    File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                            "${Environment.DIRECTORY_DOWNLOADS}/music-downloader-${prefs.get(Keys.lastVersionName, "")}.apk"))

            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)


            intent.flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK


            val pm = context.packageManager
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.parse("file://$path"), APK_MIME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }
}