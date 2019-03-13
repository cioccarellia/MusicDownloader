package com.andreacioccarelli.musicdownloader.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.andreacioccarelli.musicdownloader.App.Companion.checklist
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.client.DownloadClient
import com.andreacioccarelli.musicdownloader.data.enums.Format
import com.andreacioccarelli.musicdownloader.data.requests.YoutubeRequestBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.YoutubeSearchResponse
import com.andreacioccarelli.musicdownloader.extensions.*
import com.andreacioccarelli.musicdownloader.ui.adapters.ChecklistAdapter
import com.andreacioccarelli.musicdownloader.ui.adapters.SearchAdapter
import com.andreacioccarelli.musicdownloader.ui.base.BaseActivity
import com.andreacioccarelli.musicdownloader.ui.gradients.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_layout.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.IOException

/**
 *  Designed and Developed by Andrea Cioccarelli
 */

@SuppressLint("GoogleAppIndexingApiWarning")
class MainActivity : BaseActivity() {

    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        initToolbar()
        initInput()
        initRecyclerView()
        initFab()
        initIntentReceiver()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        title = "Music Downloader"
    }

    private fun initInput() {
        with(search) {
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

                if (isShowingError) {
                    fab.show()
                }
            }

            popUpKeyboard()
            requestFocus()
        }
    }

    private var snack: Snackbar? = null
    private val searches = mutableListOf<Int>()
    private var searchCount = 0

    private fun initRecyclerView() {
        with(resultsRecyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun initFab() {
        fab.setOnClickListener { view ->
            if (!areAllPermissionsGranted) {
                askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) {
                    if (it.isAllGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
                        areAllPermissionsGranted = true
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
                searchLayout.error = "Fill in the search field"

                displayFormError()
                return@setOnClickListener
            }

            if (isOffline) {
                searchLayout.error = "Device is offline"

                displayFormError()
                return@setOnClickListener
            }


            snack = Snackbar.make(view, "Searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CANCEL") {
                        isSearching = false
                        search.isEnabled = true
                        fab.show()
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.Orange_600))

            performSearch()
        }
    }

    fun performSearch(implicitLink: String = "") {
        searchLayout.isErrorEnabled = false
        search.dismissKeyboard()
        resultsRecyclerView?.smoothScrollToPosition(0)

        searches.add(searchCount++)
        isSearching = true

        fab.hide()

        if (snack == null) {
            snack = Snackbar.make(window.decorView.findViewById(android.R.id.content), "Searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CANCEL") {
                        isSearching = false
                        search.isEnabled = true
                        fab.show()
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.Orange_600))
            snack?.show()
        } else snack?.show()

        val query = if (implicitLink.isEmpty()) search.text else implicitLink

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val requestBuilder = YoutubeRequestBuilder.get(query)
                val request = OkHttpClient().newCall(requestBuilder).execute()

                val jsonRequest = request.body()!!.string()
                val response = Gson().fromJson(
                        jsonRequest,
                        YoutubeSearchResponse::class.java)

                withContext(Dispatchers.Main) {
                    if (!isSearching || searches.contains(searchCount)) {
                        // Another search has just started, dismissing
                        return@withContext
                    }
                    isSearching = false

                    val searchResultsCount = response.pageInfo.totalResults

                    if (searchResultsCount == 0) {
                        emptyResult.visibility = View.VISIBLE
                        resultsRecyclerView.visibility = View.GONE
                    } else {
                        emptyResult.visibility = View.GONE
                        val searchAdapter = SearchAdapter(response, this@MainActivity, supportFragmentManager)

                        with(resultsRecyclerView) {
                            visibility = View.VISIBLE
                            adapter = ScaleInAnimationAdapter(searchAdapter).apply {
                                setDuration(800)
                                setInterpolator(OvershootInterpolator())
                                setFirstOnly(true)
                                setHasFixedSize(true)
                            }
                        }
                    }



                    if (resultsRecyclerView.adapter?.itemCount == 1) {
                        delay(107)
                        resultsRecyclerView.getChildAt(0)?.performClick()
                    }

                    fab.show()
                    snack?.dismiss()
                    search.clearFocus()
                }
            } catch (timeout: IOException) {
                withContext(Dispatchers.Main) {
                    fab.show()
                    snack?.dismiss()
                    VibrationUtil.medium()

                    Alerter.create(this@MainActivity)
                            .setTitle("No internet connection")
                            .setText("Cannot reach YouTube servers, please check your connection")
                            .setIcon(ContextCompat.getDrawable(this@MainActivity, R.drawable.toast_warning)!!)
                            .setBackgroundDrawable(GradientGenerator.appThemeGradient)
                            .show()
                }
            }
        }
    }

    private fun initIntentReceiver() {
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    search.text = intent.getStringExtra(Intent.EXTRA_TEXT).toEditable()
                    fab.performClick()
                }
            }
        }
    }

    private var isShowingError = false
    private fun displayFormError() {
        isShowingError = true
        fab.hide()
        VibrationUtil.strong()

        Handler().postDelayed({
            fab?.show()
            searchLayout?.isErrorEnabled = false
            isShowingError = false
        }, 2500)
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    lateinit var checklistDialog: MaterialDialog
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_list -> {

            checklistDialog = MaterialDialog(this)

            if (checklist.isEmpty()) {
                checklistDialog =  MaterialDialog(this).show {
                    customView(R.layout.empty_view_checklist)
                }

            } else {
                val checklistAdapter = ChecklistAdapter(this@MainActivity)
                checklistDialog = MaterialDialog(this).show {
                    title(text = "Checklist")
                    positiveButton(text = "DOWNLOAD ALL") {
                        MaterialDialog(this@MainActivity).show {
                            title(text = "Select Format")
                            listItemsSingleChoice(items = listOf("MP3", "MP4"), initialSelection = 0) { _, index, _ ->
                                val format = Format.values()[index]

                                DownloadClient(this@MainActivity,
                                        checklist.toDownloadInfoList())
                                        .exec(format)
                            }
                            positiveButton(text = "SELECT")
                        }
                    }
                    customListAdapter(
                        ScaleInAnimationAdapter(checklistAdapter).apply {
                            setDuration(500)
                            setInterpolator(OvershootInterpolator())
                            setFirstOnly(false)
                        }
                    )
                }
            }
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}
