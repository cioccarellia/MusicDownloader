package com.andreacioccarelli.musicdownloader.data.requests

import com.andreacioccarelli.musicdownloader.constants.UPDATE_URL
import okhttp3.Request

/**
 * Created by andrea on 2018/Aug.
 * Part of the package com.andreacioccarelli.musicdownloader.data.requests
 */

object UpdateRequestBuilder {
    fun get(): Request = Request.Builder()
            .url(UPDATE_URL)
            .build()
}