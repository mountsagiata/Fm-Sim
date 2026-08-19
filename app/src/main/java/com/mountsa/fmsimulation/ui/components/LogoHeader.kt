package com.mountsa.fmsimulation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.theme.FM_Green

@Composable
fun LogoHeader(
    modifier: Modifier = Modifier,
    showEdition: Boolean = true,
    scale: Float = 1f,
    variant: LogoVariant = LogoVariant.FULL,
    animated: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (variant) {
            LogoVariant.FULL -> FullLogo(scale, animated, glowAlpha)
            LogoVariant.COMPACT -> CompactLogo(scale)
            LogoVariant.MINIMAL -> MinimalLogo(scale)
        }

        if (showEdition && variant == LogoVariant.FULL) {
            Spacer(modifier = Modifier.height((12 * scale).dp))

            Text(
                text = "2 0 2 7   E D I T I O N",
                fontSize = (12 * scale).sp,
                color = colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = (3 * scale).sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                softWrap = false
            )
        }
    }
}

@Composable
private fun FullLogo(scale: Float, animated: Boolean, glowAlpha: Float) {
    val colorScheme = MaterialTheme.colorScheme
    val onBgColor = colorScheme.onBackground

    Text(
        text = "FOOTBALL",
        fontSize = (38 * scale).sp,
        fontWeight = FontWeight.Black,
        color = FM_Green,
        letterSpacing = (6 * scale).sp,
        softWrap = false,
        modifier = Modifier.drawBehind {
            if (animated) {
                drawCircle(
                    color = FM_Green.copy(alpha = glowAlpha * 0.3f),
                    radius = size.width / 2,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }
    )

    // Separator line
    Box(
        modifier = Modifier
            .width((180 * scale).dp)
            .height((3 * scale).dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        onBgColor.copy(alpha = 0.4f),
                        onBgColor.copy(alpha = 0.8f),
                        onBgColor.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )

    Spacer(modifier = Modifier.height((6 * scale).dp))

    // MANAGER Box
    val borderColor = if (animated) {
        Brush.horizontalGradient(
            colors = listOf(
                onBgColor.copy(alpha = 0.5f),
                onBgColor,
                onBgColor.copy(alpha = 0.5f)
            )
        )
    } else {
        androidx.compose.ui.graphics.SolidColor(onBgColor)
    }

    Box(
        modifier = Modifier
            .border(
                width = (4 * scale).dp,
                brush = borderColor,
                shape = RectangleShape
            )
            .padding(horizontal = (16 * scale).dp, vertical = (2 * scale).dp)
            .drawBehind {
                if (animated) {
                    drawRect(
                        color = onBgColor.copy(alpha = glowAlpha * 0.05f),
                        topLeft = Offset(-5f * scale, -5f * scale),
                        size = androidx.compose.ui.geometry.Size(
                            width = size.width + (10f * scale),
                            height = size.height + (10f * scale)
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MANAGER",
            fontSize = (60 * scale).sp,
            fontWeight = FontWeight.Black,
            color = onBgColor,
            letterSpacing = ((-1) * scale).sp,
            softWrap = false,
            style = androidx.compose.ui.text.TextStyle(
                shadow = if (animated) Shadow(
                    color = onBgColor.copy(alpha = 0.2f),
                    blurRadius = 4f * scale,
                    offset = Offset.Zero
                ) else null
            )
        )
    }
}

@Composable
private fun CompactLogo(scale: Float) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FM",
            fontSize = (48 * scale).sp,
            fontWeight = FontWeight.Black,
            color = FM_Green,
            letterSpacing = (2 * scale).sp
        )
        Text(
            text = "2027",
            fontSize = (24 * scale).sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            letterSpacing = (1 * scale).sp
        )
    }
}

@Composable
private fun MinimalLogo(scale: Float) {
    Text(
        text = "FM",
        fontSize = (32 * scale).sp,
        fontWeight = FontWeight.Black,
        color = FM_Green,
        letterSpacing = (2 * scale).sp
    )
}

enum class LogoVariant {
    FULL, COMPACT, MINIMAL
}

@Preview(name = "Logo Light Mode")
@Composable
private fun FullLogoPreview() {
    MaterialTheme(colorScheme = androidx.compose.material3.lightColorScheme()) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(32.dp)
        ) {
            LogoHeader(scale = 0.7f, variant = LogoVariant.FULL)
        }
    }
}
