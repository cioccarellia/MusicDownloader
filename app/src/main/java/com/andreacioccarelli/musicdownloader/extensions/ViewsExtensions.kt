package com.andreacioccarelli.musicdownloader.extensions

import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import es.dmoral.toasty.Toasty

/**
 * Designed and developed by Andrea Cioccarelli
 */

fun CheckBox.switch() {
    isChecked = !isChecked
}

fun success(text: String, @DrawableRes icon: Int = R.drawable.ic_check_white_48dp) {
    Toasty.custom(App.instance, text, App.instance.getDrawable(icon), ContextCompat.getColor(App.instance, R.color.Green_A400), Toast.LENGTH_SHORT, true, true)
            .show()
}