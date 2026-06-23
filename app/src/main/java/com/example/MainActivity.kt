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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {

    companion object {
        // Compose-observable state to store unhandled UI or background crashes globally
        private val globalCrashState = mutableStateOf<Throwable?>(null)

        fun triggerManualCrash(throwable: Throwable) {
            globalCrashState.value = throwable
        }
    }

    override fun onResume() {
        super.onResume()
        // Offer App Open Ads on application foreground resume
        AdManager.showAppOpenAdIfAvailable(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Google AdMob Mobile Ads SDK and pre-load ads
        AdManager.initialize(this)
        
        enableEdgeToEdge()

        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        // 1. Intercept background and coroutine thread unhandled crashes
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            mainHandler.post {
                globalCrashState.value = throwable
            }
        }

        // 2. Intercept Main UI Loop crashes (prevents OS "force-close" dialog on recomposition or event crashes)
        mainHandler.post {
            while (true) {
                try {
                    android.os.Looper.loop()
                } catch (e: Throwable) {
                    globalCrashState.value = e
                }
            }
        }

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val viewModel = remember { GameViewModel(context) }
                val uiState by viewModel.uiState.collectAsState()
                
                val currentCrash = globalCrashState.value

                if (currentCrash != null) {
                    // Display our premium Global Error Boundary screen
                    CrashRecoveryScreen(
                        error = currentCrash,
                        onRestartLevel = {
                            globalCrashState.value = null
                            val currentLvl = GamePreferences(context).getCurrentLevel()
                            viewModel.loadLevel(currentLvl)
                        },
                        onGoHome = {
                            globalCrashState.value = null
                            viewModel.navigateTo(ScreenType.HOME_SCREEN)
                        },
                        onClearAllData = {
                            globalCrashState.value = null
                            context.getSharedPreferences("brain_trap_prefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .clear()
                                .apply()
                            // Force-restart application safely to restore pristine states
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                } else {
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
                        bottomBar = {
                            // Anchor standard adaptive banner ad at the bottom of the screens
                            AdBannerView(modifier = Modifier.fillMaxWidth())
                        }
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
                                                // Trigger App Open Ad upon splash transition
                                                AdManager.showAppOpenAdIfAvailable(this@MainActivity)
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
                                                AdManager.showInterstitial(this@MainActivity) {
                                                    viewModel.navigateTo(ScreenType.HOME_SCREEN)
                                                }
                                            },
                                            onNextLevel = {
                                                AdManager.showInterstitial(this@MainActivity) {
                                                    val next = uiState.currentLevelNumber + 1
                                                    viewModel.loadLevel(next)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            val languageCode by viewModel.language.collectAsState()

                            // Premium simulation interactive modal overlays
                            if (uiState.showSimulatedAdOffer) {
                                AdSimulationOverlay(
                                    gameState = uiState,
                                    languageCode = languageCode,
                                    onRestored = {
                                        AdManager.showRewardedAd(
                                            activity = this@MainActivity,
                                            onUserEarnedReward = {
                                                viewModel.claimAdForFullLives()
                                            },
                                            onAdClosed = {
                                                viewModel.closeAdOffer()
                                            }
                                        )
                                    },
                                    onBuyWithCoins = {
                                        viewModel.purchaseStoreItemByPoints("lives", 40)
                                        viewModel.closeAdOffer()
                                    },
                                    onClose = {
                                        viewModel.closeAdOffer()
                                    }
                                )
                            }

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
}

@Composable
fun CrashRecoveryScreen(
    error: Throwable,
    onRestartLevel: () -> Unit,
    onGoHome: () -> Unit,
    onClearAllData: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Capture complete layout exception details
    val writer = java.io.StringWriter()
    error.printStackTrace(java.io.PrintWriter(writer))
    val stackTraceString = writer.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Large 3D-styled Warn Comic Badge
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(Color(0xFFFFEBEE), RoundedCornerShape(20.dp))
                .border(2.dp, ComicBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🐙", fontSize = 40.sp)
        }

        // Header
        Text(
            text = "Puzzle Brain-Split Exception!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ComicBorder,
            textAlign = TextAlign.Center
        )

        Text(
            text = "একটি অপ্রত্যাশিত বাটন-বিভ্রাট ধাঁধার ইঞ্জিনকে সাময়িকভাবে থামিয়ে দিয়েছে! চিন্তা করবেন না, আমরা এই রানিং অবজেক্টটি সুরক্ষিত রেখেছি। নিচে থেকে দ্রুত সমাধান বেছে নিন:",
            fontSize = 12.5.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        // Stacktrace Code block card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.8.dp, ComicBorder, RoundedCornerShape(14.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "crash_exception_log.log",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .size(10.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.8.dp)
                    .background(ComicBorder)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = stackTraceString,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    color = Color(0xFFC62828),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ComicRecoveryButton(
                text = "লাফালাফি ছাড়াই পুনরায় চেষ্টা (Quick Restart)",
                baseColor = ComicGreen,
                onClick = onRestartLevel
            )

            ComicRecoveryButton(
                text = "মূল স্ক্রিনে ফিরে যান (Go Level Menu)",
                baseColor = ComicBlue,
                onClick = onGoHome
            )

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { onClearAllData() }
            ) {
                Text(
                    text = "⚠️ সমস্ত গেম ডেটা রিসেট করুন (Clear Config)",
                    fontSize = 11.5.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ComicRecoveryButton(
    text: String,
    baseColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp)
                .background(Color.Black, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 2.dp)
                .background(baseColor.darken(0.35f), RoundedCornerShape(12.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(baseColor, RoundedCornerShape(12.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
