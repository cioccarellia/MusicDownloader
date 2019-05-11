package com.andreacioccarelli.musicdownloader.ui.holders

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.andreacioccarelli.musicdownloader.R

class ResultCardViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var card: CardView = v.findViewById(R.id.card)
    var icon: ImageView = v.findViewById(R.id.icon)
    var titleLayout: RelativeLayout = v.findViewById(R.id.titleLayout)
    var title: TextView = v.findViewById(R.id.title)
}