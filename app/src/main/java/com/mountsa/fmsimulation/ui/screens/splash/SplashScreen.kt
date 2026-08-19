package com.mountsa.fmsimulation.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.R
import com.mountsa.fmsimulation.ui.components.LogoHeader
import com.mountsa.fmsimulation.ui.theme.FM_Green
import com.mountsa.fmsimulation.ui.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun FmSplashScreen(viewModel: SplashViewModel) {
    val statusText by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    // Smooth animation for the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    val tips = listOf(
        "MANAGE YOUR BUDGET WISELY",
        "SCOUT YOUNG TALENTS EARLY",
        "ROTATE SQUAD FOR FITNESS",
        "KEEP MORALE HIGH WITH PRAISE",
        "ADAPT TACTICS TO OPPONENTS",
        "UPGRADE FACILITIES FOR GROWTH",
        "NEGOTIATE CONTRACTS CAREFULLY"
    )

    var currentTipIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        // Background image (same one used on ProfileScreen), faded
        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.linearGradient(0.3f to Color.Black, 0.45f to Color.Transparent),
                        blendMode = BlendMode.DstIn
                    )
                }
                .graphicsLayer(alpha = 0.15f)
        )

        // Gradient Background Overlay
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colorScheme.background.copy(alpha = 0.5f),
                            colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LogoHeader(scale = 0.55f, showEdition = true, animated = true)
            Spacer(modifier = Modifier.weight(1.2f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.85f)
        ) {
            // 1. Tips section (Top)
            AnimatedContent(
                targetState = tips[currentTipIndex],
                transitionSpec = { fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically() },
                label = "tips"
            ) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tip,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            // 2. Flowing Progress Bar (Center)
            FlowingProgressBar(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Status and Percentage Row (Bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText.uppercase(),
                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = FM_Green,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun FlowingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flow")
    
    // Sweep effect for the "energy" flow inside the bar
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val drawWidth = size.width * progress.coerceIn(0f, 1f)
            
            if (drawWidth > 0) {
                // The main Liquid Fill
                val liquidBrush = Brush.horizontalGradient(
                    colors = listOf(
                        FM_Green.copy(alpha = 0.8f),
                        FM_Green,
                        FM_Green.copy(alpha = 0.8f)
                    )
                )

                drawRoundRect(
                    brush = liquidBrush,
                    size = size.copy(width = drawWidth),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                )

                // The "Flowing" Shimmer Effect
                val shimmerX = size.width * sweepOffset
                val shimmerBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    startX = shimmerX - 150f,
                    endX = shimmerX + 150f
                )

                clipRect(right = drawWidth) {
                    drawRect(
                        brush = shimmerBrush,
                        size = size
                    )
                }
                
                // Glowing Head (Liquid tip effect)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(drawWidth, size.height / 2),
                        radius = 12.dp.toPx()
                    ),
                    center = Offset(drawWidth, size.height / 2),
                    radius = 12.dp.toPx()
                )
                
                drawCircle(
                    color = FM_Green,
                    radius = 4.dp.toPx(),
                    center = Offset(drawWidth, size.height / 2),
                    alpha = 0.8f
                )
            }
        }
    }
}
