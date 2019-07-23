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

    val data by lazy { ArrayList<Result>() }

    init {
        data.clear()
        data.addAll(response.items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return ResultCardViewHolder(v)
    }

    override fun onBindViewHolder(holder: ResultCardViewHolder, uncheckedIndex: Int) {
        val i = holder.adapterPosition

        Glide.with(activity)
                .load(data[i].snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(holder.icon)

        with(holder) {
            title.text = data[i].snippet.title.breakHtml()

            iconLayout.setOnClickListener {
                YoutubeUtil.getVideoViewerDialog(activity, data[i].id.videoId).show()
            }

            card.setOnClickListener {
                val bottomSheetFragment = BottomDialogFragment(data[i], holder)
                bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
            }

            card.setOnLongClickListener {
                VibrationUtil.medium()
                if (checklist.contains(data[i].id.videoId)) {
                    ToastUtil.error("Removed from checklist", R.drawable.remove_outline, duration = Toast.LENGTH_SHORT)
                    title.applyChecklistBadge(false)

                    CoroutineScope(Dispatchers.Default).launch {
                        checklist.remove(data[i].id.videoId)
                        App.checklistedIds.remove(data[i].id.videoId)
                    }
                } else {
                    ToastUtil.success("Added to checklist", R.drawable.add_outline, duration = Toast.LENGTH_SHORT)
                    title.applyChecklistBadge(true)

                    CoroutineScope(Dispatchers.Default).launch {
                        checklist.add(ChecklistEntry(data[i]))
                        App.checklistedIds.add(data[i].id.videoId)
                    }
                }

                true
            }

            Handler().post {
                if (title.lineCount == 1) {
                    title.height = activity.resources.getDimension(R.dimen.result_thumb_width).toInt()
                }

                titleLayout.visibility = View.VISIBLE

                if (App.checklistedIds.contains(data[i].id.videoId)) {
                    title.applyChecklistBadge(true)
                } else {
                    title.applyChecklistBadge(false)
                }
            }
        }
    }
}