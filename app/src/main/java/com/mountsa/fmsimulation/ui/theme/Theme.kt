package com.mountsa.fmsimulation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.max

// TEMA TETAP (STATIC DARK THEME) - Dominan Hitam, Tombol Hijau
private val FMColorScheme = darkColorScheme(
    primary = FM_Green,
    onPrimary = Fm_Black,
    secondary = FM_BlueElectric,
    onSecondary = Color.Black,
    background = FM_DarkBackground,
    onBackground = FM_TextPrimary,
    surface = FM_Surface,
    onSurface = FM_TextPrimary,
    surfaceVariant = FM_SurfaceVariant,
    onSurfaceVariant = FM_TextSecondary,
    outline = FM_GlassStroke,
    error = Color(0xFFCF6679)
)

@Composable
fun FootballManagerSimulationTheme(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Ambil nilai terpanjang untuk memastikan kalkulasi landscape akurat
    // (Mencegah bug jika sistem mendeteksi tinggi sebagai lebar saat transisi)
    val currentScreenWidth = max(configuration.screenWidthDp, configuration.screenHeightDp).toFloat()

    // Base Design Width: 800dp adalah standar ukuran ideal UI Game Landscape modern.
    // Semua ukuran .dp yang Anda tulis di kode akan dihitung berbasis layar lebar 800dp ini.
    val designWidth = 800f

    // Hitung faktor skala dasar
    val rawScaleFactor = currentScreenWidth / designWidth

    // PENTING: Batasi scaleFactor (Clamping) agar di HP yang sangat panjang (2400x1080 / rasio 20:9),
    // UI tidak menjadi terlalu raksasa atau meluber keluar batas aman layar.
    val scaleFactor = rawScaleFactor.coerceIn(0.85f, 1.25f)

    // Buat density kustom yang menggabungkan skala kepadatan layar bawaan HP dengan scaleFactor game Anda
    val customDensity = Density(
        density = density.density * scaleFactor,
        fontScale = density.fontScale // Menjaga ukuran font sistem pengguna tetap proporsional
    )

    // Injeksi kustom density ke dalam siklus hidup UI Jetpack Compose
    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = FMColorScheme,
            typography = Typography, // Memastikan file Typography.kt Anda teraplikasikan
            content = content
        )
    }
}