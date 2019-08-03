package com.andreacioccarelli.musicdownloader.util

import android.content.Context
import android.os.Handler
import androidx.annotation.CheckResult
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.andreacioccarelli.musicdownloader.R
import com.andreacioccarelli.musicdownloader.ui.toast.ToastUtil
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.jetbrains.anko.find

object YoutubeUtil {

    private interface YoutubePlayerListener : YouTubePlayerListener {
        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {}
        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
        override fun onApiChange(youTubePlayer: YouTubePlayer) {}
        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {}
        override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
        override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
    }
    
    @CheckResult
    fun getVideoPreviewDialog(
        context: Context,
        id: String
    ): MaterialDialog {
        val dialog = MaterialDialog(context)
            .customView(R.layout.video_player_dialog, scrollable = false)

        val youtubePlayer = dialog.getCustomView().find<YouTubePlayerView>(R.id.player)

        youtubePlayer.enableAutomaticInitialization = false
        youtubePlayer.initialize(object: YoutubePlayerListener {
            override fun onReady(youTubePlayer: YouTubePlayer) { youTubePlayer.loadVideo(id, 0f) }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                VibrationUtil.strong()
                
                when (error) {
                    PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> ToastUtil.error("An parameter error has occurred while playing video")
                    PlayerConstants.PlayerError.HTML_5_PLAYER -> ToastUtil.error("YouTube HTML5 player error")
                    PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> ToastUtil.warn("Video not found")
                    PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER -> ToastUtil.warn("MusicDownloader can't play this type of video in the preview dialog")
                    PlayerConstants.PlayerError.UNKNOWN -> ToastUtil.error("An unknown error has occurred while playing video")
                }
            }
        }, true)

        dialog.setOnDismissListener {
            youtubePlayer.release()
        }
        
        Handler().post {
            dialog.view.setBackgroundResource(R.color.Grey_1000)
        }
    
    
        return dialog
    }
}