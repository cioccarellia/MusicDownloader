package com.andreacioccarelli.musicdownloader.ui.drawables

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object GradientGenerator {
    fun make(radius: Float, @ColorRes startColor: Int, @ColorRes endColor: Int): Drawable {
        val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(ContextCompat.getColor(App.instance.applicationContext, startColor),
                        ContextCompat.getColor(App.instance.baseContext, endColor)))
        gd.cornerRadius = radius
        return gd
    }

    val errorGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.instance.applicationContext, R.color.Red_400),
                            ContextCompat.getColor(App.instance.baseContext, R.color.Red_A700)))
            gd.cornerRadius = 0F
            return gd
        }


    val appThemeGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.instance.applicationContext, R.color.Yellow_700),
                            ContextCompat.getColor(App.instance.baseContext, R.color.Amber_900)))
            gd.cornerRadius = 0F
            return gd
        }

    val successGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.instance.applicationContext, R.color.Green_A400),
                            ContextCompat.getColor(App.instance.baseContext, R.color.Teal_A700)))
            gd.cornerRadius = 0F
            return gd
        }
}