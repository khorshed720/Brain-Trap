package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info

// ==========================================
// 1. SPLASH SCREEN (PULSING LOGO + GLOW)
// ==========================================
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    val glowAlpha by rememberInfiniteTransition(label = "GlowTransition").animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // Premium splash duration
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, SurfaceBlue, Color(0xFF0F0B24))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative retro grid background in local Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            val lineColor = GrayLine.copy(alpha = 0.12f)
            // Vertical grids
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 1.5f
                )
                x += gridStep
            }
            // Horizontal grids
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1.5f
                )
                y += gridStep
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .background(
                        color = Color(0xFF13132B),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        2.5.dp,
                        Brush.linearGradient(listOf(PrimaryRed, LightBlueAccent)),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(PrimaryRed.copy(alpha = glowAlpha * 0.35f), Color.Transparent)
                            )
                        )
                )

                // Custom Canvas-drawn elegant Brain inside a Trap symbol (Matches custom launcher foreground)
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawBrainTrapSymbol(this, PrimaryRed, LightBlueAccent, SoftGrayText)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Game Heading
            Text(
                text = "BRAIN TRAP",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SoftGrayText,
                letterSpacing = 4.sp
            )

            // Dynamic Tagline
            Text(
                text = "TRICKY PUZZLES & MIND TRAPS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryRed,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Double Trap Quiz & Logic Game",
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = LightBlueAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            CircularProgressIndicator(
                color = PrimaryRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// Draw internal custom vector-brain symbol dynamically to ensure high graphics performance
private fun drawBrainTrapSymbol(scope: DrawScope, primaryColor: Color, accentColor: Color, textColor: Color) {
    val w = scope.size.width
    val h = scope.size.height

    // Draw outer trap square
    scope.drawRect(
        color = primaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f),
        size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.6f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )

    // Draw brain bulb core
    scope.drawCircle(
        color = textColor,
        radius = w * 0.18f,
        center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f)
    )

    // Light filament lightning bolt inside bulb
    val boltPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(w * 0.5f, h * 0.33f)
        lineTo(w * 0.44f, h * 0.46f)
        lineTo(w * 0.56f, h * 0.46f)
        lineTo(w * 0.5f, h * 0.58f)
    }
    scope.drawPath(
        path = boltPath,
        color = primaryColor,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )

    // Lower thread socket
    scope.drawRect(
        color = accentColor,
        topLeft = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.63f),
        size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.06f)
    )
}

// ==========================================
// 1.5. PREMIUM COMIC PLAYFUL COMPONENTS (STICKER OUTLINE STYLE)
// ==========================================

@Composable
fun ComicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = White,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Thick Comic Offset Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, shape)
        )
        // High Contrast Bordered Card
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor, shape)
                .border(2.5.dp, Color.Black, shape)
        ) {
            content()
        }
    }
}

@Composable
fun ComicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ComicYellow,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() }
    ) {
        // Shadow Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(Color.Black, shape)
        )
        // Primary Button Surface
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, shape)
                .border(2.5.dp, Color.Black, shape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun ComicCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ComicWhiteCard,
    size: Dp = 44.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 2.dp, y = 2.dp)
                .background(Color.Black, CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, CircleShape)
                .border(2.5.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun SegmentedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSegs = 15
            val filledSegs = (progress * totalSegs).toInt().coerceIn(0, totalSegs)
            val wSeg = size.width / totalSegs

            for (i in 0 until totalSegs) {
                if (i < filledSegs) {
                    val color = when {
                        i < totalSegs / 3 -> Color(0xFFE94560) // Trainee
                        i < totalSegs * 2 / 3 -> Color(0xFFFFD600) // Proficient
                        else -> Color(0xFF4CAF50) // Master genius
                    }
                    drawRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(i * wSeg, 0f),
                        size = androidx.compose.ui.geometry.Size(wSeg - 2.dp.toPx(), size.height)
                    )
                }
            }
        }
    }
}

