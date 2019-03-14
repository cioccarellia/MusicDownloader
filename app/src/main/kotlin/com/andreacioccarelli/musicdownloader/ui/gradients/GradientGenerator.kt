package com.andreacioccarelli.musicdownloader.ui.gradients

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
                intArrayOf(ContextCompat.getColor(App.context.applicationContext, startColor),
                        ContextCompat.getColor(App.context.baseContext, endColor)))
        return gd.apply {
            cornerRadius = radius
        }
    }

    fun random(): Drawable {
        val colorList = listOf(
                R.color.Amber_600, R.color.Amber_700, R.color.Amber_800,
                R.color.Amber_900, R.color.Amber_A400, R.color.Amber_A700,

                R.color.Orange_600, R.color.Orange_700, R.color.Orange_800,
                R.color.Orange_900, R.color.Orange_A400, R.color.Orange_A700)

        val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(ContextCompat.getColor(App.context.applicationContext, colorList.random()),
                        ContextCompat.getColor(App.context.baseContext, colorList.random())))
        return gd.apply {
            cornerRadius = 0F
        }
    }

    val errorGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.context.applicationContext, R.color.Red_400),
                            ContextCompat.getColor(App.context.baseContext, R.color.Red_A700)))
            return gd.apply {
                cornerRadius = 0F
            }
        }


    val appThemeGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.context.applicationContext, R.color.Yellow_700),
                            ContextCompat.getColor(App.context.baseContext, R.color.Amber_900)))
            return gd.apply {
                cornerRadius = 0F
            }
        }

    val successGradient: Drawable
        get() {
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ContextCompat.getColor(App.context.applicationContext, R.color.Green_A400),
                            ContextCompat.getColor(App.context.baseContext, R.color.Teal_A700)))
            return gd.apply {
                cornerRadius = 0F
            }
        }
}