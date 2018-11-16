package com.andreacioccarelli.musicdownloader.data.serializers

import com.google.gson.annotations.SerializedName

/**
 *  Designed and developed by Andrea Cioccarelli
 */

data class DirectLinkResponse(
        val state: String,
        @SerializedName("video_id") val videoId: String,
        val title: String,
        val format: String,
        val download: String,
        val percentage: String,
        val reason: String)