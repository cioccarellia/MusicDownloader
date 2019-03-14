package com.andreacioccarelli.musicdownloader.ui.toast

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import es.dmoral.toasty.Toasty

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Suppress("unused")
object ToastUtil {
    private const val toastDuration = 5000

    fun success(text: String,
                @DrawableRes icon: Int = R.drawable.toast_check,
                duration: Int = toastDuration) =
        Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Green_A400),
            duration,
            true,
            true)
            .show()

    fun warn(text: String,
             @DrawableRes icon: Int = R.drawable.toast_warning,
             duration: Int = toastDuration) =
        Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Amber_600),
            duration,
            true,
            true)
            .show()

    fun error(text: String,
              @DrawableRes icon: Int = R.drawable.toast_error,
              duration: Int = toastDuration) =
        Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Red_600),
            duration,
            true,
            true)
            .show()

    fun info(text: String,
             @DrawableRes icon: Int = R.drawable.toast_info,
             duration: Int = toastDuration) =
        Toasty.custom(App.context,
            text,
            icon,
            ContextCompat.getColor(App.context, R.color.Blue_600),
            duration,
            true,
            true)
            .show()
}