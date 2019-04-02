package com.andreacioccarelli.musicdownloader.data.serializers

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 *  Designed and developed by Andrea Cioccarelli
 */
data class DirectLinkResponse(
        @SerializedName("state")                        val state: String,
        @SerializedName("video_id")                     val videoId: String,
        @SerializedName("title")                        val title: String,
        @SerializedName("format")                       val format: String,
        @SerializedName("download")                     val download: String,
        @SerializedName("percentage")                   val percentage: String,
        @SerializedName("reason")                       val reason: String,
        @Expose(serialize = false, deserialize = false) var fileName: String
)