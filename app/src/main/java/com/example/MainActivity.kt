package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val viewModel = remember { GameViewModel(context) }
                val uiState by viewModel.uiState.collectAsState()



                // Register standard system back handlers for game routing
                BackHandler(enabled = true) {
                    when (uiState.screenType) {
                        ScreenType.SPLASH_SCREEN -> finish()
                        ScreenType.HOME_SCREEN -> finish()
                        ScreenType.LEVEL_SELECT_SCREEN -> viewModel.navigateTo(ScreenType.HOME_SCREEN)
                        ScreenType.GAME_SCREEN -> viewModel.navigateTo(ScreenType.HOME_SCREEN)
                        ScreenType.RESULT_SCREEN -> viewModel.navigateTo(ScreenType.LEVEL_SELECT_SCREEN)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBg,

                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Routing Animations Switch
                        AnimatedContent(
                            targetState = uiState.screenType,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "MainScreenRoutingTransition"
                        ) { screenType ->
                            when (screenType) {
                                ScreenType.SPLASH_SCREEN -> {
                                    SplashScreen(
                                        onNavigateToHome = {
                                            viewModel.navigateTo(ScreenType.HOME_SCREEN)
                                        }
                                    )
                                }
                                ScreenType.HOME_SCREEN -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onPlayClicked = {
                                            viewModel.navigateTo(ScreenType.GAME_SCREEN)
                                        },
                                        onNavigateToLevels = {
                                            viewModel.navigateTo(ScreenType.LEVEL_SELECT_SCREEN)
                                        }
                                    )
                                }
                                ScreenType.LEVEL_SELECT_SCREEN -> {
                                    LevelSelectScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = {
                                            viewModel.navigateTo(ScreenType.HOME_SCREEN)
                                        },
                                        onLevelSelected = { selectedLvl ->
                                            viewModel.loadLevel(selectedLvl)
                                        }
                                    )
                                }
                                ScreenType.GAME_SCREEN -> {
                                    GameScreen(
                                        viewModel = viewModel,
                                        gameState = uiState,
                                        onNavigateBack = {
                                            viewModel.navigateTo(ScreenType.HOME_SCREEN)
                                        }
                                    )
                                }
                                ScreenType.RESULT_SCREEN -> {
                                    ResultScreen(
                                        viewModel = viewModel,
                                        gameState = uiState,
                                        onResetHome = {
                                            viewModel.navigateTo(ScreenType.HOME_SCREEN)
                                        },
                                        onNextLevel = {
                                            val next = uiState.currentLevelNumber + 1
                                            viewModel.loadLevel(next)
                                        }
                                    )
                                }
                            }
                        }

                        // Premium simulation interactive modal overlays
                        if (uiState.showSimulatedAdOffer) {
                            AdSimulationOverlay(
                                gameState = uiState,
                                onRestored = {
                                    viewModel.claimAdForFullLives()
                                },
                                onClose = {
                                    viewModel.closeAdOffer()
                                }
                            )
                        }

                        val languageCode by viewModel.language.collectAsState()
                        if (uiState.isAdPlaying) {
                            AdPlayerScreen(gameState = uiState, languageCode = languageCode)
                        }

                        // Custom glassmorphic bottom toast banners
                        uiState.showToastMsg?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 60.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(SurfaceBlue, RoundedCornerShape(12.dp))
                                        .border(1.dp, LightBlueAccent, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                        .clickable { viewModel.dismissToast() }
                                ) {
                                    Text(
                                        text = msg,
                                        color = SoftGrayText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Auto dismiss toast after 2.5 seconds
                            LaunchedEffect(msg) {
                                delay(2500)
                                viewModel.dismissToast()
                            }
                        }
                    }
                }
            }
        }
    }
}
