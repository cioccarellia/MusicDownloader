package com.andreacioccarelli.musicdownloader.ui.adapters

import android.app.Activity
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.extensions.switch
import com.andreacioccarelli.musicdownloader.ui.activities.MainActivity
import com.andreacioccarelli.musicdownloader.util.QueueStore
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class QueueAdapter(private val data: MutableList<Triple<String, String, Boolean>>, private val activity: Activity) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.queue_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.title.text = data[holder.adapterPosition].first

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            QueueStore.setChecked(data[holder.adapterPosition].first, isChecked)
        }

        with(holder.card) {
            setOnClickListener {
                holder.checkbox.switch()
            }

            setOnLongClickListener {
                if (data.size == 1) {
                    val ref = (activity as MainActivity)
                    ref.queueDialog.dismiss()
                }

                QueueStore.remove(activity, data[holder.adapterPosition].first)
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

        doAsync {
            val isChecked = QueueStore.get(activity)
                    .filter { it.first == holder.title.text }[0].third

            uiThread {
                holder.checkbox.isChecked = isChecked
            }
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var checkbox: CheckBox = v.findViewById(R.id.checkbox)
        var titleLayout: RelativeLayout = v.findViewById(R.id.titleLayout)
        var title: TextView = v.findViewById(R.id.title)
        var card: CardView = v.findViewById(R.id.card)
    }
}