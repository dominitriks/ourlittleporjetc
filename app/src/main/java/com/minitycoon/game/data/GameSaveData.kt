package com.minitycoon.game.data

/**
 * Persisted game state. Kept intentionally small for v1 — as new systems
 * (more businesses, prestige, achievements, etc.) are added, extend this
 * data class and bump SaveManager's storage accordingly.
 */
data class GameSaveData(
    val money: Double,
    val businessLevel: Int,
    val lastSaveTimeMillis: Long
)
