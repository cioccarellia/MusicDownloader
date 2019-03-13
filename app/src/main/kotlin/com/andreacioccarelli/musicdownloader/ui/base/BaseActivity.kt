package com.andreacioccarelli.musicdownloader.ui.base

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.ClipboardManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.extensions.isUrl
import com.andreacioccarelli.musicdownloader.extensions.toEditable
import com.andreacioccarelli.musicdownloader.ui.gradients.GradientGenerator
import com.andreacioccarelli.musicdownloader.ui.update.AppUpdateChecker
import com.andreacioccarelli.musicdownloader.util.ConnectionStatus
import com.andreacioccarelli.musicdownloader.util.NetworkUtil
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_content.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    internal var areAllPermissionsGranted = false
    private var wasOffline = false
    var isOffline = false

    private val networkConnectionListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (NetworkUtil.connectionStatus == ConnectionStatus.ONLINE) {
                if (wasOffline) {
                    Alerter.create(this@BaseActivity)
                            .setTitle("Connection detected")
                            .setText("Device is back online")
                            .setDuration(4_000)
                            .setIcon(R.drawable.access_point_network)
                            .enableSwipeToDismiss()
                            .setBackgroundDrawable(GradientGenerator.successGradient)
                            .show()

                    searchLayout.isErrorEnabled = false
                }

                isOffline = false
            } else {
                Alerter.create(this@BaseActivity)
                        .setTitle("Device is offline")
                        .setText("You need an active internet connection to use this app")
                        .setDuration(7_000)
                        .setIcon(R.drawable.access_point_network_off)
                        .enableSwipeToDismiss()
                        .setBackgroundDrawable(GradientGenerator.errorGradient)
                        .setOnClickListener(
                                View.OnClickListener { startActivityForResult(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0) })
                        .show()

                wasOffline = true
                isOffline = true
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initNetwork()
        initClipboard()
        initPermissions()
        initUpdateChecker()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }


    private fun initClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.text

        text?.let {
            if (it.isUrl && intent?.getStringExtra(Intent.EXTRA_TEXT) == null) {
                search.text = clipboard.text.toEditable()
            }
        }
    }

    private fun initPermissions() {
        if (!isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
            askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) {
                areAllPermissionsGranted = if (!it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    Alerter.create(this@BaseActivity)
                            .setTitle("Cannot read external storage")
                            .setText("You have to grant the requested permission to correctly use this app")
                            .setDuration(5_000)
                            .setIcon(R.drawable.folder)
                            .setBackgroundDrawable(GradientGenerator.errorGradient)
                            .setOnClickListener( View.OnClickListener { initPermissions() })
                            .show()
                    false
                } else {
                    Alerter.clearCurrent(this)
                    true
                }
            }
        } else areAllPermissionsGranted = true
    }


    private fun initUpdateChecker() = AppUpdateChecker.checkForUpdates(this)
    private fun initNetwork() = registerReceiver(networkConnectionListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    private fun unregisterReceivers() = try {
        unregisterReceiver(networkConnectionListener)
    } catch (ignored: RuntimeException) {}

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }
}