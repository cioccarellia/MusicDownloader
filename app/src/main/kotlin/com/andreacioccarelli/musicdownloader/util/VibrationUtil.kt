package com.andreacioccarelli.musicdownloader.util

import android.os.Build
import android.os.VibrationEffect
import com.andreacioccarelli.musicdownloader.App
import org.jetbrains.anko.vibrator

/**
 *  Designed and developed by Andrea Cioccarelli
 */

@Suppress("unused")
object VibrationUtil {
    private const val VIBRATION_WEAK = 20L
    private const val AMPLITUDE_WEAK = 100

    fun weak() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.context.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_WEAK, AMPLITUDE_WEAK))
        } else {
            App.context.baseContext.vibrator.vibrate(VIBRATION_WEAK)
        }
    }

    private const val VIBRATION_MEDIUM = 40L
    private const val AMPLITUDE_MEDIUM = 150

    fun medium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.context.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MEDIUM, AMPLITUDE_MEDIUM))
        } else {
            App.context.baseContext.vibrator.vibrate(VIBRATION_MEDIUM)
        }
    }

    private const val VIBRATION_STRONG = 80L
    private const val AMPLITUDE_STRONG = 255

    fun strong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.context.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_STRONG, AMPLITUDE_STRONG))
        } else {
            App.context.baseContext.vibrator.vibrate(VIBRATION_STRONG)
        }
    }
}