package com.andreacioccarelli.musicdownloader.data.serializers

import com.google.gson.annotations.SerializedName

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.data.serializers
 */

data class DirectLinkResponse(
        val state: String,
        @SerializedName("video_id") val videoId: String,
        val title: String,
        val format: String,
        val download: String,
        val percentage: String,
        val reason: String)