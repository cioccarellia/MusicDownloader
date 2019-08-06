package com.andreacioccarelli.musicdownloader.ui.adapters

import android.app.Activity
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.App.Companion.checklist
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistEntry
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.data.serializers.YoutubeSearchResponse
import com.andreacioccarelli.musicdownloader.extensions.applyChecklistBadge
import com.andreacioccarelli.musicdownloader.extensions.breakHtml
import com.andreacioccarelli.musicdownloader.extensions.contains
import com.andreacioccarelli.musicdownloader.ui.fragments.BottomDialogFragment
import com.andreacioccarelli.musicdownloader.ui.holders.ResultCardViewHolder
import com.andreacioccarelli.musicdownloader.ui.toast.ToastUtil
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.andreacioccarelli.musicdownloader.util.YoutubeUtil
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class SearchResultAdapter(
    response: YoutubeSearchResponse,
    private val activity: Activity,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ResultCardViewHolder>() {

    override fun getItemCount() = data.size

    val data = ArrayList<Result>()

    init {
        data.clear()
        data.addAll(response.items)
        data.sortByDescending { App.checklistedIds.contains(it.id.videoId) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return ResultCardViewHolder(v)
    }

    override fun onBindViewHolder(holder: ResultCardViewHolder, i: Int) {
        val videoId = data[i].id.videoId
        
        Glide.with(activity)
                .load(data[i].snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(holder.icon)

        with(holder) {
            title.text = data[i].snippet.title.breakHtml()

            iconLayout.setOnClickListener {
                YoutubeUtil.getVideoPreviewDialog(activity, videoId).show()
            }

            card.setOnClickListener {
                val bottomSheetFragment = BottomDialogFragment(data[i], holder)
                bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
            }

            card.setOnLongClickListener {
                VibrationUtil.medium()
                if (checklist.contains(videoId)) {
                    ToastUtil.error("Removed from checklist", R.drawable.remove_outline, duration = Toast.LENGTH_SHORT)
                    title.applyChecklistBadge(false)

                    CoroutineScope(Dispatchers.Default).launch {
                        checklist.remove(videoId)
                        App.checklistedIds.remove(videoId)
                    }
                } else {
                    ToastUtil.success("Added to checklist", R.drawable.add_outline, duration = Toast.LENGTH_SHORT)
                    title.applyChecklistBadge(true)

                    CoroutineScope(Dispatchers.Default).launch {
                        checklist.add(ChecklistEntry(data[i]))
                        App.checklistedIds.add(videoId)
                    }
                }

                true
            }

            Handler().post {
                if (title.lineCount == 1) {
                    title.height = activity.resources.getDimension(R.dimen.result_thumb_width).toInt()
                }

                titleLayout.visibility = View.VISIBLE

                if (App.checklistedIds.contains(videoId)) {
                    title.applyChecklistBadge(true)
                } else {
                    title.applyChecklistBadge(false)
                }
            }
        }
    }
}