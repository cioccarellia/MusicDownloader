package com.andreacioccarelli.musicdownloader.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.constants.MIME_TEXT_PLAIN
import com.andreacioccarelli.musicdownloader.constants.PACKAGE_YOUTUBE
import com.andreacioccarelli.musicdownloader.constants.YOUTUBE_CHANNEL_URL
import com.andreacioccarelli.musicdownloader.constants.YOUTUBE_WATCH_URL
import com.andreacioccarelli.musicdownloader.data.model.Format
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.extensions.correctSpecialChars
import com.andreacioccarelli.musicdownloader.extensions.toUri
import com.andreacioccarelli.musicdownloader.ui.downloader.MusicDownloader
import com.andreacioccarelli.musicdownloader.ui.gradients.GradientGenerator
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistStore
import com.andreacioccarelli.musicdownloader.util.ToastUtil
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.jetbrains.anko.find

/**
 * Created by La mejor on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.ui.fragment
 */

@SuppressLint("ValidFragment")
class BottomDialogFragment(val remoteResult: Result) : BottomSheetDialogFragment() {

    private var isInChecklist = false
    private lateinit var titleTextView: TextView
    var title = ""

    override fun getTheme() = R.style.BottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_dialog, container, false)
        VibrationUtil.medium()

        title = remoteResult.snippet.title.correctSpecialChars()

        titleTextView = view.find(R.id.thumb_title)
        titleTextView.text = title

        Glide.with(this.requireActivity())
                .load(remoteResult.snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(view.find(R.id.thumb_icon))

        with(view) {
            find<CardView>(R.id.thumbCard).setOnClickListener {
                showChangeFileNameDialog()
            }
            find<CardView>(R.id.play).setOnClickListener { openVideoInDialog() }
            find<CardView>(R.id.open_video).setOnClickListener { openVideo() }
            find<CardView>(R.id.open_channel).setOnClickListener { openChannel() }
            find<CardView>(R.id.copy_link).setOnClickListener { copyLink() }
            find<CardView>(R.id.share_link).setOnClickListener { shareLink() }
        }

        val addTo = view.find<CardView>(R.id.add_to_list)
        val removeFrom = view.find<CardView>(R.id.remove_from_list)
        isInChecklist = ChecklistStore.contains(remoteResult.id.videoId)

        if (isInChecklist) {
            removeFrom.setOnClickListener {
                ChecklistStore.remove(remoteResult.snippet.title)
                dismiss()
                VibrationUtil.medium()
                ToastUtil.success("Removed from Checklist")
            }

            addTo.visibility = View.GONE
        } else {
            addTo.setOnClickListener {
                ChecklistStore.add(remoteResult.snippet.title, watchLink, remoteResult.snippet.thumbnails.medium.url)
                dismiss()
                VibrationUtil.medium()
                ToastUtil.success("Added to Checklist")
            }

            removeFrom.visibility = View.GONE
        }

        val mp3 = view.find<CardView>(R.id.mp3)
        mp3.apply {
            setOnClickListener { handleClick(Format.MP3) }
            setOnLongClickListener {
                showChangeFileNameDialog()
                true
            }
        }

        val mp4 = view.find<CardView>(R.id.mp4)
        mp4.apply {
            setOnClickListener { handleClick(Format.MP4) }
            setOnLongClickListener {
                showChangeFileNameDialog()
                true
            }
        }

        return view
    }

    private fun openVideoInDialog() {
        val dialog = MaterialDialog(requireContext())
                .customView(R.layout.video_player_dialog, scrollable = false)

        dialog.window!!.setBackgroundDrawable(GradientGenerator.make(26F, R.color.Grey_1000, R.color.Grey_1000))

        val youtubePlayer = dialog.getCustomView().find<YouTubePlayerView>(R.id.player)

        youtubePlayer.initialize(object: YouTubePlayerListener {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(remoteResult.id.videoId, 0f)
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                when(error) {
                    PlayerConstants.PlayerError.UNKNOWN -> ToastUtil.error("Unknown error while playing video")
                    PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> ToastUtil.error("Internal error while playing video")
                    PlayerConstants.PlayerError.HTML_5_PLAYER -> ToastUtil.error("HTML player error")
                    PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> ToastUtil.warn("Video not found")
                    PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> ToastUtil.warn("MusicDownloader Can't play this type of video")
                }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {}
            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
            override fun onApiChange(youTubePlayer: YouTubePlayer) {}
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {}
            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
        }, true)

        with(dialog) {
            show()
            setOnDismissListener {
                youtubePlayer.release()
            }
        }
    }

    private val watchLink = "$YOUTUBE_WATCH_URL${remoteResult.id.videoId}"

    private fun showChangeFileNameDialog() {
        val dialog = MaterialDialog(requireContext())
                .title(text = "Change file name")
                .input(prefill = titleTextView.text, waitForPositiveButton = true) { _, text ->
                    titleTextView.text = text
                    title = text.toString()
                }
                .positiveButton(text = "SUBMIT")

        with(dialog) {
            show()
            getInputField().let { input ->
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

    private fun openVideo() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.run {
                intent.data = watchLink.toUri()
                setPackage(PACKAGE_YOUTUBE)
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

        ToastUtil.success("Link copied", R.drawable.copy)
        VibrationUtil.medium()
        dismiss()
    }

    private fun shareLink() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.run {
            type = MIME_TEXT_PLAIN
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, remoteResult.snippet.title)
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, watchLink)
        }
        startActivity(Intent.createChooser(sharingIntent, "Share link to"))
        VibrationUtil.medium()
        dismiss()
    }

    private fun handleClick(format: Format) {
        VibrationUtil.medium()
        dismiss()
        MusicDownloader(activity, watchLink)
                .exec(format)
    }
}