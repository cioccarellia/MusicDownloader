package com.andreacioccarelli.musicdownloader.ui.adapters

import android.app.Activity
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.App.Companion.checklist
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.extensions.toYoutubeUrl
import com.andreacioccarelli.musicdownloader.ui.activities.MainActivity
import com.andreacioccarelli.musicdownloader.ui.holders.ChecklistCardViewHolder
import com.andreacioccarelli.musicdownloader.util.VibrationUtil
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.find

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class ChecklistAdapter(
        private val activity: Activity
) : RecyclerView.Adapter<ChecklistCardViewHolder>() {

    override fun getItemCount() = data.size

    val data by lazy {
        checklist.getAll()
            .toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistCardViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return ChecklistCardViewHolder(v)
    }

    override fun onBindViewHolder(holder: ChecklistCardViewHolder, uncheckedIndex: Int) {
        val i = holder.adapterPosition
        
        Glide.with(activity)
                .load(data[i].thumbnailLink)
                .thumbnail(0.1F)
                .into(holder.icon)

        holder.title.text = data[i].title

        with(holder.card) {
            setOnClickListener {
                // Dismisses dialog, puts text inside the box and performs the search
                val ref = (activity as MainActivity)

                val search = ref.find<TextView>(R.id.search)
                search.text = data[i].title

                val rv = ref.find(R.id.recyclerView) as RecyclerView?
                rv?.smoothScrollToPosition(0)

                ref.performSearch(implicitLink = data[i].videoId.toYoutubeUrl())
                ref.checklistDialog.dismiss()
            }

            setOnLongClickListener {
                val entry = data[i]

                VibrationUtil.weak()
                App.checklistedIds.remove(entry.videoId)

                CoroutineScope(Dispatchers.Default).launch {
                    checklist.remove(entry)
                }

                data.removeAt(i)
                notifyItemRemoved(i)

                if (data.isEmpty()) {
                    // If the last remaining item is removed, safely close the dialog
                    val ref = (activity as? MainActivity)
                    ref?.checklistDialog?.dismiss()
                }

                true
            }
        }

        Handler().post {
            /**
             * A title could have just 1 line of text, and so we should give it
             * a normal dimension, if compared to longer ones
             **/
            if (holder.title.lineCount == 1) {
                holder.title.height = activity.resources.getDimension(R.dimen.result_thumb_width).toInt()
            }

            holder.titleLayout.visibility = View.VISIBLE
        }
    }
}