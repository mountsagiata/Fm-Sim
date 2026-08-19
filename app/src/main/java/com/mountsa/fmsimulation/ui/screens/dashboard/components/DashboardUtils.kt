package com.mountsa.fmsimulation.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun DashboardGridItem(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF5F))
            HorizontalDivider(Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: Int, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value.toString(), fontSize = 10.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = Color.White)
    }
}

@Composable
fun LeagueLogo(leagueId: Long, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    if (leagueId <= 0L) return
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/database/logo/league/l$leagueId.webp")
            .crossfade(true)
            .build(),
        contentDescription = "League Logo",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}