@Composable
fun CuteCartoonBrainMascot(
    modifier: Modifier = Modifier,
    animationTick: Float = 0f
) {
    val swimOffset = sin(animationTick * 2 * Math.PI.toFloat()) * 5.dp.value

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Float / Swim Animation translation
        translate(top = swimOffset) {
            // Shadow behind Brain
            drawCircle(
                color = Color.Black.copy(alpha = 0.08f),
                radius = w * 0.35f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.55f)
            )

            // Draw Lobe Elements
            // Left Hemisphere
            drawCircle(
                color = Color(0xFFFFC0CB), // Playful Pink
                radius = w * 0.22f,
                center = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.45f)
            )
            // Right Hemisphere
            drawCircle(
                color = Color(0xFFFFC0CB),
                radius = w * 0.22f,
                center = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.45f)
            )
            // Cerebellum (Lower brain stem lobes)
            drawCircle(
                color = Color(0xFFFF8DA1), // Darker pink crease
                radius = w * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.6f)
            )
            drawCircle(
                color = Color(0xFFFF8DA1),
                radius = w * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.6f)
            )

            // Dynamic Folds wrinkly lines
            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.34f),
                size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.16f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )
            drawArc(
                color = Color.Black,
                startAngle = 240f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.34f),
                size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.16f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )

            // Outer thick Comic Boundary Stroke
            drawArc(
                color = Color.Black,
                startAngle = 110f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.2f),
                size = androidx.compose.ui.geometry.Size(w * 0.46f, h * 0.46f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.5f)
            )
            drawArc(
                color = Color.Black,
                startAngle = -170f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.2f),
                size = androidx.compose.ui.geometry.Size(w * 0.46f, h * 0.46f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.5f)
            )

            // Thick Round Comic Specs (Glasses)
            val gY = h * 0.48f
            val gR = w * 0.10f
            // Left Spectacle Frame
            drawCircle(
                color = Color.White,
                radius = gR,
                center = androidx.compose.ui.geometry.Offset(w * 0.4f, gY)
            )
            drawCircle(
                color = Color.Black,
                radius = gR,
                center = androidx.compose.ui.geometry.Offset(w * 0.4f, gY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
            // Left Pupil with shine
            drawCircle(
                color = Color.Black,
                radius = gR * 0.45f,
                center = androidx.compose.ui.geometry.Offset(w * 0.41f, gY - 1f)
            )
            drawCircle(
                color = Color.White,
                radius = gR * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.43f, gY - 3f)
            )

            // Right Spectacle Frame
            drawCircle(
                color = Color.White,
                radius = gR,
                center = androidx.compose.ui.geometry.Offset(w * 0.6f, gY)
            )
            drawCircle(
                color = Color.Black,
                radius = gR,
                center = androidx.compose.ui.geometry.Offset(w * 0.6f, gY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
            // Right Pupil with shine
            drawCircle(
                color = Color.Black,
                radius = gR * 0.45f,
                center = androidx.compose.ui.geometry.Offset(w * 0.61f, gY - 1f)
            )
            drawCircle(
                color = Color.White,
                radius = gR * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.63f, gY - 3f)
            )

            // Bridge of Specs
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(w * 0.48f, gY),
                end = androidx.compose.ui.geometry.Offset(w * 0.52f, gY),
                strokeWidth = 6f
            )

            // Smiling rosy mouth
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.45f, gY + 12f),
                size = androidx.compose.ui.geometry.Size(w * 0.10f, w * 0.08f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Cheek blushers
            drawCircle(
                color = Color(0xFFFF69B4).copy(alpha = 0.4f),
                radius = w * 0.04f,
                center = androidx.compose.ui.geometry.Offset(w * 0.28f, gY + 14f)
            )
            drawCircle(
                color = Color(0xFFFF69B4).copy(alpha = 0.4f),
                radius = w * 0.04f,
                center = androidx.compose.ui.geometry.Offset(w * 0.72f, gY + 14f)
            )

            // Right Thumbs Up Hand!
            val handX = w * 0.76f
            val handY = h * 0.62f
            drawCircle(
                color = Color(0xFFFFC0CB),
                radius = w * 0.06f,
                center = androidx.compose.ui.geometry.Offset(handX, handY)
            )
            drawCircle(
                color = Color.Black,
                radius = w * 0.06f,
                center = androidx.compose.ui.geometry.Offset(handX, handY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
            )
            // Arm connector line
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.60f),
                end = androidx.compose.ui.geometry.Offset(handX - 5f, handY),
                strokeWidth = 5f
            )
        }
    }
}

