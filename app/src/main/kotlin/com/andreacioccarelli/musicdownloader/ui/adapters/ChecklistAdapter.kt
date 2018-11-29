package com.andreacioccarelli.musicdownloader.ui.adapters

import android.app.Activity
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.ui.activities.MainActivity
import com.andreacioccarelli.musicdownloader.util.ChecklistStore
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.find

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class ChecklistAdapter(private val data: MutableList<Pair<String, String>>, private val activity: Activity) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        Glide.with(activity)
                .load(data[holder.adapterPosition].second)
                .thumbnail(0.1F)
                .into(holder.icon)
        
        holder.title.text = data[holder.adapterPosition].first

        with(holder.card) {
            setOnClickListener {
                val ref = (activity as MainActivity)

                val search = ref.find<TextView>(R.id.search)
                val fab = ref.find<FloatingActionButton>(R.id.fab)
                val rv = ref.find<RecyclerView>(R.id.resultsRecyclerView)

                search.text = data[holder.adapterPosition].first
                fab.performClick()
                rv.smoothScrollToPosition(0)
                ref.checklistDialog.dismiss()
            }

            setOnLongClickListener {
                if (data.size == 1) {
                    val ref = (activity as MainActivity)
                    ref.checklistDialog.dismiss()
                }

                ChecklistStore.remove(activity, data[holder.adapterPosition].first)
                data.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                true
            }
        }

        Handler().post {
            if (holder.title.lineCount == 1) {
                holder.title.height = activity.resources.getDimension(R.dimen.result_thumb_width).toInt()
            }

            holder.titleLayout.visibility = View.VISIBLE
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var icon: ImageView = v.findViewById(R.id.icon)
        var titleLayout: RelativeLayout = v.findViewById(R.id.titleLayout)
        var title: TextView = v.findViewById(R.id.title)
        var card: CardView = v.findViewById(R.id.card)
    }
}