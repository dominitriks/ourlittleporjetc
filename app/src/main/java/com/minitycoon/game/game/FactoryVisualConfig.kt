package com.minitycoon.game.game

/**
 * Maps a business's level to a small integer "visual stage" (1..STAGE_COUNT).
 * The factory's Compose drawing (see ui/visuals/FactoryVisual.kt) picks its
 * shape/size/materials/detail count purely from this stage number, so
 * rebalancing STAGE_COUNT or the thresholds is a one-file change.
 *
 * Future business types (shop, farm, bank, ...) should follow the same
 * pattern: one small "stage-for-level" config object here, one Canvas-based
 * visual composable in ui/visuals/ that reads the art bible in
 * ui/theme/Color.kt. Nothing outside this file and FactoryVisual.kt hard-codes
 * "factory" stage logic, so adding a business type never requires touching it.
 */
object FactoryVisualConfig {
    const val STAGE_COUNT = 5

    fun stageForLevel(level: Int): Int = when {
        level <= 5 -> 1   // Small industrial building
        level <= 12 -> 2  // Bigger factory + extra machinery
        level <= 22 -> 3  // Expanded production zone + second floor
        level <= 35 -> 4  // Large modern factory + more machinery
        else -> 5         // Large high-tech production base
    }
}
