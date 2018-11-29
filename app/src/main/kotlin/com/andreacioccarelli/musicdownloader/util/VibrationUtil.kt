package com.andreacioccarelli.musicdownloader.util

import android.os.Build
import android.os.VibrationEffect
import com.andreacioccarelli.musicdownloader.App
import org.jetbrains.anko.vibrator

/**
 *  Designed and developed by Andrea Cioccarelli
 */

object VibrationUtil {
    private const val VIBRATION_WEAK = 25L
    private const val AMPLITUDE_WEAK = 100

    fun weak() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.instance.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_WEAK, AMPLITUDE_WEAK))
        } else {
            App.instance.baseContext.vibrator.vibrate(VIBRATION_WEAK)
        }
    }

    private const val VIBRATION_MEDIUM = 50L
    private const val AMPLITUDE_MEDIUM = 180

    fun medium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.instance.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MEDIUM, AMPLITUDE_MEDIUM))
        } else {
            App.instance.baseContext.vibrator.vibrate(VIBRATION_MEDIUM)
        }
    }

    private const val VIBRATION_STRONG = 100L
    private const val AMPLITUDE_STRONG = 255

    fun strong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            App.instance.baseContext.vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_STRONG, AMPLITUDE_STRONG))
        } else {
            App.instance.baseContext.vibrator.vibrate(VIBRATION_STRONG)
        }
    }
}