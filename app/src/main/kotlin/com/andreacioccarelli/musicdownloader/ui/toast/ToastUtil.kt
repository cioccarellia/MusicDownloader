package com.andreacioccarelli.musicdownloader.ui.toast

import android.widget.Toast
import androidx.annotation.DrawableRes
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import es.dmoral.toasty.Toasty

/**
 * Designed and Developed by Andrea Cioccarelli
 */

@Suppress("unused")
object ToastUtil {
    private const val toastDuration = Toast.LENGTH_LONG

    fun success(text: String,
                @DrawableRes icon: Int = R.drawable.toast_check,
                duration: Int = toastDuration) =
        Toasty.custom(App.context,
            text,
            icon,
            R.color.Green_A400,
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
            R.color.Amber_600,
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
            R.color.Red_600,
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
            R.color.Blue_600,
            duration,
            true,
            true)
            .show()
}