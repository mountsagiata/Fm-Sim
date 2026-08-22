package com.mountsa.fmsimulation.ui.screens.match

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mountsa.fmsimulation.R
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import java.util.Calendar

@Composable
fun MatchStageBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(FM_DARK_BG)) {
        Image(
            painter = painterResource(R.drawable.splashscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(.13f)
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Black.copy(.68f),
                        FM_GREEN.copy(.035f),
                        Color.Black.copy(.72f)
                    )
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(.2f), Color.Transparent, Color.Black.copy(.62f))
                )
            )
        )
        content()
    }
}

fun matchDayInfo(matchDate: Long, stadium: String): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = matchDate }
    val kickoff = listOf("12:30", "15:00", "17:30", "20:00")[calendar.get(Calendar.DAY_OF_MONTH) % 4]
    val weather = listOf("Clear 18°C", "Cloudy 16°C", "Light rain 14°C", "Breezy 17°C")[calendar.get(Calendar.DAY_OF_YEAR) % 4]
    return "$kickoff  •  ${stadium.ifBlank { "Main Stadium" }}  •  $weather"
}
