package com.andreacioccarelli.musicdownloader.data.enums

/**
 * Designed and Developed by Andrea Cioccarelli
 */

enum class FailedConversionError {
    /**
     * The url is wrong, the id is malformed or the content isn't parsable
     * */
    MALFORMED_URL,

    /**
     * The video is private, the user doesn't have access to it, the video
     * requires a purchase (E.g. films, pro content) or an error occurred
     * while fetching video info
     * */
    UNADDRESSABLE_VIDEO,

    /**
     * The video length exceeds 3 hours
     * */
    VIDEO_LENGTH,

    /**
     * An exception was raised while downloading a video list
     * */
    BATCH_FAILED,

    /**
     * An unknown error was thrown (an unknown repose was provided by the server)
     * */
    UNKNOWN_ERROR,
    NO_ERROR
}