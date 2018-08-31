package com.andreacioccarelli.musicdownloader.ui.adapters

import android.app.Activity
import android.os.Handler
import android.support.v4.app.FragmentManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.data.serializers.Result
import com.andreacioccarelli.musicdownloader.data.serializers.YoutubeSearchResponse
import com.andreacioccarelli.musicdownloader.ui.fragments.BottomSheetChooser
import com.bumptech.glide.Glide

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.ui.adapters
 */
class ResultsAdapter(response: YoutubeSearchResponse, private val activity: Activity, private val fm: FragmentManager) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    val data by lazy { ArrayList<Result>() }

    init {
        data.clear()
        data.addAll(response.items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        Glide.with(activity)
                .load(data[i].snippet.thumbnails.medium.url)
                .thumbnail(0.1F)
                .into(holder.icon)

        holder.title.text = data[i].snippet.title

        holder.card.setOnClickListener {
            val bottomSheetFragment = BottomSheetChooser(data[i])
            bottomSheetFragment.show(fm, bottomSheetFragment.tag)
        }

        Handler().post {
            if (holder.title.lineCount == 1) {
                holder.title.height = activity.resources.getDimension(R.dimen.result_thumb_width).toInt()
            }

            holder.titleLayout.visibility = View.VISIBLE
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var card: CardView = v.findViewById(R.id.card)
        var icon: ImageView = v.findViewById(R.id.icon)
        var titleLayout: RelativeLayout = v.findViewById(R.id.titleLayout)
        var title: TextView = v.findViewById(R.id.title)
    }
}