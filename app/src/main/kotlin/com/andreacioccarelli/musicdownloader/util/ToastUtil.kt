package com.andreacioccarelli.musicdownloader.util

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import es.dmoral.toasty.Toasty

/**
 * Designed and Developed by Andrea Cioccarelli
 */


object ToastUtil {
    fun success(text: String,
                @DrawableRes icon: Int = R.drawable.toast_check,
                duration: Int = 5000) = Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Green_A400),
            duration,
            true,
            true)
            .show()

    fun warn(text: String,
             @DrawableRes icon: Int = R.drawable.toast_warning,
             duration: Int = 5000) = Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Amber_600),
            duration,
            true,
            true)
            .show()

    fun error(text: String,
              @DrawableRes icon: Int = R.drawable.toast_error,
              duration: Int = 5000) = Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Red_600),
            duration,
            true,
            true)
            .show()

    fun info(text: String,
             @DrawableRes icon: Int = R.drawable.toast_info,
             duration: Int = 5000) = Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Blue_600),
            duration,
            true,
            true)
            .show()
}