package com.starqr.starcatcher

import android.content.Context
import android.content.SharedPreferences

object GameProgress {
    private const val PREFS_NAME = "StarCatcherPrefs"
    private const val KEY_HIGHEST_LEVEL = "highestLevel"
    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getHighestLevelUnlocked(): Int {
        return prefs.getInt(KEY_HIGHEST_LEVEL, 1) // По умолчанию открыт только 1-й уровень
    }

    fun levelCompleted(unlockedLevel: Int) {
        val currentHighest = getHighestLevelUnlocked()
        if (unlockedLevel > currentHighest) {
            prefs.edit().putInt(KEY_HIGHEST_LEVEL, unlockedLevel).apply()
        }
    }
}