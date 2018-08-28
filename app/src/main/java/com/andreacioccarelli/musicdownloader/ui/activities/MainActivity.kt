package com.andreacioccarelli.musicdownloader.ui.activities

import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.data.requests.YoutubeRequestBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.YoutubeSearchResponse
import com.andreacioccarelli.musicdownloader.extensions.dismissKeyboard
import com.andreacioccarelli.musicdownloader.extensions.onSubmit
import com.andreacioccarelli.musicdownloader.extensions.toEditable
import com.andreacioccarelli.musicdownloader.ui.adapters.ListAdapter
import com.andreacioccarelli.musicdownloader.ui.drawables.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.ConnectionStatus
import com.andreacioccarelli.musicdownloader.util.DownloadListUtil
import com.andreacioccarelli.musicdownloader.util.NetworkUtil
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.assent.Assent
import com.afollestad.assent.AssentActivity
import com.afollestad.assent.Permission
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItems
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AssentActivity() {

    private var wasOffline = false
    private var isOffline = false
    private var arePermissionsGranted = false
    private var isSearching = false

    private val networkConnectionListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (NetworkUtil.connectionStatus == ConnectionStatus.ONLINE) {
                if (wasOffline) {
                    Alerter.create(this@MainActivity)
                            .setTitle("Here we go!")
                            .setText("Device is back online")
                            .setDuration(4_000)
                            .setIcon(R.drawable.download_success)
                            .enableSwipeToDismiss()
                            .setBackgroundDrawable(GradientGenerator.successGradient)
                            .show()

                    searchLayout.isErrorEnabled = false
                }

                isOffline = false
            } else {
                Alerter.create(this@MainActivity)
                        .setTitle("Device offline")
                        .setText("You need an active internet connection to use this app")
                        .setDuration(8_000)
                        .setIcon(R.drawable.download_error)
                        .enableSwipeToDismiss()
                        .setBackgroundDrawable(GradientGenerator.errorGradient)
                        .setOnClickListener { startActivityForResult(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0) }
                        .show()

                wasOffline = true
                isOffline = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initInput()
        initPermissions()
        initToolbar()
        initFab()
        initNetwork()
    }

    private fun initInput() {
        search.onSubmit {
            fab.performClick()
            search.dismissKeyboard()
        }
    }

    private fun initPermissions() {
        if (!Assent.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
            Assent.request(Permission.WRITE_EXTERNAL_STORAGE) {
                arePermissionsGranted = if (!it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    Alerter.create(this@MainActivity)
                            .setTitle("Cannot read external storage")
                            .setText("You have to grant the requested permission to correctly use this up")
                            .setDuration(8_000)
                            .setIcon(R.drawable.folder)
                            .setBackgroundDrawable(GradientGenerator.errorGradient)
                            .setOnClickListener { _ -> initPermissions() }
                            .show()
                    false
                } else {
                    true
                }
            }
        } else arePermissionsGranted = true
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        title = "Music Downloader"
    }


    private fun initFab() = with(fab) {
        setOnClickListener { view ->
            if (!arePermissionsGranted) {
                Assent.request(Permission.WRITE_EXTERNAL_STORAGE) {
                    if (it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                        arePermissionsGranted = true
                        fab.performClick()
                    } else {
                        val intent = Intent()
                        intent.action = ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }
                return@setOnClickListener
            }

            if (search.text.isEmpty() || search.text.isBlank()) {
                searchLayout.error = "Fill the search field"

                displayFormError()
                return@setOnClickListener
            }

            if (isOffline) {
                searchLayout.error = "Device is offline"

                displayFormError()
                return@setOnClickListener
            }


            searchLayout.isErrorEnabled = false
            search.dismissKeyboard()
            search.isEnabled = false
            isSearching = true

            val snack = Snackbar.make(view, "Searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CANCEL") {
                        isSearching = false
                        search.isEnabled = true
                        fab.show()
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.Orange_600))

            fab.hide()
            snack.show()
            VibrationUtil.medium()

            doAsync {
                val requestBuilder = YoutubeRequestBuilder.get(search.text)
                val request = OkHttpClient().newCall(requestBuilder).execute()

                val jsonRequest = request.body()!!.string()

                uiThread {
                    if (!isSearching) return@uiThread

                    val gson = Gson()
                    val response = gson.fromJson(
                            jsonRequest,
                            YoutubeSearchResponse::class.java)

                    if (response.pageInfo.totalResults == 0) {
                        empty_result.visibility = View.VISIBLE
                        songRecyclerView.visibility = View.GONE
                    } else {
                        empty_result.visibility = View.GONE

                        with(songRecyclerView) {
                            visibility = View.VISIBLE
                            adapter = ListAdapter(response, this@MainActivity, supportFragmentManager)
                            layoutManager = LinearLayoutManager(this@MainActivity)
                        }
                    }

                    fab.show()
                    snack.dismiss()
                    VibrationUtil.medium()
                    search.isEnabled = true
                }
            }
        }
    }

    private fun displayFormError() {
        fab.hide()
        VibrationUtil.strong()

        Handler().postDelayed({
            fab.show()
            searchLayout.isErrorEnabled = false
        }, 2500)
    }

    private fun initNetwork() = registerReceiver(networkConnectionListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    private fun unregisterReceivers() = unregisterReceiver(networkConnectionListener)

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_list -> {

                if (DownloadListUtil.isEmpty(this)) {
                    MaterialDialog(this)
                            .customView(R.layout.empty_checklist)
                            .show()
                } else {
                    MaterialDialog(this)
                            .title(text = "Checklist")
                            .listItems(items = DownloadListUtil.get(this), selection = { dialog, id, title ->
                                search.text = title.toEditable()
                                fab.performClick()
                                dialog.dismiss()
                            })
                            .show()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
