package com.andreacioccarelli.musicdownloader.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.text.ClipboardManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
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
import com.andreacioccarelli.musicdownloader.ui.adapters.SearchResultAdapter
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

/**
 *  Designed and Developed by Andrea Cioccarelli
 */

class MainActivity : BaseActivity() {

    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)

        initToolbar()
        initInput()
        initClipboard()
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

            onSubmit {
                fab.performClick()
                search.dismissKeyboard()
            }

            popUpKeyboard()
            requestFocus()
        }
    }

    private fun initClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.text

        text?.let {
            if (it.isYoutubeUrl && intent?.getStringExtra(Intent.EXTRA_TEXT) == null) {
                search.text = clipboard.text.toEditable()
                performSearch(clipboard.text.toString())
            }
        }
    }

    private fun initRecyclerView() {
        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private var snack: Snackbar? = null

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

            if (search.text!!.isEmpty() || search.text!!.isBlank()) {
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

    private val searchList = mutableListOf<Int>()
    private var searchCount = 0

    private var fetchExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        CoroutineScope(Dispatchers.Main.immediate).launch {
            fab.show()
            snack?.dismiss()
            VibrationUtil.medium()

            Alerter.create(this@MainActivity)
                    .setTitle("No internet connection")
                    .setText("Couldn't reach YouTube servers, please check your connection")
                    .setIcon(ContextCompat.getDrawable(this@MainActivity, R.drawable.toast_warning)!!)
                    .setBackgroundDrawable(GradientGenerator.appThemeGradient)
                    .show()
        }
    }

    fun performSearch(implicitLink: String = "") {
        searchLayout.isErrorEnabled = false
        search.dismissKeyboard()
        recyclerView?.smoothScrollToPosition(0)

        searchList.add(searchCount++)
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

        GlobalScope.launch(Dispatchers.IO + fetchExceptionHandler) {
            val requestBuilder = YoutubeRequestBuilder.get(query!!)
            val request = OkHttpClient().newCall(requestBuilder).execute()

            val jsonRequest = request.body()!!.string()
            val response = Gson().fromJson(jsonRequest, YoutubeSearchResponse::class.java)

            withContext(Dispatchers.Main.immediate) {
                if (!isSearching || searchList.contains(searchCount)) {
                    // Another search has just started, dismissing
                    return@withContext
                }

                isSearching = false
                val numberOfResults = response.pageInfo.totalResults

                if (numberOfResults == 0) {
                    recyclerView.visibility = View.GONE
                    emptyResultImage.visibility = View.VISIBLE
                } else {
                    emptyResultImage.visibility = View.GONE
                    val searchAdapter = SearchResultAdapter(response, this@MainActivity, supportFragmentManager)

                    with(recyclerView) {
                        visibility = View.VISIBLE
                        adapter = ScaleInAnimationAdapter(searchAdapter).apply {
                            setDuration(207)
                            setFirstOnly(true)
                            setHasFixedSize(true)

                            if (isTablet) {
                                setInterpolator(LinearOutSlowInInterpolator())
                            } else {
                                setInterpolator(OvershootInterpolator())
                            }
                        }
                    }
                }

                if (recyclerView.adapter?.itemCount == 1) {
                    delay(107)
                    recyclerView.getChildAt(0)?.performClick()
                }

                fab.show()
                snack?.dismiss()
                search.clearFocus()
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
        }, 2000)
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
                val extensions = listOf("MP3", "MP4")

                checklistDialog = MaterialDialog(this).show {
                    title(text = "Checklist")
                    positiveButton(text = "DOWNLOAD ALL") {
                        MaterialDialog(this@MainActivity).show {
                            title(text = "Select Format")
                            listItemsSingleChoice(items = extensions, initialSelection = 0) { _, index, _ ->
                                // Enum hack
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
                            setDuration(207)
                            setInterpolator(LinearOutSlowInInterpolator())
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