// ==========================================
// 2. HOME SCREEN (PLAY, LEVEL SEARCH, SETTINGS, DAILY GIFT, STORE)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onPlayClicked: () -> Unit,
    onNavigateToLevels: () -> Unit
) {
    val soundEnabled by viewModel.isSoundEnabled.collectAsState()
    val playLang by viewModel.language.collectAsState()
    val score by viewModel.unlockedLevel.collectAsState() // unlocked progressions
    val uiState by viewModel.uiState.collectAsState()

    var showSettingDialog by remember { mutableStateOf(false) }

    // Unity Ads Configuration remembered states
    var inputGameId by remember { mutableStateOf("") }
    var inputTestMode by remember { mutableStateOf(true) }
    var inputRewarded by remember { mutableStateOf("") }
    var inputInterstitial by remember { mutableStateOf("") }
    var inputBanner by remember { mutableStateOf("") }
    var showPlacementsConfig by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(showSettingDialog) {
        if (showSettingDialog) {
            inputGameId = viewModel.getAdsGameId()
            inputTestMode = viewModel.getAdsTestMode()
            inputRewarded = viewModel.getAdsRewardedPlacement()
            inputInterstitial = viewModel.getAdsInterstitialPlacement()
            inputBanner = viewModel.getAdsBannerPlacement()
        }
    }

    // Infinite Mascot floating animation tick
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    val mascotTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mascotTick"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg)
    ) {
        // High Quality Hand-drawn Notebook Sheet Lines drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 26.dp.toPx()
            val lineColor = GrayLine // Notebook rule lines (warm cream)

            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Left Crimson book bound margin
            drawLine(
                color = Color(0xFFFFB3B3),
                start = androidx.compose.ui.geometry.Offset(42.dp.toPx(), 0f),
                end = androidx.compose.ui.geometry.Offset(42.dp.toPx(), size.height),
                strokeWidth = 2.5f
            )
        }

        // Action Column Elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Action Row: Controls + Wallet Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Settings Button
                    ComicCircleButton(
                        onClick = { showSettingDialog = true },
                        backgroundColor = ComicYellow,
                        size = 46.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp),
                            tint = Color.Black
                        )
                    }

                    // Level Select Screen Navigation
                    ComicCircleButton(
                        onClick = onNavigateToLevels,
                        backgroundColor = ComicBlue,
                        size = 46.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Level Map",
                            modifier = Modifier.size(22.dp),
                            tint = Color.Black
                        )
                    }
                }

                // Bulbs Wallet Chip (Simulated Points wallet)
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .background(Color.White, RoundedCornerShape(21.dp))
                        .border(2.5.dp, Color.Black, RoundedCornerShape(21.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            modifier = Modifier.size(18.dp),
                            tint = ComicYellow
                        )
                        Text(
                            text = "${uiState.score}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        // Add package store clicker (+ button)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(ComicYellow, CircleShape)
                                .border(1.dp, Color.Black, CircleShape)
                                .clickable { viewModel.setStoreDialogVisible(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // IQ Progress Indicator Ruler
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicWhiteCard, RoundedCornerShape(16.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (playLang == "bn") "আইকিউ (IQ) লেভেল প্রোগ্রেস" else "IQ Level Progression",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Text(
                            text = "IQ: ${score * 12 - 12}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = ComicOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar mapping current progression fraction up to 150 tasks
                    val progressFraction = (score.toFloat() / 150f).coerceIn(0f, 1f)
                    SegmentedProgressBar(progress = progressFraction)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (playLang == "bn") "সর্বোচ্চ আনলকড লেভেল: $score/১৫০" else "Unlocked: $score/150 Levels",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Central Mascot & Interactive Badge Cards Box
            ComicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                backgroundColor = ComicSoftGreen
            ) {
                // Background artistic grids
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleLine = 12.dp.toPx()
                    var indexX = 0f
                    // Draw clean dashed sketch book cross lines
                    while (indexX < size.width) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.04f),
                            start = androidx.compose.ui.geometry.Offset(indexX, 0f),
                            end = androidx.compose.ui.geometry.Offset(indexX, size.height),
                            strokeWidth = 2f
                        )
                        indexX += scaleLine
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Vector drawn cute floating Mascot on the left half
                    CuteCartoonBrainMascot(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        animationTick = mascotTick
                    )

                    // Progress detail card on the right half
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .padding(start = 12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ComicYellow, RoundedCornerShape(8.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TRAIN BRAIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "BRAIN TRAP",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (playLang == "bn") "ভুল করলেই ট্র্যাপ! বাঁচতে হলে বুদ্ধি বাঁচাও" else "Don't fall for tricky baits! Play safe.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action 1: PLAY MAIN LEVELS GAME
            ComicButton(
                onClick = onPlayClicked,
                backgroundColor = ComicOrange,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (playLang == "bn") "▶ খেলতে শুরু করো (PLAY)" else "▶ TRAIN NOW (PLAY)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action 2: OPEN ALL LEVELS MAP
            ComicButton(
                onClick = onNavigateToLevels,
                backgroundColor = ComicWhiteCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (playLang == "bn") "সব লেভেল দেখুন (LEVELS)" else "VIEW MAP (LEVELS)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Row: Daily Chest, Support guidelines bottom actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gift Claims Box Circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ComicCircleButton(
                        onClick = { viewModel.setDailyDialogVisible(true) },
                        backgroundColor = ComicYellow,
                        size = 56.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Gift",
                            modifier = Modifier.size(26.dp),
                            tint = Color.Black
                        )
                        // Glowing claim alert marker!
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, CircleShape)
                                .border(1.5.dp, Color.Black, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (playLang == "bn") "ডেইলি গিফট" else "Daily Gift",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // How-to-play instruction modal drawer trigger
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Help Question mark circles
                    ComicCircleButton(
                        onClick = {
                            viewModel.startCampaignAd {
                                viewModel.setStoreDialogVisible(true)
                            }
                        },
                        backgroundColor = ComicBlue,
                        size = 56.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Power Shop",
                            modifier = Modifier.size(26.dp),
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (playLang == "bn") "পাওয়ার শপ" else "Power Shop",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Sound Effect control circular toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ComicCircleButton(
                        onClick = { viewModel.toggleSound() },
                        backgroundColor = if (soundEnabled) ComicPurple else Color.LightGray,
                        size = 56.dp
                    ) {
                        Text(if (soundEnabled) "ON" else "OFF", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (playLang == "bn") "শব্দ ও হর্ন" else "Sound",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        // ==========================================
        // OVERLAYS / DIALOGS IMPLEMENTATION
        // ==========================================

        // 1. SETTINGS SHEET POPUP DIALOG
        if (showSettingDialog) {
            AlertDialog(
                onDismissRequest = { showSettingDialog = false },
                containerColor = ComicPaperBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(2.dp),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (playLang == "bn") "গেম সেটিংস" else "Game Settings",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Coins Wallet Indicator Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF9E6), RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0xFFFFCC00), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🪙",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (playLang == "bn") "কয়েন ওয়ালেট" else "Coin Wallet",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = if (playLang == "bn") "গেম খেলে আরও অর্জন করুন" else "Earn more by playing",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = "${uiState.coins} Coins",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE69A00)
                                )
                            }
                        }

                        // Sfx switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (playLang == "bn") "শব্দ ও টোন (Sound SFX)" else "Sound Effects (SFX)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = { viewModel.toggleSound() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (soundEnabled) ComicPurple else Color.LightGray
                                ),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (soundEnabled) "ON" else "OFF", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        // Language Toggle switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (playLang == "bn") "ভাষা পরিবর্তন করুন" else "Switch Game Language",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = { 
                                    viewModel.toggleLanguage()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicBlue),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (playLang == "bn") "ENGLISH" else "বাংলা", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        // Clear Progress Data Reset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (playLang == "bn") "ডাটা রিসেট করুন" else "Reset Progress Data",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = {
                                    SoundManager.playWrong()
                                    viewModel.clearAllProgress()
                                    showSettingDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (playLang == "bn") "রিসেট" else "RESET",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            showSettingDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComicPurple),
                        border = BorderStroke(2.dp, Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (playLang == "bn") "ঠিক আছে" else "OKAY",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }

        // 2. DAILY GIFT BONANZA DIALOG
        if (uiState.showDailyClaimDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setDailyDialogVisible(false) },
                containerColor = ComicPaperBg,
                modifier = Modifier
                    .border(2.5.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(2.dp),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (playLang == "bn") "Daily Gift Bonanza" else "Daily Gift Bonanza",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = if (playLang == "bn") "Claim your daily rewards now!" else "Log in daily to claim free Light Bulbs!",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1 to 7 Days grid row layout representation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..4).forEach { dayNum ->
                                val isActive = (dayNum == uiState.currentClaimDayCycle)
                                val baseBg = if (isActive) ComicYellow else Color.White
                                val opacity = if (dayNum < uiState.currentClaimDayCycle) 0.5f else 1f

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.8f)
                                        .background(baseBg.copy(alpha = opacity), RoundedCornerShape(10.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("D$dayNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Bulb",
                                            modifier = Modifier.size(16.dp),
                                            tint = ComicYellow
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = when (dayNum) {
                                                4 -> "+30"
                                                else -> "+25"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (5..7).forEach { dayNum ->
                                val isActive = (dayNum == uiState.currentClaimDayCycle)
                                val baseBg = if (isActive) ComicYellow else if (dayNum == 7) ComicSoftOrange else Color.White
                                val opacity = if (dayNum < uiState.currentClaimDayCycle) 0.5f else 1f

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(baseBg.copy(alpha = opacity), RoundedCornerShape(10.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (dayNum == 7) "Day 7" else "Day $dayNum",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Bulb",
                                            modifier = Modifier.size(18.dp),
                                            tint = if (dayNum == 7) ComicOrange else ComicYellow
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = when (dayNum) {
                                                5 -> "+35"
                                                6 -> "+50"
                                                else -> "+200"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive CTA Button to claim gifts
                        if (!uiState.hasClaimedToday) {
                            Button(
                                onClick = { viewModel.claimDailyReward() },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicOrange),
                                border = BorderStroke(2.dp, Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = if (playLang == "bn") "Claim Reward" else "CLAIM REWARD NOW",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(ComicSoftGreen, RoundedCornerShape(12.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (playLang == "bn") "Gift collected successfully today!" else "Gift successfully collected today!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setDailyDialogVisible(false) }) {
                        Text(
                            text = "BACK TO HOME",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            )
        }


                 // 3. STORE SHOP DIALOG OVERLAY
        if (uiState.showStoreDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setStoreDialogVisible(false) },
                containerColor = ComicPaperBg,
                modifier = Modifier
                    .border(2.5.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(2.dp),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (playLang == "bn") "Power & Tool Shop" else "Power & Tool Shop",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = if (playLang == "bn") "Trade bulbs to buy vital power-ups!" else "Trade bulbs to buy vital puzzle powers!",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Store Item 1: Freeze
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (playLang == "bn") "Time Freeze Pack (x3)" else "Time Freeze Pack (x3)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = if (playLang == "bn") "Adds +15s to puzzle timers" else "Adds +15s to puzzle timers",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { viewModel.purchaseStoreItemByPoints("freeze", 50) },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicYellow),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("50 Bulbs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        // Store Item 2: Bomb
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (playLang == "bn") "Bomb Popper Pack (x3)" else "Bomb Popper Pack (x3)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = if (playLang == "bn") "Instantly removes 2 wrong options" else "Instantly removes 2 wrong options",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { viewModel.purchaseStoreItemByPoints("bomb", 75) },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicYellow),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("75 Bulbs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        // Store Item 3: Shield Defend
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (playLang == "bn") "Shield Defend Pack (x3)" else "Shield Defend Pack (x3)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = if (playLang == "bn") "Prevents failure penalty on 1 error" else "Prevents failure penalty on 1 error",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { viewModel.purchaseStoreItemByPoints("shield", 60) },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicYellow),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("60 Bulbs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        // Store Item 4: Skip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (playLang == "bn") "Puzzle Skip Pack (x3)" else "Puzzle Skip Pack (x3)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = if (playLang == "bn") "Instantly auto-pass active level" else "Instantly auto-pass active level",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { viewModel.purchaseStoreItemByPoints("skip", 90) },
                                colors = ButtonDefaults.buttonColors(containerColor = ComicYellow),
                                border = BorderStroke(1.5.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("90 Bulbs", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setStoreDialogVisible(false) }) {
                        Text(
                            text = "BACK TO GAME",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            )
        }
    }
}

// ==========================================
// 3. LEVEL SEARCH / SELECT SCREEN (150 LEVELS GRID WITH STAGES)
// ==========================================
@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onLevelSelected: (Int) -> Unit
) {
    val maxUnlocked by viewModel.unlockedLevel.collectAsState()
    val languageCode by viewModel.language.collectAsState()

    var activeStageTab by remember { mutableIntStateOf(1) } // Tab: 1 (Stage 1), 2 (Stage 2), 3 (Stage 3)

    val stageRange = when (activeStageTab) {
        1 -> 1..50
        2 -> 51..100
        else -> 101..150
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg)
    ) {
        // Hand-drawn Notebook Sheet Lines drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 24.dp.toPx()
            val lineColor = GrayLine // Notebook rule lines (warm cream)

            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Left Crimson book bound margin
            drawLine(
                color = Color(0xFFFFB3B3),
                start = androidx.compose.ui.geometry.Offset(42.dp.toPx(), 0f),
                end = androidx.compose.ui.geometry.Offset(42.dp.toPx(), size.height),
                strokeWidth = 2.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Screen Header Back Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComicCircleButton(
                    onClick = onNavigateBack,
                    backgroundColor = ComicYellow,
                    size = 42.dp
                ) {
                    Text("⬅", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = if (languageCode == "bn") "Brain Trap Levels" else "Brain Trap Levels",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            // Three Stage Cards Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stage 1 Tab
                StageTabButton(
                    title = if (languageCode == "bn") "স্টেজ ১ (১-৫০)" else "Stage 1 (1-50)",
                    sub = if (languageCode == "bn") "বোকা শুরু" else "Noob",
                    isActive = activeStageTab == 1,
                    activeColor = ComicGreen,
                    onClick = { activeStageTab = 1 },
                    modifier = Modifier.weight(1f)
                )

                // Stage 2 Tab
                StageTabButton(
                    title = if (languageCode == "bn") "স্টেজ ২ (৫১-১০০)" else "Stage 2 (51-100)",
                    sub = if (languageCode == "bn") "মাথা ঘুরানো" else "Medium",
                    isActive = activeStageTab == 2,
                    activeColor = ComicOrange,
                    onClick = { activeStageTab = 2 },
                    modifier = Modifier.weight(1f)
                )

                // Stage 3 Tab
                StageTabButton(
                    title = if (languageCode == "bn") "স্টেজ ৩ (১০১-১৫০)" else "Stage 3 (101-150)",
                    sub = if (languageCode == "bn") "মাথা নষ্ট" else "Insane",
                    isActive = activeStageTab == 3,
                    activeColor = ComicPurple,
                    onClick = { activeStageTab = 3 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Levels scrolling Grid list representation
            LazyVerticalGrid(
                columns = GridCells.Adaptive(68.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(stageRange.toList()) { levelNum ->
                    val isUnlocked = (levelNum <= maxUnlocked)
                    val isCompleted = (levelNum < maxUnlocked)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = when {
                                    isCompleted -> ComicSoftGreen
                                    isUnlocked -> ComicYellow
                                    else -> Color.White.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isUnlocked) Color.Black else Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = isUnlocked) {
                                onLevelSelected(levelNum)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUnlocked) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$levelNum",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = if (isCompleted) "✓ " else "PLAY",
                                    fontSize = 9.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            // Locked symbol
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔒", fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "LOCK",
                                    fontSize = 8.sp,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StageTabButton(
    title: String,
    sub: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) activeColor else Color.White)
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                fontSize = 10.sp,
                color = if (isActive) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
// 4. GAME PLAY SCREEN (TIMER, POWER-UPS, OPTIONS)
// ==========================================
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    gameState: GameUiState,
    onNavigateBack: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()

    val optXRay by viewModel.powerUpsXRay.collectAsState()
    val optFreeze by viewModel.powerUpsFreeze.collectAsState()
    val optBomb by viewModel.powerUpsBomb.collectAsState()
    val optSkip by viewModel.powerUpsSkip.collectAsState()
    val optShield by viewModel.powerUpsShield.collectAsState()

    val currentQ = gameState.currentQuestion

    // Animation flag for shaking buttons on incorrect click
    var shakeTriggerCount by remember { mutableStateOf(0) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeTriggerCount % 2 == 1) 20f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ShakeOffset"
    )

    // Flash timer when remaining time < 5
    val timeLimitColor = if (gameState.timeLeft <= 5) {
        val blinkAlpha by rememberInfiniteTransition(label = "").animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(250),
                repeatMode = RepeatMode.Reverse
            ),
            label = "TimerBlink"
        )
        WrongRed.copy(alpha = blinkAlpha)
    } else {
        ComicOrange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg)
    ) {
        // Hand-drawn Notebook Sheet Lines drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 24.dp.toPx()
            val lineColor = GrayLine // Notebook rule lines (warm cream)

            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Left Crimson book bound margin
            drawLine(
                color = Color(0xFFFFB3B3),
                start = androidx.compose.ui.geometry.Offset(42.dp.toPx(), 0f),
                end = androidx.compose.ui.geometry.Offset(42.dp.toPx(), size.height),
                strokeWidth = 2.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Game Top HUD panel: Lives, Levels, Scores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                ComicCircleButton(
                    onClick = onNavigateBack,
                    backgroundColor = ComicYellow,
                    size = 36.dp
                ) {
                    Text("⬅", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }

                // Level indicator
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (languageCode == "bn") "লেভেল: ${gameState.currentLevelNumber}" else "Level: ${gameState.currentLevelNumber}",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                }

                // Lives represent standard hearts (❤️)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { index ->
                        val hasLife = index < gameState.lives
                        Canvas(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            if (hasLife) {
                                drawHeart(this, WrongRed)
                            } else {
                                drawHeart(this, Color.Black.copy(alpha = 0.15f))
                            }
                        }
                    }
                }

                // Total Score & Coins indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bulbs
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "💡 ${gameState.score}",
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            fontSize = 11.sp
                        )
                    }

                    // Coins
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🪙 ${gameState.coins}",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Central Progress bar countdown representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(10.dp)
                    .background(Color.White, RoundedCornerShape(5.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(5.dp))
            ) {
                val progressFraction = gameState.timeLeft.toFloat() / gameState.totalLevelTime.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(timeLimitColor, ComicYellow)
                            ),
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            // Countdown text display
            Text(
                text = "${gameState.timeLeft}s",
                color = if (gameState.timeLeft <= 5) WrongRed else Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                textAlign = TextAlign.End
            )

            // HORIZONTAL POWER-UPS BAR: Moved to the top area beautifully!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Power Up: Xray (Dims wrong choices)
                PowerUpButton(
                    icon = Icons.Default.Search,
                    title = "X-Ray",
                    count = optXRay,
                    isActive = gameState.isXRayActive,
                    onActivate = { viewModel.activateXRay() },
                    onRefill = { viewModel.requestAdForPowerUp("xray") }
                )

                // Power Up: Freeze (Increases timer by 15s)
                PowerUpButton(
                    icon = Icons.Default.Settings,
                    title = "Freeze",
                    count = optFreeze,
                    isActive = gameState.isFreezeActive,
                    onActivate = { viewModel.activateFreeze() },
                    onRefill = { viewModel.requestAdForPowerUp("freeze") }
                )

                // Power Up: Bomb (Removes 2 wrong)
                PowerUpButton(
                    icon = Icons.Default.Delete,
                    title = "Bomb",
                    count = optBomb,
                    isActive = gameState.isBombActive,
                    onActivate = { viewModel.activateBomb() },
                    onRefill = { viewModel.requestAdForPowerUp("bomb") }
                )

                // Power Up: Skip level
                PowerUpButton(
                    icon = Icons.Default.Refresh,
                    title = "Skip",
                    count = optSkip,
                    isActive = false,
                    onActivate = { viewModel.activateSkip() },
                    onRefill = { viewModel.requestAdForPowerUp("skip") }
                )

                // Power Up: Shield (protects you once)
                PowerUpButton(
                    icon = Icons.Default.Star,
                    title = "Shield",
                    count = optShield,
                    isActive = gameState.isShieldActive,
                    onActivate = { viewModel.activateShield() },
                    onRefill = { viewModel.requestAdForPowerUp("shield") }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main gameplay area taking full width now!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    // Comic Question Board Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 10.dp)
                    ) {
                        // Offset Shadow
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(20.dp))
                        )
                        // Notebook paper card
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Stage ${gameState.currentStageNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .background(ComicYellow, RoundedCornerShape(6.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (languageCode == "bn") currentQ?.questionBn.toString() else currentQ?.questionEn.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }

                    // Answer Options (4 buttons)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        currentQ?.optionsBn?.indices?.forEach { idx ->
                            val promptOptionBn = currentQ.optionsBn[idx]
                            val promptOptionEn = currentQ.optionsEn[idx]

                            val isHiddenByBomb = gameState.revealedIncorrectIndices.contains(idx)
                            val isDimmedByXRay = gameState.isXRayActive && idx != currentQ.correctIndex

                            // UI TRICK coordinate adjustments
                            val offset = gameState.uiTrickState.buttonOffsets.getOrNull(idx) ?: OffsetPair(0f, 0f)

                            // Animation scale triggers for certain tricky options
                            val scaleModifier = if (currentQ.type == "ui_trick" && currentQ.uiTargetButtonScale == idx) 1.25f else 1f

                            // Button dynamic style rules
                            val backBgColor = when {
                                isDimmedByXRay -> Color.LightGray.copy(alpha = 0.5f)
                                currentQ.type == "ui_trick" && gameState.uiTrickState.labelColorSwap -> ComicYellow
                                idx == 0 && currentQ.type == "ui_trick" -> ComicSoftOrange
                                idx == 1 && currentQ.type == "ui_trick" -> ComicBlue
                                else -> Color.White
                            }

                            if (!isHiddenByBomb) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .offset(
                                            x = (offset.x + if (gameState.selectedOptionIndex == idx && !gameState.isCorrectAnswer) shakeOffset else 0f).dp,
                                            y = offset.y.dp
                                        )
                                        .scale(scaleModifier)
                                ) {
                                    // Custom visual offset shadow click helper
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .offset(x = 3.dp, y = 3.dp)
                                            .background(Color.Black, RoundedCornerShape(14.dp))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(backBgColor, RoundedCornerShape(14.dp))
                                            .border(
                                                width = if (gameState.isXRayActive && idx == currentQ.correctIndex) 3.dp else 2.dp,
                                                color = if (gameState.isXRayActive && idx == currentQ.correctIndex) ComicGreen else Color.Black,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                if (idx != currentQ.correctIndex) {
                                                    shakeTriggerCount++
                                                }
                                                viewModel.selectOption(idx)
                                            }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Bullet letter (A, B, C, D)
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .background(ComicBlue, CircleShape)
                                                    .border(2.dp, Color.Black, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when (idx) {
                                                        0 -> "A"
                                                        1 -> "B"
                                                        2 -> "C"
                                                        else -> "D"
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = if (languageCode == "bn") promptOptionBn else promptOptionEn,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Excluded option block (leaves clean negative space)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }

// Draw life heart vector manually
fun drawHeart(scope: DrawScope, color: Color) {
    val px = scope.size.width
    val py = scope.size.height
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(px / 2f, py * 0.85f)
        cubicTo(px * 0.15f, py * 0.6f, 0f, py * 0.35f, px * 0.2f, py * 0.15f)
        cubicTo(px * 0.35f, 0f, px / 2f, py * 0.2f, px / 2f, py * 0.3f)
        cubicTo(px / 2f, py * 0.2f, px * 0.65f, 0f, px * 0.8f, py * 0.15f)
        cubicTo(px, py * 0.35f, px * 0.85f, py * 0.6f, px / 2f, py * 0.85f)
        close()
    }
    scope.drawPath(path, color)

    // draw stroke board as well in black to fit style
    scope.drawPath(
        path = path,
        color = Color.Black,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
    )
}

@Composable
fun PowerUpButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    isActive: Boolean,
    onActivate: () -> Unit,
    onRefill: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (isActive) ComicYellow else Color.White,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isActive) 3.dp else 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (count > 0) {
                        onActivate()
                    } else {
                        onRefill()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = Color.Black
            )

            // Badge with remaining count or dynamic free refill button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .background(if (count > 0) ComicOrange else ComicGreen, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (count > 0) "$count" else "GET",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================
// 5. RESULT SCREEN (CONGRATULATE / RETRY OVERLAY)
// ==========================================
@Composable
fun ResultScreen(
    viewModel: GameViewModel,
    gameState: GameUiState,
    onResetHome: () -> Unit,
    onNextLevel: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val isCorrect = gameState.isCorrectAnswer
    val currentQ = gameState.currentQuestion

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg)
    ) {
        // Hand-drawn Notebook Sheet Lines drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 24.dp.toPx()
            val lineColor = GrayLine

            // Horizontal lines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Left Bound Margins
            drawLine(
                color = Color(0xFFFFB3B3),
                start = androidx.compose.ui.geometry.Offset(42.dp.toPx(), 0f),
                end = androidx.compose.ui.geometry.Offset(42.dp.toPx(), size.height),
                strokeWidth = 2.5f
            )
        }

        // Overlay particle canvas for confetti rendering on correct triggers
        if (isCorrect && viewModel.confettiList.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                viewModel.confettiList.forEach { p ->
                    drawCircle(
                        color = Color(p.colorHex),
                        radius = p.size,
                        center = androidx.compose.ui.geometry.Offset(p.x, p.y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Stars graphic banner badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        color = if (isCorrect) ComicSoftGreen else Color.White,
                        shape = CircleShape
                    )
                    .border(3.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(52.dp)) {
                    if (isCorrect) {
                        drawStarSymbol(this, ComicYellow)
                    } else {
                        // Drawing cross mark
                        drawLine(
                            color = PrimaryRed,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 10f
                        )
                        drawLine(
                            color = PrimaryRed,
                            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = 10f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score details feedback title
            Text(
                text = gameState.explanationTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isCorrect) ComicGreen else PrimaryRed,
                textAlign = TextAlign.Center
            )

            // Speed bonus point details
            if (isCorrect && gameState.lastAnswerSpeedBonus > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(ComicYellow, RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (languageCode == "bn") "+৫ সেকেন্ড স্পিড বোনাস! ⚡" else "+5 Sec Speed Bonus! ⚡",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Section Box (Only viewable when correct, as requested by the user)
            if (isCorrect) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // border shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.Black, RoundedCornerShape(18.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .border(2.5.dp, Color.Black, RoundedCornerShape(18.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = if (languageCode == "bn") "Explanation Details:" else "Explanation Details Guide:",
                                fontSize = 13.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (languageCode == "bn") currentQ?.explanationBn.toString() else currentQ?.explanationEn.toString(),
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // If wrong, show a beautifully styled cartoonish 'Try Again!' container that motivates them but reveals nothing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.Black, RoundedCornerShape(18.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .border(2.5.dp, Color.Black, RoundedCornerShape(18.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "That answer is incorrect. Try rethinking your strategy!",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Control Action Columns
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isCorrect) {
                    ComicButton(
                        onClick = onNextLevel,
                        backgroundColor = ComicGreen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (languageCode == "bn") "পরবর্তী লেভেল (NEXT)" else "NEXT LEVEL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    ComicButton(
                        onClick = { viewModel.retryLevel() },
                        backgroundColor = ComicOrange,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (languageCode == "bn") "পুনরায় চেষ্টা করুন (RETRY)" else "TRY AGAIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Go to Main Home
                ComicButton(
                    onClick = onResetHome,
                    backgroundColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (languageCode == "bn") "মূল স্ক্রিনে ফিরুন" else "GO TO HOME",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// Draw beautiful award star symbol manually
fun drawStarSymbol(scope: DrawScope, color: Color) {
    val w = scope.size.width
    val h = scope.size.height
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(w * 0.5f, 0f)
        lineTo(w * 0.62f, h * 0.38f)
        lineTo(w, h * 0.38f)
        lineTo(w * 0.69f, h * 0.62f)
        lineTo(w * 0.81f, h)
        lineTo(w * 0.5f, h * 0.76f)
        lineTo(w * 0.19f, h)
        lineTo(w * 0.31f, h * 0.62f)
        lineTo(0f, h * 0.38f)
        lineTo(w * 0.38f, h * 0.38f)
        close()
    }
    scope.drawPath(path, color)

    // Draw outline
    scope.drawPath(
        path = path,
        color = Color.Black,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
    )
}

// ==========================================
// 6. AD OVERLAYS (SIMULATED INTERSTITIALS / REWARDS)
// ==========================================
@Composable
fun AdSimulationOverlay(
    gameState: GameUiState,
    onRestored: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .background(Color.Black, RoundedCornerShape(24.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicPaperBg, RoundedCornerShape(24.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡ ADVERTISEMENT UNIT ⚡",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ComicOrange,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Brain Test Sponsored Offer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, CircleShape)
                        .border(2.5.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Lives Refill",
                        modifier = Modifier.size(46.dp),
                        tint = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You have run out of daily lives! Relax and watch a short sponsored video to restore all your lives for FREE and continue your adventure.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }

                    Button(
                        onClick = onRestored,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ComicYellow),
                        border = BorderStroke(2.dp, Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "WATCH (AD)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }
            }
        }
    }
}

// Interstitial Playing Full Screen Block Overlay
@Composable
fun AdPlayerScreen(
    gameState: GameUiState,
    languageCode: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComicPaperBg),
        contentAlignment = Alignment.Center
    ) {
        // Hand-drawn lines under ad screen as well
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 24.dp.toPx()
            val lineColor = GrayLine
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text(
                text = if (languageCode == "bn") "বিজ্ঞাপন লোড হচ্ছে" else "SPONSOR VIDEO LOADING",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, CircleShape)
                    .border(3.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ComicOrange,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    strokeWidth = 5.dp
                )
                // Elegant loading icon
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ComicOrange,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (languageCode == "bn") "ভিডিও প্রস্তুত করা হচ্ছে..." else "Preparing sponsored stream...",
                fontSize = 17.sp,
                color = Color.Black,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (languageCode == "bn") "অনুগ্রহ করে সচল ইন্টারনেট কানেকশন রাখুন এবং কিছুক্ষণ অপেক্ষা করুন।" else "Connecting to ad server, please keep your internet on.",
                fontSize = 12.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
