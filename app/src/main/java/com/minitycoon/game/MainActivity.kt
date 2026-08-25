package com.minitycoon.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minitycoon.game.data.SaveManager
import com.minitycoon.game.game.GameViewModel
import com.minitycoon.game.ui.GameScreen
import com.minitycoon.game.ui.theme.MiniTycoonTheme
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniTycoonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val saveManager = remember { SaveManager(applicationContext) }
                    val viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory(saveManager))
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }
}
