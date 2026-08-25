package com.minitycoon.game.game

/**
 * Maps a business's level to a small integer "visual stage" (1..STAGE_COUNT).
 * The factory's Compose drawing (see ui/visuals/FactoryVisual.kt) picks its
 * shape/size/colors purely from this stage number, so raising STAGE_COUNT or
 * moving the thresholds is a one-file change.
 *
 * Future business types (shop, farm, bank, ...) should follow the same
 * pattern: one small "stage-for-level" config object here, one Canvas-based
 * visual composable in ui/visuals/. Nothing below hard-codes "factory" logic
 * outside of this file and FactoryVisual.kt, so adding a business type never
 * requires touching this one.
 */
object FactoryVisualConfig {
    const val STAGE_COUNT = 4

    fun stageForLevel(level: Int): Int = when {
        level <= 5 -> 1
        level <= 10 -> 2
        level <= 20 -> 3
        else -> 4
    }
}
