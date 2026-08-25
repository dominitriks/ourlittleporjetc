package com.minitycoon.game.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.minitycoon.game.game.FactoryVisualConfig
import com.minitycoon.game.game.GameConfig
import com.minitycoon.game.game.GameViewModel
import com.minitycoon.game.ui.theme.MoneyGreen
import com.minitycoon.game.ui.theme.UpgradeOrange
import com.minitycoon.game.ui.visuals.FactoryVisual
import com.minitycoon.game.ui.visuals.FloatingText
import com.minitycoon.game.ui.visuals.FloatingTextItem
import com.minitycoon.game.ui.visuals.SceneBackground
import kotlinx.coroutines.delay
import java.util.Locale

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
            TopMoneyBar(
                money = viewModel.money,
                incomePerSecond = viewModel.incomePerSecond,
                pulseTrigger = moneyPulseTrigger,
                modifier = Modifier
                    .fillMaxWidth()
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

            BottomFactoryCard(
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
private fun TopMoneyBar(
    money: Double,
    incomePerSecond: Double,
    pulseTrigger: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger > 0) {
            scale.animateTo(1.12f, tween(120))
            scale.animateTo(1f, tween(180))
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💰 ${formatMoney(money)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MoneyGreen,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            )
            Text(
                text = "💵 ${formatMoney(incomePerSecond)}/s",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BottomFactoryCard(
    level: Int,
    upgradeCost: Double,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Factory", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PressableButton(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    containerColor = UpgradeOrange,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UPGRADE", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Cost: ${formatMoney(upgradeCost)}", color = Color.White)
                    }
                }

                PressableButton(
                    onClick = onCollect,
                    enabled = true,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("COLLECT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/** Shared press-scale feedback for the game's primary action buttons. */
@Composable
private fun PressableButton(
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "buttonScale")

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f)
        ),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

private fun formatMoney(value: Double): String = "$" + String.format(Locale.US, "%,.0f", value)
