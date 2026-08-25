package com.minitycoon.game.game

import kotlin.math.pow

/**
 * Single place to tune game balance. Change values here to rebalance
 * the whole game without touching UI or save/load logic.
 */
object GameConfig {

    // --- Starting state ---
    const val STARTING_MONEY: Double = 50.0
    const val STARTING_LEVEL: Int = 1

    // --- Income formula: income(level) = BASE_INCOME * INCOME_GROWTH^(level - 1) ---
    const val BASE_INCOME_PER_SECOND: Double = 1.0
    const val INCOME_GROWTH: Double = 1.12

    // --- Upgrade cost formula: cost(level) = BASE_UPGRADE_COST * UPGRADE_COST_GROWTH^(level - 1) ---
    const val BASE_UPGRADE_COST: Double = 50.0
    const val UPGRADE_COST_GROWTH: Double = 1.15

    // --- Offline earnings ---
    const val MAX_OFFLINE_SECONDS: Long = 8L * 60L * 60L // 8 hours cap
    const val MIN_OFFLINE_SECONDS_TO_NOTIFY: Long = 5L // avoid spamming the dialog on quick re-opens

    // --- UI tick rate (how often the on-screen money counter refreshes while app is open) ---
    const val UI_TICK_INTERVAL_MILLIS: Long = 200L

    fun incomePerSecond(level: Int): Double =
        BASE_INCOME_PER_SECOND * INCOME_GROWTH.pow(level - 1)

    fun upgradeCost(level: Int): Double =
        BASE_UPGRADE_COST * UPGRADE_COST_GROWTH.pow(level - 1)
}
