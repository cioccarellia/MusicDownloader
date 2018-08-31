package com.andreacioccarelli.musicdownloader.data.serializers

/**
 * Created by andrea on 2018/Aug.
 * Part of the package com.andreacioccarelli.musicdownloader.data.serializers
 */

data class UpdateCheck(
        val versionCode: Int,
        val versionName: String,
        val changelog: String,
        val downloadInfo: DownloadInfo
)

data class DownloadInfo(val useBundledUpdateLink: Boolean, val updateLink: String?)