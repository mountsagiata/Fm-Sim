package com.mountsa.fmsimulation.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity

@Composable
fun PlayerAvatar(player: PlayerEntity, size: Dp = 34.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(Color(0xFF282B2F)),
        contentAlignment = Alignment.Center
    ) {
        if (player.avatarAsset.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/${player.avatarAsset}")
                    .crossfade(true)
                    .build(),
                contentDescription = player.shortName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            androidx.compose.material3.Text(
                player.shortName.take(2).uppercase(),
                color = Color.White,
                fontSize = (size.value * .24f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
