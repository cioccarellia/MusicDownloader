package com.andreacioccarelli.musicdownloader.ui.update

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.BuildConfig
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.constants.APK_URL
import com.andreacioccarelli.musicdownloader.constants.Keys
import com.andreacioccarelli.musicdownloader.data.requests.UpdateRequestBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.UpdateCheck
import com.andreacioccarelli.musicdownloader.extensions.onceFor4
import com.andreacioccarelli.musicdownloader.ui.gradients.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.UpdateUtil
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.*
import okhttp3.OkHttpClient

/**
 * Designed and Developed by Andrea Cioccarelli
 */

object AppUpdateChecker {

    private val onPackageDownloadCompleated: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == -1L) return

            try {
                Alerter.hide()
            } catch (ignored: Exception) {}

            GlobalScope.launch(Dispatchers.IO) {
                delay(107)
                UpdateUtil.getDownloadedPackageFile(BuildConfig.VERSION_NAME).delete()
                UpdateUtil.openUpdateInPackageManager(context)
            }
        }
    }

    fun checkForUpdates(activity: Activity) = onceFor4 {
        GlobalScope.launch(Dispatchers.IO) {
            val requestBuilder = UpdateRequestBuilder.get()
            val request = OkHttpClient().newCall(requestBuilder).execute()

            val jsonRequest = request.body()!!.string()

            val gson = Gson()
            val updateCheck = gson.fromJson(
                    jsonRequest,
                    UpdateCheck::class.java)

            App.prefs.put(Keys.lastVersionName, updateCheck.versionName)

            if (updateCheck.versionCode > BuildConfig.VERSION_CODE && !App.prefs.get(Keys.ignoring + updateCheck.versionCode, false)) {
                withContext(Dispatchers.Main) {
                    MaterialDialog(activity)
                            .title(text = "Update ${updateCheck.versionName} found!")
                            .message(text = updateCheck.changelog)
                            .positiveButton(text = if (UpdateUtil.getDownloadedPackageFile(updateCheck.versionName).exists())
                                "INSTALL UPDATE" else "DOWNLOAD UPDATE") { dialog ->
                                if (UpdateUtil.getDownloadedPackageFile(updateCheck.versionName).exists()) {
                                    UpdateUtil.openUpdateInPackageManager(activity)
                                    dialog.dismiss()
                                } else {
                                    val uri = Uri.parse(
                                            if (updateCheck.downloadInfo.useBundledUpdateLink)
                                                APK_URL
                                            else
                                                updateCheck.downloadInfo.updateLink!!
                                    )
                                    val downloadRequest = DownloadManager.Request(uri)

                                    with(downloadRequest) {
                                        setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                        setAllowedOverRoaming(true)
                                        setVisibleInDownloadsUi(true)
                                        setAllowedOverMetered(true)
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                        setTitle(UpdateUtil.getNotificationTitle(updateCheck))
                                        setDescription(UpdateUtil.getNotificationContent())
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                                        setDestinationInExternalPublicDir("", UpdateUtil.getDestinationSubpath(updateCheck))
                                    }

                                    val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(downloadRequest)

                                    activity.registerReceiver(onPackageDownloadCompleated, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                                    dialog.dismiss()

                                    Alerter.create(activity)
                                            .setTitle(UpdateUtil.getNotificationContent())
                                            .setText(UpdateUtil.getNotificationTitle(updateCheck))
                                            .setBackgroundDrawable(GradientGenerator.successGradient)
                                            .setIcon(R.drawable.download)
                                            .setDuration(9_000)
                                            .setDismissable(false)
                                            .show()
                                }
                            }
                            .negativeButton(text = "NO") { dialog ->
                                if (dialog.isCheckPromptChecked() && UpdateUtil.getDownloadedPackageFile(updateCheck.versionName).exists()) {
                                    UpdateUtil.clearDuplicatedInstallationPackage("music-downloader-${updateCheck.versionName}.apk")
                                }
                                dialog.dismiss()
                            }
                            .checkBoxPrompt(text = "Ignore this update", isCheckedDefault = false) { state ->
                                App.prefs.put(Keys.ignoring + updateCheck.versionCode, state)
                            }
                            .noAutoDismiss()
                            .show()
                }
            }
        }
    }

}