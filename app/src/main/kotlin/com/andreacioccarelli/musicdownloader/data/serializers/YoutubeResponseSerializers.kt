package com.andreacioccarelli.musicdownloader.data.serializers

/**
 *  Designed and developed by Andrea Cioccarelli
 */

data class YoutubeSearchResponse(
        val kind: String,
        val etag: String,
        val nextPageToken: String,
        val regionCode: String,
        val pageInfo: PageInfo,
        val items: List<Result>
)

data class VideoId (
        val kind: String,
        val videoId: String
)

data class PageInfo (
        val totalResults: Int,
        val resultsPerPage: Int
)

data class Result (
        val kind: String,
        val etag: String,
        val id: VideoId,
        val snippet: Snippet
)

data class Thumbnails(
        val default: Thumb,
        val medium: Thumb,
        val high: Thumb
)

data class Thumb(
        val url: String,
        val width: Int,
        val height: Int
)

data class Snippet (
        val publishedAt: String,
        val channelId: String,
        val title: String,
        val description: String,
        val thumbnails: Thumbnails,
        val channelTitle: String,
        val liveBroadcastContent: String
)