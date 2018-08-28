package com.andreacioccarelli.musicdownloader.ui.drawables

import com.andreacioccarelli.musicdownloader.App
import com.andreacioccarelli.musicdownloader.R
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

/**
 * Created by andrea on 2018/Aug.
 * Part of the package andreacioccarelli.musicdownloader.ui.drawables
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


    val infoGradient: Drawable
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