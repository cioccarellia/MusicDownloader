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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.logkit.logd
import com.andreacioccarelli.logkit.loge
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.constants.*
import com.andreacioccarelli.musicdownloader.data.formats.Format
import com.andreacioccarelli.musicdownloader.data.requests.DownloadLinkRequestsBuilder
import com.andreacioccarelli.musicdownloader.data.serializers.DirectLinkResponse
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.extensions.toUri
import com.andreacioccarelli.musicdownloader.extensions.updateState
import com.andreacioccarelli.musicdownloader.ui.drawables.GradientGenerator
import com.andreacioccarelli.musicdownloader.util.DownloadListUtil
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
class BottomSheetChooser(val remoteResult: Result) : BottomSheetDialogFragment() {

    private val prefs by lazy { CryptoPrefs(App.instance.baseContext, FILE, KEY) }
    private var isInChecklist = false

    override fun getTheme() = R.style.BottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)
        VibrationUtil.strong()

        view.find<TextView>(R.id.thumb_title).text = remoteResult.snippet.title
        Glide.with(this)
                .load(remoteResult.snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(view.find(R.id.thumb_icon))

        view.find<CardView>(R.id.play).setOnClickListener { openVideo() }
        view.find<CardView>(R.id.view_channel).setOnClickListener { openChannel() }
        view.find<CardView>(R.id.copy_link).setOnClickListener { copyLink() }
        view.find<CardView>(R.id.share_link).setOnClickListener { shareLink() }
        view.find<CardView>(R.id.mp3).setOnClickListener { v ->  handleClick(Format.MP3, v) }
        view.find<CardView>(R.id.mp4).setOnClickListener { v -> handleClick(Format.MP4, v) }

        val addTo = view.find<CardView>(R.id.add_to_list)
        val removeFrom = view.find<CardView>(R.id.remove_from_list)

        isInChecklist = DownloadListUtil.contains(requireContext(), remoteResult.snippet.title)

        if (isInChecklist) {
            removeFrom.setOnClickListener {
                DownloadListUtil.remove(requireContext(), remoteResult.snippet.title)
                dismiss()
                VibrationUtil.medium()
            }

            addTo.visibility = View.GONE
        } else {
            addTo.setOnClickListener {
                DownloadListUtil.add(requireContext(), remoteResult.snippet.title)
                dismiss()
                VibrationUtil.medium()
            }

            removeFrom.visibility = View.GONE
        }

        return view
    }

    private fun openVideo() {
        val dialog = MaterialDialog(requireContext())
                .customView(R.layout.video_player, scrollable = false)

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

    private val watchLink = "$YOUTUBE_URL${remoteResult.id.videoId}"

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

    private fun handleClick(format: Format, view: View) {
        VibrationUtil.medium()
        dismiss()
        val act = requireActivity()
        val downloadManager = act.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        doAsync {
            val requestBuilder = DownloadLinkRequestsBuilder.get(remoteResult.id.videoId, format)
            val request = OkHttpClient().newCall(requestBuilder).execute()
            val gson = Gson()

            val json = request.body()!!.string()
            logd(json)

            val response = gson.fromJson(json, DirectLinkResponse::class.java)

            when {
                response.state == RESPONSE_OK -> processDownloadForReadyFile(act, response, downloadManager)
                response.state == RESPONSE_WAIT -> {
                    conversionAlert = Alerter.create(act)

                    with(conversionAlert) {
                        setTitle("Preparing download")
                        setText("Waiting server to process file...")
                        enableInfiniteDuration(true)
                        setBackgroundDrawable(GradientGenerator.infoGradient)
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
                            .setDuration(8_000)
                            .setBackgroundDrawable(GradientGenerator.infoGradient)
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
            if (isInChecklist) DownloadListUtil.remove(act, remoteResult.snippet.title)
            val fileName = "${response.title}.${response.format}"
            val fileDownloadLink = response.download
                    .replace("\"", "")
                    .replace("\"", "")
                    .replace("\\/", "/")

            val filePath = prefs.getString(Keys.folder, DEFAULT_PATH) + fileName

            logd(filePath, fileName, fileDownloadLink)

            Alerter.hide()
            Alerter.create(act)
                    .setTitle("Downloading file!")
                    .setText(fileName)
                    .setDuration(8_000)
                    .setBackgroundDrawable(GradientGenerator.infoGradient)
                    .setIcon(R.drawable.download)
                    .show()

            doAsync {
                val uri = fileDownloadLink.toUri()
                val downloadRequest = DownloadManager.Request(uri)

                with(downloadRequest) {
                    setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    setAllowedOverRoaming(true)
                    setVisibleInDownloadsUi(true)
                    setTitle("Downloading ${response.format} file")
                    setDescription(remoteResult.snippet.title)
                    allowScanningByMediaScanner()
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MusicDownloader/$fileName")
                }

                downloadManager.enqueue(downloadRequest)
            }
        }
    }

    private fun processDownloadForWaitingFile(act: Activity, requestBuilder: Request, downloadManager: DownloadManager) {
        doAsync {
            val nextRequest = OkHttpClient().newCall(requestBuilder).execute()

            val json = nextRequest.body()!!.string()
            logd(json)

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
                            .setDuration(8_000)
                            .setBackgroundDrawable(GradientGenerator.errorGradient)
                            .setIcon(R.drawable.ic_error_outline_white_48dp)
                            .show()
                    return@uiThread
                }
            }
        }
    }
}