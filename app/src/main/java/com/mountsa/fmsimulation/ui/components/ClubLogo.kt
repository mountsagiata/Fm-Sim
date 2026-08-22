package com.mountsa.fmsimulation.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mountsa.fmsimulation.data.repository.DataRepository
import androidx.compose.ui.unit.sp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Composable
fun ClubLogo(
    clubId: Long,
    size: Dp = 32.dp,
    repository: DataRepository? = null
) {
    var logoAsset by remember { mutableStateOf<String?>(null) }

    // Load logo asset asynchronously
    androidx.compose.runtime.LaunchedEffect(clubId) {
        try {
            // This would need to be called from ViewModel or injected
            // For now, use a direct path pattern
            logoAsset = "database/logo/club/l$clubId.webp"
        } catch (e: Exception) {
            logoAsset = null
        }
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (logoAsset != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/$logoAsset")
                    .crossfade(true)
                    .build(),
                contentDescription = "Club Logo",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(com.mountsa.fmsimulation.ui.localization.localized("🛡️"), fontSize = size.value.sp)
        }
    }
}

// Extension function to get Dp to sp approximation
private val Dp.value: Float get() = this.value