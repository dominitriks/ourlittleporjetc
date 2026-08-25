package com.minitycoon.game.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.minitycoon.game.data.GameSaveData
import com.minitycoon.game.data.SaveManager

/**
 * Holds all game state for v1 (a single business). Income is derived from
 * [businessLevel] via [GameConfig] rather than stored separately, so the two
 * numbers can never drift out of sync.
 *
 * Idle income uses timestamp-based calculation: elapsed real time is
 * multiplied by the current income rate, both for the on-screen ticker while
 * the app is open and for the offline-earnings catch-up on launch. There is
 * no background service and no per-frame animation loop.
 */
class GameViewModel(private val saveManager: SaveManager) : ViewModel() {

    var money by mutableStateOf(0.0)
        private set

    var businessLevel by mutableStateOf(GameConfig.STARTING_LEVEL)
        private set

    var offlineEarningsMessage by mutableStateOf<String?>(null)
        private set

    private var lastTickTimeMillis: Long = System.currentTimeMillis()

    val incomePerSecond: Double
        get() = GameConfig.incomePerSecond(businessLevel)

    val upgradeCost: Double
        get() = GameConfig.upgradeCost(businessLevel)

    init {
        val saved = saveManager.load()
        money = saved.money
        businessLevel = saved.businessLevel
        applyOfflineEarnings(saved.lastSaveTimeMillis)
        lastTickTimeMillis = System.currentTimeMillis()
    }

    private fun applyOfflineEarnings(lastSaveTimeMillis: Long) {
        if (lastSaveTimeMillis <= 0L) return // first ever launch, nothing to catch up

        val elapsedSeconds = ((System.currentTimeMillis() - lastSaveTimeMillis) / 1000L)
            .coerceIn(0L, GameConfig.MAX_OFFLINE_SECONDS)

        if (elapsedSeconds < GameConfig.MIN_OFFLINE_SECONDS_TO_NOTIFY) return

        val earnings = elapsedSeconds * GameConfig.incomePerSecond(businessLevel)
        if (earnings > 0) {
            money += earnings
            offlineEarningsMessage = "While you were away, you earned $${formatWhole(earnings)}."
        }
    }

    fun dismissOfflineEarningsMessage() {
        offlineEarningsMessage = null
    }

    /** Advances the money counter based on wall-clock time elapsed since the last tick. */
    fun tick() {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastTickTimeMillis) / 1000.0
        if (elapsedSeconds > 0) {
            money += elapsedSeconds * incomePerSecond
            lastTickTimeMillis = now
        }
    }

    /** Manual "Collect" button: an instant bonus on top of passive income. */
    fun collect() {
        money += incomePerSecond
    }

    /** Returns true if the upgrade was purchased, so the UI can trigger one-shot visual feedback. */
    fun upgrade(): Boolean {
        val cost = upgradeCost
        if (money >= cost) {
            money -= cost
            businessLevel += 1
            persist()
            return true
        }
        return false
    }

    /** Call from the UI whenever the app is backgrounded so progress is never lost. */
    fun persist() {
        saveManager.save(
            GameSaveData(
                money = money,
                businessLevel = businessLevel,
                lastSaveTimeMillis = System.currentTimeMillis()
            )
        )
    }

    private fun formatWhole(value: Double): String = String.format("%,.0f", value)

    class Factory(private val saveManager: SaveManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(saveManager) as T
        }
    }
}
