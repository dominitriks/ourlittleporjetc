package com.minitycoon.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.minitycoon.game.game.FactoryVisualConfig
import com.minitycoon.game.game.GameConfig
import com.minitycoon.game.game.GameViewModel
import com.minitycoon.game.ui.components.GameButton
import com.minitycoon.game.ui.components.MoneyHud
import com.minitycoon.game.ui.components.formatMoney
import com.minitycoon.game.ui.theme.ButtonCollectBottom
import com.minitycoon.game.ui.theme.ButtonCollectTop
import com.minitycoon.game.ui.theme.ButtonTextLight
import com.minitycoon.game.ui.theme.ButtonUpgradeBottom
import com.minitycoon.game.ui.theme.ButtonUpgradeDisabledBottom
import com.minitycoon.game.ui.theme.ButtonUpgradeDisabledTop
import com.minitycoon.game.ui.theme.ButtonUpgradeTop
import com.minitycoon.game.ui.theme.MoneyGreen
import com.minitycoon.game.ui.theme.PanelBorderHighlight
import com.minitycoon.game.ui.theme.PanelNavyBottom
import com.minitycoon.game.ui.theme.PanelNavyTop
import com.minitycoon.game.ui.theme.UpgradeOrange
import com.minitycoon.game.ui.visuals.FactoryVisual
import com.minitycoon.game.ui.visuals.FloatingText
import com.minitycoon.game.ui.visuals.FloatingTextItem
import com.minitycoon.game.ui.visuals.SceneBackground
import kotlinx.coroutines.delay

@Composable
fun GameScreen(viewModel: GameViewModel) {
    // Timestamp-based ticker: recomputes elapsed real time each cycle instead
    // of relying on a background service or a tight per-frame loop.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.tick()
            delay(GameConfig.UI_TICK_INTERVAL_MILLIS)
        }
    }

    // Persist whenever the app leaves the foreground (covers Back button and Home).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.persist()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var floatingTexts by remember { mutableStateOf(listOf<FloatingTextItem>()) }
    var nextFloatingId by remember { mutableStateOf(0L) }
    var glowTrigger by remember { mutableStateOf(0) }
    var moneyPulseTrigger by remember { mutableStateOf(0) }

    fun spawnFloatingText(text: String, color: Color) {
        nextFloatingId += 1
        floatingTexts = floatingTexts + FloatingTextItem(nextFloatingId, text, color)
    }

    viewModel.offlineEarningsMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissOfflineEarningsMessage() },
            confirmButton = {
                Button(onClick = { viewModel.dismissOfflineEarningsMessage() }) {
                    Text("Nice!")
                }
            },
            title = { Text("Welcome back") },
            text = { Text(message) }
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(innerPadding)
        ) {
            MoneyHud(
                money = viewModel.money,
                incomePerSecond = viewModel.incomePerSecond,
                pulseTrigger = moneyPulseTrigger,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SceneBackground(modifier = Modifier.fillMaxSize())

                FactoryVisual(
                    stage = FactoryVisualConfig.stageForLevel(viewModel.businessLevel),
                    glowTrigger = glowTrigger,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-36).dp)
                ) {
                    floatingTexts.forEach { item ->
                        key(item.id) {
                            FloatingText(
                                item = item,
                                onFinished = { id ->
                                    floatingTexts = floatingTexts.filterNot { it.id == id }
                                }
                            )
                        }
                    }
                }
            }

            BottomFactoryPanel(
                level = viewModel.businessLevel,
                upgradeCost = viewModel.upgradeCost,
                canAfford = viewModel.money >= viewModel.upgradeCost,
                onUpgrade = {
                    val leveledUp = viewModel.upgrade()
                    if (leveledUp) {
                        glowTrigger += 1
                        spawnFloatingText("+LEVEL", UpgradeOrange)
                    }
                },
                onCollect = {
                    val amount = viewModel.incomePerSecond
                    viewModel.collect()
                    moneyPulseTrigger += 1
                    spawnFloatingText("+${formatMoney(amount)}", MoneyGreen)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun BottomFactoryPanel(
    level: Int,
    upgradeCost: Double,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                drawRect(brush = Brush.verticalGradient(listOf(PanelNavyTop, PanelNavyBottom)))
                drawRect(
                    color = PanelBorderHighlight,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height * 0.04f)
                )
            }
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "FACTORY",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ButtonTextLight
            )
            Text(
                "Level $level",
                style = MaterialTheme.typography.bodyLarge,
                color = ButtonTextLight.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GameButton(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    topColor = ButtonUpgradeTop,
                    bottomColor = ButtonUpgradeBottom,
                    disabledTopColor = ButtonUpgradeDisabledTop,
                    disabledBottomColor = ButtonUpgradeDisabledBottom,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text("UPGRADE", fontWeight = FontWeight.Bold, color = ButtonTextLight)
                    Text(formatMoney(upgradeCost), color = ButtonTextLight.copy(alpha = 0.9f))
                }

                GameButton(
                    onClick = onCollect,
                    enabled = true,
                    topColor = ButtonCollectTop,
                    bottomColor = ButtonCollectBottom,
                    disabledTopColor = ButtonCollectTop,
                    disabledBottomColor = ButtonCollectBottom,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text("COLLECT", fontWeight = FontWeight.Bold, color = ButtonTextLight)
                }
            }
        }
    }
}
