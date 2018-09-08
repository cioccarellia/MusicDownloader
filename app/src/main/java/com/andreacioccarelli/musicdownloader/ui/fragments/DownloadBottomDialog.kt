package com.andreacioccarelli.musicdownloader.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.CardView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.extensions.sanitize
import com.andreacioccarelli.musicdownloader.extensions.toUri
import com.andreacioccarelli.musicdownloader.extensions.updateState
import com.andreacioccarelli.musicdownloader.ui.drawables.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.ChecklistUtil
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener
import com.tapadoo.alerter.Alerter
import es.dmoral.toasty.Toasty
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread


/**
 * Created by La mejor on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.ui.fragment
 */

@SuppressLint("ValidFragment")
class DownloadBottomDialog(val remoteResult: Result) : BottomSheetDialogFragment() {

    private val prefs by lazy { CryptoPrefs(App.instance.baseContext, FILE, KEY) }
    private var isInChecklist = false
    lateinit var title: TextView

    override fun getTheme() = R.style.BottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.checklist, container, false)
        VibrationUtil.strong()

        title = view.find(R.id.thumb_title)
        title.text = remoteResult.snippet.title

        Glide.with(this)
                .load(remoteResult.snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(view.find(R.id.thumb_icon))

        view.find<CardView>(R.id.thumbCard).setOnClickListener {
            val dialog = MaterialDialog(requireContext())
                    .title(text = "Change file name")
                    .input(prefill = title.text, waitForPositiveButton = true) { dialog, text ->
                        title.text = text
                    }
                    .positiveButton(text = "SUBMIT")

            with(dialog) {
                show()
                getInputField()?.let { input ->
                    input.selectAll()
                    input.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(p0: Editable?) {
                            if (p0.isNullOrBlank()) {
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                return
                            }

                            val text = p0.toString()
                            val inputField = dialog.getInputField()!!

                            if (text.contains("/")) {
                                inputField.error = "File name cannot contain /"
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                return
                            }

                            if (text.endsWith(".mp3") || text.endsWith(".mp4")) {
                                inputField.error = "We will think about putting an extension, just enter the file name"
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                return
                            }

                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                    })
                }
            }
        }

        view.find<CardView>(R.id.play).setOnClickListener { openVideoInDialog() }
        view.find<CardView>(R.id.open_video).setOnClickListener { openVideo() }
        view.find<CardView>(R.id.open_channel).setOnClickListener { openChannel() }
        view.find<CardView>(R.id.copy_link).setOnClickListener { copyLink() }
        view.find<CardView>(R.id.share_link).setOnClickListener { shareLink() }
        view.find<CardView>(R.id.mp3).setOnClickListener { handleClick(Format.MP3) }
        view.find<CardView>(R.id.mp4).setOnClickListener { handleClick(Format.MP4) }

        val addTo = view.find<CardView>(R.id.add_to_list)
        val removeFrom = view.find<CardView>(R.id.remove_from_list)

        isInChecklist = ChecklistUtil.contains(requireContext(), remoteResult.snippet.title)

        if (isInChecklist) {
            removeFrom.setOnClickListener {
                ChecklistUtil.remove(requireContext(), remoteResult.snippet.title)
                dismiss()
                VibrationUtil.medium()
            }

            addTo.visibility = View.GONE
        } else {
            addTo.setOnClickListener {
                ChecklistUtil.add(requireContext(), remoteResult.snippet.title, remoteResult.snippet.thumbnails.medium.url)
                dismiss()
                VibrationUtil.medium()
            }

            removeFrom.visibility = View.GONE
        }

        return view
    }

    private fun openVideoInDialog() {
        val dialog = MaterialDialog(requireContext())
                .customView(R.layout.video_player_dialog, scrollable = false)

        dialog.window!!.setBackgroundDrawable(GradientGenerator.make(26F, R.color.Grey_1000, R.color.Grey_1000))
        val youtubePlayer = dialog.getCustomView()!!.find<YouTubePlayerView>(R.id.player)

        youtubePlayer.initialize({ initializedYouTubePlayer ->
            initializedYouTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                override fun onReady() {
                    initializedYouTubePlayer.loadVideo(remoteResult.id.videoId, 0f)
                }
            })
        }, true)

        with(dialog) {
            show()
            setOnDismissListener {
                youtubePlayer.release()
            }
        }
    }

    private val watchLink = "$YOUTUBE_WATCH_URL${remoteResult.id.videoId}"

    private fun openVideo() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.run {
                intent.data = watchLink.toUri()
                setPackage("com.google.android.youtube")
            }

            startActivity(intent)
        } catch (err: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = watchLink.toUri()
            startActivity(intent)
        }
    }

    private fun openChannel() {
        val channelUrl = "$YOUTUBE_CHANNEL_URL${remoteResult.snippet.channelId}".toUri()

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.run {
                intent.data = channelUrl
                setPackage("com.google.android.youtube")
            }

            startActivity(intent)
        } catch (err: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = channelUrl
            startActivity(intent)
        }
    }


    private fun copyLink() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", watchLink)
        clipboard.primaryClip = clip

        Toasty.success(requireContext(), "Link copied!").show()
        VibrationUtil.medium()
        dismiss()
    }

    private fun shareLink() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.run {
            type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, remoteResult.snippet.title)
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, watchLink)
        }
        startActivity(Intent.createChooser(sharingIntent, "Share link to.."))
        VibrationUtil.medium()
        dismiss()
    }

    private lateinit var conversionAlert: Alerter

    private fun handleClick(format: Format) {
        VibrationUtil.medium()
        dismiss()
        val act = requireActivity()
        val downloadManager = act.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        doAsync {
            val requestBuilder = DownloadLinkRequestsBuilder.get(remoteResult.id.videoId, format)
            val request = OkHttpClient().newCall(requestBuilder).execute()
            val gson = Gson()

            val response = gson.fromJson(request.body()!!.string(), DirectLinkResponse::class.java)

            when {
                response.state == RESPONSE_OK -> processDownloadForReadyFile(act, response, downloadManager)
                response.state == RESPONSE_WAIT -> {
                    conversionAlert = Alerter.create(act)

                    with(conversionAlert) {
                        setTitle("Preparing download")
                        setText("Waiting server to process file...")
                        enableInfiniteDuration(true)
                        setBackgroundDrawable(GradientGenerator.appThemeGradient)
                        setIcon(R.drawable.download)
                        enableProgress(true)
                        show()
                    }

                    processDownloadForWaitingFile(act, requestBuilder, downloadManager)
                }
                response.state == RESPONSE_ERROR -> {
                    Alerter.create(act)
                            .setTitle("Error while downloading")
                            .setText(response.reason)
                            .setDuration(7_000)
                            .setBackgroundDrawable(GradientGenerator.appThemeGradient)
                            .setIcon(R.drawable.download_error)
                            .show()
                }
                else -> {
                    loge("Unknown state: $response")
                    throw IllegalStateException()
                }
            }
        }
    }

    private fun processDownloadForReadyFile(act: Activity, response: DirectLinkResponse, downloadManager: DownloadManager) {
        act.runOnUiThread {
            if (isInChecklist) ChecklistUtil.remove(act, remoteResult.snippet.title)

            val SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath!!
            val DOWNLOAD_PATH = Environment.DIRECTORY_DOWNLOADS!!

            val defaultPath = "$SDCARD_PATH/$DOWNLOAD_PATH/"

            val fileName = if (title.text.isBlank() || title.text.isEmpty()) response.title else title.text
            val compleateFileName = "$fileName.${response.format}"
            val fileDownloadLink = response.download.sanitize()
            val filePath = prefs.getString(Keys.folder, defaultPath) + fileName

            Alerter.hide()
            Alerter.create(act)
                    .setTitle("Downloading file")
                    .setText(compleateFileName)
                    .setDuration(7_000)
                    .setBackgroundDrawable(GradientGenerator.appThemeGradient)
                    .setIcon(R.drawable.download)
                    .show()

            doAsync {
                val uri = fileDownloadLink.toUri()
                val downloadRequest = DownloadManager.Request(uri)

                with(downloadRequest) {
                    setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    setAllowedOverRoaming(true)
                    setVisibleInDownloadsUi(true)
                    setTitle("Downloading ${remoteResult.snippet.title}")
                    setDescription(fileName)
                    allowScanningByMediaScanner()
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MusicDownloader/$compleateFileName")
                }

                downloadManager.enqueue(downloadRequest)
            }
        }
    }

    private fun processDownloadForWaitingFile(act: Activity, requestBuilder: Request, downloadManager: DownloadManager) {
        doAsync {
            val nextRequest = OkHttpClient().newCall(requestBuilder).execute()

            val json = nextRequest.body()!!.string()

            val nextResponse = Gson().fromJson(json, DirectLinkResponse::class.java)

            uiThread {
                if (nextResponse.state != RESPONSE_ERROR) {
                    conversionAlert.updateState(nextResponse)

                    if (nextResponse.state == RESPONSE_OK) {
                        Alerter.clearCurrent(act)
                        processDownloadForReadyFile(act, nextResponse, downloadManager)
                    } else {
                        processDownloadForWaitingFile(act, requestBuilder, downloadManager)
                    }
                } else {
                    Alerter.create(act)
                            .setTitle("Cannot download file")
                            .setText("Video length exceeds 3 hours")
                            .setDuration(7_000)
                            .setBackgroundDrawable(GradientGenerator.errorGradient)
                            .setIcon(R.drawable.ic_error_outline_white_48dp)
                            .show()
                    return@uiThread
                }
            }
        }
    }
}