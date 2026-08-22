package com.mountsa.fmsimulation.ui.screens.dashboard.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

private enum class SettingsSection(val title: String) { GAME("Game"), DISPLAY("Display"), AUDIO("Audio"), LANGUAGE("Language") }

@Composable
fun SettingsHub(viewModel: DashboardViewModel) {
    var selected by remember { mutableStateOf(SettingsSection.GAME) }
    var reset by remember { mutableStateOf(false) }
    val musicEnabled by viewModel.audioManager.musicEnabled.collectAsStateWithLifecycle()
    val sfxEnabled by viewModel.audioManager.sfxEnabled.collectAsStateWithLifecycle()
    val music by viewModel.audioManager.musicVolume.collectAsStateWithLifecycle()
    val sfx by viewModel.audioManager.sfxVolume.collectAsStateWithLifecycle()
    val crowd by viewModel.audioManager.crowdVolume.collectAsStateWithLifecycle()
    val language by viewModel.localeManager.language.collectAsStateWithLifecycle()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 700.dp
        if (compact) Column(Modifier.fillMaxSize()) {
            SettingsTabs(selected) { selected = it }
            SettingsPane(selected, musicEnabled, sfxEnabled, music, sfx, crowd, language, viewModel, { reset = true }, Modifier.fillMaxSize())
        } else Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                Modifier.width(190.dp).fillMaxHeight().background(Color.White.copy(.025f), RoundedCornerShape(16.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("SETTINGS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(8.dp))
                SettingsSection.entries.forEach { section ->
                    FilterChip(
                        selected = selected == section, onClick = { selected = section },
                        label = { Text(section.title, modifier = Modifier.fillMaxWidth()) }, modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FM_GREEN.copy(.18f))
                    )
                }
            }
            SettingsPane(selected, musicEnabled, sfxEnabled, music, sfx, crowd, language, viewModel, { reset = true }, Modifier.weight(1f))
        }
    }
    if (reset) AlertDialog(
        onDismissRequest = { reset = false }, title = { Text("Reset career data?") },
        text = { Text("This permanently deletes the active career, fixtures, match history and standings.") },
        confirmButton = { TextButton(onClick = { reset = false; viewModel.resetCareer() }) { Text("RESET", color = Color.Red) } },
        dismissButton = { TextButton(onClick = { reset = false }) { Text("CANCEL") } }
    )
}

@Composable
private fun SettingsTabs(selected: SettingsSection, onSelect: (SettingsSection) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selected.ordinal, edgePadding = 8.dp, containerColor = Color.Transparent) {
        SettingsSection.entries.forEach { section -> Tab(selected == section, { onSelect(section) }, text = { Text(section.title) }) }
    }
}

@Composable
private fun SettingsPane(
    section: SettingsSection, musicEnabled: Boolean, sfxEnabled: Boolean,
    music: Float, sfx: Float, crowd: Float, language: String,
    viewModel: DashboardViewModel, onReset: () -> Unit, modifier: Modifier
) {
    Column(
        modifier.fillMaxHeight().background(Color.White.copy(.025f), RoundedCornerShape(16.dp))
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(section.title.uppercase(), color = FM_GREEN, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        when (section) {
            SettingsSection.GAME -> {
                SettingToggle("Auto-save after match", true)
                SettingToggle("Enable match commentary", true)
                SettingToggle("Confirm before advancing on matchday", true)
                SettingToggle("Pause automatically at half-time", true)
                HorizontalDivider(color = Color.White.copy(.08f))
                Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(.75f))) {
                    Text("RESET CAREER DATA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            SettingsSection.DISPLAY -> {
                SettingToggle("Show player faces", true)
                SettingToggle("Show attributes as progress bars", true)
                SettingToggle("Compact squad rows", false)
                SettingToggle("Reduce animations", false)
                Text("Layout adapts automatically for phones, tablets, foldables and landscape screens.", color = Color.Gray, fontSize = 12.sp)
            }
            SettingsSection.AUDIO -> {
                SettingToggle("Music", musicEnabled, viewModel.audioManager::setMusicEnabled)
                VolumeSlider("Music", music, viewModel.audioManager::setMusicVolume)
                SettingToggle("Sound effects", sfxEnabled, viewModel.audioManager::setSfxEnabled)
                VolumeSlider("Sound effects", sfx, viewModel.audioManager::setSfxVolume)
                VolumeSlider("Crowd", crowd, viewModel.audioManager::setCrowdVolume)
            }
            SettingsSection.LANGUAGE -> LanguageSetting(language, viewModel.localeManager::setLanguage)
        }
    }
}

@Composable
private fun VolumeSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text("${(value * 100).toInt()}%", color = FM_GREEN, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Slider(
            value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth().height(32.dp),
            colors = SliderDefaults.colors(thumbColor = FM_GREEN, activeTrackColor = FM_GREEN, inactiveTrackColor = Color.White.copy(.12f))
        )
    }
}

@Composable
private fun LanguageSetting(selected: String, onSelect: (String) -> Unit) {
    val languages = linkedMapOf("system" to "System default", "id" to "Bahasa Indonesia", "en" to "English", "pt" to "Português", "ja" to "日本語")
    languages.forEach { (tag, label) -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected == tag, { onSelect(tag) }); Text(label, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    } }
}

@Composable
fun SettingToggle(label: String, initialValue: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    var checked by remember(initialValue) { mutableStateOf(initialValue) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked, { checked = it; onCheckedChange(it) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = FM_GREEN))
    }
}
