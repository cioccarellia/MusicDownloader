package com.andreacioccarelli.musicdownloader

import android.app.Application
import androidx.room.Room
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.musicdownloader.constants.FILE
import com.andreacioccarelli.musicdownloader.constants.KEY
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDatabase
import com.andreacioccarelli.musicdownloader.extensions.Delegates
import com.andreacioccarelli.musicdownloader.ui.typeface.Typefaces
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import kotlin.LazyThreadSafetyMode.NONE

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class App : Application() {

    companion object {
        var context by Delegates.ctx<Application>()
        val client by lazy(NONE) { OkHttpClient() }
        val prefs by lazy { CryptoPrefs(context, FILE, KEY, shouldEncrypt = false) }

        val checklist by lazy {
            val db = Room.databaseBuilder(
                context,
                ChecklistDatabase::class.java, "checklist"
            )

            with(db) {
                allowMainThreadQueries()
                fallbackToDestructiveMigration()
            }

            db.build().checklistDao()
        }

        val checklistedIds by lazy {
            mutableListOf<String>().also {
                it.addAll(
                    checklist.getAll().map { video ->
                        video.videoId
                    }
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                .setDefaultFontPath(Typefaces.MEDIUM)
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        CoroutineScope(Dispatchers.Default).launch {
            ::checklist.get()
            ::checklistedIds.get()
        }
    }
}