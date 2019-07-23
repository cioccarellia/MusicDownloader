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
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.andreacioccarelli.musicdownloader.App.Companion.checklist
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.client.DownloadClient
import com.andreacioccarelli.musicdownloader.constants.MIME_TEXT_PLAIN
import com.andreacioccarelli.musicdownloader.constants.PACKAGE_YOUTUBE
import com.andreacioccarelli.musicdownloader.constants.YOUTUBE_CHANNEL_URL
import com.andreacioccarelli.musicdownloader.constants.YOUTUBE_WATCH_URL
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistEntry
import com.andreacioccarelli.musicdownloader.data.enums.Format
import com.andreacioccarelli.musicdownloader.data.model.DownloadInfo
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.extensions.applyChecklistBadge
import com.andreacioccarelli.musicdownloader.extensions.breakHtml
import com.andreacioccarelli.musicdownloader.extensions.toUri
import com.andreacioccarelli.musicdownloader.ui.holders.ResultCardViewHolder
import com.andreacioccarelli.musicdownloader.ui.toast.ToastUtil
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.andreacioccarelli.musicdownloader.util.YoutubeUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.find
import org.jetbrains.anko.runOnUiThread

/**
 * Created by La mejor on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.ui.fragment
 */

@SuppressLint("ValidFragment")
class BottomDialogFragment(
        private val remoteResult: Result,
        private val adapter: ResultCardViewHolder
) : BottomSheetDialogFragment() {

    private var isInChecklist = false
    private lateinit var titleTextView: TextView
    var title = ""

    override fun getTheme() = R.style.BottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_dialog, container, false)
        VibrationUtil.medium()

        title = remoteResult.snippet.title.breakHtml()

        titleTextView = view.find(R.id.thumb_title)
        titleTextView.text = title

        Glide.with(this.requireActivity())
                .load(remoteResult.snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(view.find(R.id.thumb_icon))

        with(view) {
            find<CardView>(R.id.thumbCard).setOnClickListener { showChangeFileNameDialog() }
            find<CardView>(R.id.play).setOnClickListener { openVideoInDialog() }
            find<CardView>(R.id.open_video).setOnClickListener { openVideo() }
            find<CardView>(R.id.open_channel).setOnClickListener { openChannel() }
            find<CardView>(R.id.copy_link).setOnClickListener { copyLink() }
            find<CardView>(R.id.share_link).setOnClickListener { shareLink() }
        }

        val addTo = view.find<CardView>(R.id.add_to_list)
        val removeFrom = view.find<CardView>(R.id.remove_from_list)
        val mp3 = view.find<CardView>(R.id.mp3)
        val mp4 = view.find<CardView>(R.id.mp4)

        val list = checklist.find(remoteResult.id.videoId)
        isInChecklist = list.isNotEmpty()

        if (isInChecklist) {
            removeFrom.setOnClickListener {
                CoroutineScope(Dispatchers.Default).launch {
                    checklist.remove(remoteResult.id.videoId)
                }

                VibrationUtil.medium()
                dismiss()

                context?.runOnUiThread {
                    adapter.title.applyChecklistBadge(false)
                    ToastUtil.error("Removed from Checklist", R.drawable.remove_outline)
                }
            }

            addTo.visibility = View.GONE
        } else {
            addTo.setOnClickListener {
                CoroutineScope(Dispatchers.Default).launch {
                    checklist.add(
                         ChecklistEntry(
                              remoteResult.id.videoId,
                              title,
                              remoteResult.snippet.thumbnails.medium.url
                         )
                    )
                }

                VibrationUtil.medium()
                dismiss()

                context?.runOnUiThread {
                    adapter.title.applyChecklistBadge(true)
                    ToastUtil.success("Added to Checklist", R.drawable.add_outline)
                }
            }

            removeFrom.visibility = View.GONE
        }


        mp3.apply {
            setOnClickListener { handleClick(Format.MP3) }
            setOnLongClickListener {
                showChangeFileNameDialog()
                true
            }
        }

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
        YoutubeUtil.getVideoViewerDialog(requireContext(), remoteResult.id.videoId).show()
    }

    private fun getFullLink() = "$YOUTUBE_WATCH_URL${remoteResult.id.videoId}"

    private fun showChangeFileNameDialog() {
        val dialog = MaterialDialog(requireContext())
                .title(text = "Edit file name")
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
                    override fun afterTextChanged(text: Editable?) {
                        if (text.isNullOrBlank()) {
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                            return
                        }

                        val newName = text.toString()
                        val inputField = dialog.getInputField()

                        if (newName.contains("/")) {
                            inputField.error = "Filename cannot contain /"
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                            return
                        }

                        if (newName.endsWith(".mp3") || newName.endsWith(".mp4")) {
                            inputField.error = "We will think about putting an extension, just enter the file name"
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                            return
                        }

                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                })
            }
        }
    }

    private fun openVideo() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.run {
                intent.data = getFullLink().toUri()
                setPackage(PACKAGE_YOUTUBE)
            }

            startActivity(intent)
        } catch (err: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = getFullLink().toUri()
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
        val clip = ClipData.newPlainText("", getFullLink())
        clipboard.primaryClip = clip

        ToastUtil.success("Link copied", R.drawable.copy)
        VibrationUtil.medium()
        dismiss()
    }

    private fun shareLink() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.run {
            type = MIME_TEXT_PLAIN
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title)
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getFullLink())
        }
        startActivity(Intent.createChooser(sharingIntent, "Share link to"))
        VibrationUtil.medium()
        dismiss()
    }

    private fun handleClick(format: Format) {
        VibrationUtil.medium()
        dismiss()

        activity?.let {
            val downloadInfo = DownloadInfo(getFullLink(), title)
            DownloadClient(activity, downloadInfo).exec(format)
        }
    }
}