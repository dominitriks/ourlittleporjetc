package com.minitycoon.game.data

import android.content.Context
import android.content.SharedPreferences
import com.minitycoon.game.game.GameConfig

/**
 * Local-only save system backed by SharedPreferences. No database, no network.
 */
class SaveManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): GameSaveData {
        val hasSave = prefs.contains(KEY_MONEY)
        return if (!hasSave) {
            GameSaveData(
                money = GameConfig.STARTING_MONEY,
                businessLevel = GameConfig.STARTING_LEVEL,
                lastSaveTimeMillis = 0L // 0 = no previous session, skip offline earnings
            )
        } else {
            GameSaveData(
                money = prefs.getString(KEY_MONEY, null)?.toDoubleOrNull() ?: GameConfig.STARTING_MONEY,
                businessLevel = prefs.getInt(KEY_LEVEL, GameConfig.STARTING_LEVEL),
                lastSaveTimeMillis = prefs.getLong(KEY_LAST_TIME, 0L)
            )
        }
    }

    fun save(data: GameSaveData) {
        // Money is stored as a String (not Float) to avoid precision loss once
        // idle numbers grow large.
        prefs.edit()
            .putString(KEY_MONEY, data.money.toString())
            .putInt(KEY_LEVEL, data.businessLevel)
            .putLong(KEY_LAST_TIME, data.lastSaveTimeMillis)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "mini_tycoon_save"
        const val KEY_MONEY = "money"
        const val KEY_LEVEL = "business_level"
        const val KEY_LAST_TIME = "last_save_time"
    }
}
