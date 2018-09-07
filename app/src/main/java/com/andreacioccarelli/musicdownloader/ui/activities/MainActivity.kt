package com.andreacioccarelli.musicdownloader.ui.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.customListAdapter
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.logkit.logw
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.BuildConfig
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.constants.APK_URL
import com.andreacioccarelli.musicdownloader.constants.Keys
import com.andreacioccarelli.musicdownloader.data.requests.UpdateRequestBuilder
import com.andreacioccarelli.musicdownloader.data.requests.YoutubeRequestBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.UpdateCheck
import com.andreacioccarelli.musicdownloader.data.serializers.YoutubeSearchResponse
import com.andreacioccarelli.musicdownloader.extensions.dismissKeyboard
import com.andreacioccarelli.musicdownloader.extensions.onSubmit
import com.andreacioccarelli.musicdownloader.extensions.onceOutOf3
import com.andreacioccarelli.musicdownloader.ui.adapters.ChecklistAdapter
import com.andreacioccarelli.musicdownloader.ui.adapters.ResultsAdapter
import com.andreacioccarelli.musicdownloader.ui.drawables.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.*
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException


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
                        .setDuration(7_000)
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
        initUpdateChecker()
    }

    private fun initInput() = with(search) {
        onSubmit {
            fab.performClick()
            search.dismissKeyboard()
        }

        setOnClickListener {
            if (isSearching) {
                isSearching = false
                snack?.dismiss()
                fab.show()
            }

            if (isInError) {
                fab.show()
            }
        }
    }

    private fun initPermissions() {
        if (!Assent.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
            Assent.request(Permission.WRITE_EXTERNAL_STORAGE) {
                arePermissionsGranted = if (!it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                    Alerter.create(this@MainActivity)
                            .setTitle("Cannot read external storage")
                            .setText("You have to grant the requested permission to correctly use this up")
                            .setDuration(5_000)
                            .setIcon(R.drawable.folder)
                            .setBackgroundDrawable(GradientGenerator.errorGradient)
                            .setOnClickListener { _ -> initPermissions() }
                            .show()
                    false
                } else {
                    Alerter.clearCurrent(this)
                    true
                }
            }
        } else arePermissionsGranted = true
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        title = "Music Downloader"
    }

    private var snack: Snackbar? = null
    private val searches = mutableListOf<Int>()
    private var searchId = 0

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
            resultsRecyclerView.smoothScrollToPosition(0)

            searches.add(searchId++)
            isSearching = true

            snack = Snackbar.make(view, "Searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CANCEL") {
                        isSearching = false
                        search.isEnabled = true
                        fab.show()
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.Orange_600))

            fab.hide()
            snack!!.show()

            doAsync {
                try {
                    val requestBuilder = YoutubeRequestBuilder.get(search.text)
                    val request = OkHttpClient().newCall(requestBuilder).execute()

                    val jsonRequest = request.body()!!.string()
                    logw(jsonRequest)

                    uiThread {
                        if (!isSearching || searches.contains(searchId)) return@uiThread

                        val gson = Gson()
                        val response = gson.fromJson(
                                jsonRequest,
                                YoutubeSearchResponse::class.java)

                        if (response.pageInfo.totalResults == 0) {
                            empty_result.visibility = View.VISIBLE
                            resultsRecyclerView.visibility = View.GONE
                        } else {
                            empty_result.visibility = View.GONE

                            with(resultsRecyclerView) {
                                visibility = View.VISIBLE
                                adapter = ResultsAdapter(response, this@MainActivity, supportFragmentManager)
                                layoutManager = LinearLayoutManager(this@MainActivity)
                            }
                        }

                        fab.show()
                        snack!!.dismiss()
                    }
                } catch (timeout: IOException) {
                    uiThread {
                        fab.show()
                        snack!!.dismiss()
                        VibrationUtil.medium()

                        Alerter.create(this@MainActivity)
                                .setTitle("No internet connection")
                                .setText("Cannot reach youtube servers, please check your connection")
                                .setIcon(ContextCompat.getDrawable(this@MainActivity, R.drawable.warning)!!)
                                .setBackgroundDrawable(GradientGenerator.appThemeGradient)
                                .show()
                    }
                }
            }
        }
    }

    private var isInError = false

    private fun displayFormError() {
        isInError = true
        fab.hide()
        VibrationUtil.strong()

        Handler().postDelayed({
            fab.show()
            searchLayout.isErrorEnabled = false
            isInError = false
        }, 2500)
    }

    private val onPackageDownloadCompleated: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == -1L) return

            UpdateUtil.openUpdateInPackageManager(context)

            try {
                Alerter.clearCurrent(this@MainActivity)
            } catch (invalid: IllegalArgumentException) { logw(invalid) }
        }
    }

    private fun initUpdateChecker() = onceOutOf3 {
        doAsync {
            val requestBuilder = UpdateRequestBuilder.get()
            val request = OkHttpClient().newCall(requestBuilder).execute()

            val jsonRequest = request.body()!!.string()

            val gson = Gson()
            val updateCheck = gson.fromJson(
                    jsonRequest,
                    UpdateCheck::class.java)

            if (updateCheck.versionCode > BuildConfig.VERSION_CODE && !App.prefs.getBoolean(Keys.ignoring + updateCheck.versionCode, false)) {
                uiThread {
                    MaterialDialog(this@MainActivity)
                            .title(text = "Version ${updateCheck.versionName} found!")
                            .message(text = updateCheck.changelog)
                            .positiveButton(text = if (UpdateUtil.hasPackageBeenDownloaded(updateCheck.versionName))
                                "INSTALL UPDATE" else "DOWNLOAD UPDATE") { dialog ->
                                if (UpdateUtil.hasPackageBeenDownloaded(updateCheck.versionName)) {
                                    UpdateUtil.openUpdateInPackageManager(this@MainActivity)
                                    dialog.dismiss()
                                } else {
                                    val uri = Uri.parse(if (updateCheck.downloadInfo.useBundledUpdateLink)
                                        APK_URL else updateCheck.downloadInfo.updateLink!!)
                                    val downloadRequest = DownloadManager.Request(uri)

                                    with(downloadRequest) {
                                        setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                        setAllowedOverRoaming(true)
                                        setVisibleInDownloadsUi(true)
                                        setTitle(UpdateUtil.getNotificationTitle(updateCheck))
                                        setDescription(UpdateUtil.getNotificationContent())
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                                UpdateUtil.getDestinationSubpath(updateCheck))
                                    }

                                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(downloadRequest)

                                    registerReceiver(onPackageDownloadCompleated, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                                    dialog.dismiss()
                                    
                                    Alerter.create(this@MainActivity)
                                            .setTitle(UpdateUtil.getNotificationContent())
                                            .setText(UpdateUtil.getNotificationTitle(updateCheck))
                                            .setBackgroundDrawable(GradientGenerator.successGradient)
                                            .setIcon(R.drawable.download)
                                            .setDuration(7_000)
                                            .show()
                                }
                            }
                            .negativeButton(text = "NO") { dialog ->
                                if (dialog.isCheckPromptChecked() && UpdateUtil.hasPackageBeenDownloaded(updateCheck.versionName)) {
                                    UpdateUtil.clearDuplicatedInstallationPackage()
                                }
                                dialog.dismiss()
                            }
                            .checkBoxPrompt(text = "Ignore this version", isCheckedDefault = false) { state ->
                                App.prefs.put(Keys.ignoring + updateCheck.versionCode, state)
                            }
                            .noAutoDismiss()
                            .show()
                }
            }
        }
    }

    private fun initNetwork() = registerReceiver(networkConnectionListener, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    private fun unregisterReceivers() = try {
        unregisterReceiver(networkConnectionListener)
        unregisterReceiver(onPackageDownloadCompleated)
    } catch (notRegistered: IllegalArgumentException) { loge(notRegistered) }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    lateinit var checklistDialog: MaterialDialog

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
            R.id.action_list -> {
                if (ChecklistUtil.isEmpty(this)) {
                    checklistDialog = MaterialDialog(this)
                            .customView(R.layout.empty_checklist)
                    checklistDialog.show()
                } else {
                    checklistDialog = MaterialDialog(this)
                            .title(text = "Checklist")
                            .customListAdapter(ChecklistAdapter(ChecklistUtil.get(this).toMutableList(), this))
                    checklistDialog.show()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
    }
}
