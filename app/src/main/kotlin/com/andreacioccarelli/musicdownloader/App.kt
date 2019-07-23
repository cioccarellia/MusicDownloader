package com.andreacioccarelli.musicdownloader

import android.app.Application
import androidx.room.Room
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.andreacioccarelli.musicdownloader.constants.FILE
import com.andreacioccarelli.musicdownloader.constants.KEY
import com.andreacioccarelli.musicdownloader.data.checklist.ChecklistDatabase
import com.andreacioccarelli.musicdownloader.extensions.Delegates
import com.andreacioccarelli.musicdownloader.ui.typeface.Typefaces
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 *  Designed and developed by Andrea Cioccarelli
 */

class App : Application() {

    companion object {
        var context by Delegates.singleValue<Application>()
        val prefs by lazy { CryptoPrefs(context.applicationContext, FILE, KEY, false) }

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
            mutableListOf<String>()
                .also {
                    it.addAll(
                        checklist.getAll().map { it.videoId }
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
    }
}