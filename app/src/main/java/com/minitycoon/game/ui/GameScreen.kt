package com.minitycoon.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.minitycoon.game.game.GameConfig
import com.minitycoon.game.game.GameViewModel
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MINI TYCOON",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "💰 ${formatMoney(viewModel.money)}",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "💵 ${formatMoney(viewModel.incomePerSecond)} / sec",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏭 Factory", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Level ${viewModel.businessLevel}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.upgrade() },
                            enabled = viewModel.money >= viewModel.upgradeCost,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("UPGRADE", fontWeight = FontWeight.Bold)
                                Text(
                                    "Cost: ${formatMoney(viewModel.upgradeCost)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.collect() },
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Text("COLLECT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatMoney(value: Double): String = "$" + String.format(Locale.US, "%,.0f", value)
