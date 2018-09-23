package com.andreacioccarelli.musicdownloader.util

import android.R.attr.path
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
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
    private const val APK_MIME = "application/vnd.android.package-archive"

    fun getNotificationTitle(check: UpdateCheck) = "MusicDownloader (${BuildConfig.VERSION_NAME} -> ${check.versionName})"
    fun getNotificationContent() = "Downloading update package"


    fun getDestinationSubpath(check: UpdateCheck): String {
        val subPath = "$SUBFOLDER/music-downloader-${check.versionName}.apk"
        App.prefs.put(Keys.updateSubpath, subPath)

        clearDuplicatedInstallationPackage()

        return subPath
    }

    fun clearDuplicatedInstallationPackage() {
        File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                "${Environment.DIRECTORY_DOWNLOADS}/" + App.prefs.getString(Keys.updateSubpath, "")).delete()
    }

    private fun getPackagePath(): File = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/" +
                    "${Environment.DIRECTORY_DOWNLOADS}/" +
                    App.prefs.getString(Keys.updateSubpath, ""))

    fun hasPackageBeenDownloaded(newVersionName: String): Boolean {
        val file = File("${Environment.getExternalStorageDirectory().absolutePath}/" +
                "${Environment.DIRECTORY_DOWNLOADS}/$SUBFOLDER/music-downloader-$newVersionName.apk")

        return file.exists()
    }

    fun openUpdateInPackageManager(context: Context) {
        val cachedUpdatePackage = UpdateUtil.getPackagePath()

        val uriForFile = FileProvider.getUriForFile(context, "${context.packageName}.provider", cachedUpdatePackage)

        if (Build.VERSION.SDK_INT >= 24) {
            val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uriForFile, APK_MIME)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_ACTIVITY_NEW_TASK

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