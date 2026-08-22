package com.mountsa.fmsimulation.ui.screens.dashboard.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun SettingsHub(viewModel: DashboardViewModel) {
    var showResetConfirm by remember { mutableStateOf(false) }
    val musicEnabled by viewModel.audioManager.musicEnabled.collectAsStateWithLifecycle()
    val sfxEnabled by viewModel.audioManager.sfxEnabled.collectAsStateWithLifecycle()
    val musicVolume by viewModel.audioManager.musicVolume.collectAsStateWithLifecycle()
    val sfxVolume by viewModel.audioManager.sfxVolume.collectAsStateWithLifecycle()
    val crowdVolume by viewModel.audioManager.crowdVolume.collectAsStateWithLifecycle()
    val language by viewModel.localeManager.language.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val compact = maxWidth < 720.dp
      val settingsContent: @Composable ColumnScope.() -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SettingToggle("Auto-Save after Match", true)
                SettingToggle("Show Attributes as Progress Bars", true)
                SettingToggle("Enable Match Commentary", true)
                SettingToggle("Show Player Faces", false)

                Spacer(Modifier.height(4.dp))
                Text("AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FM_GREEN)

                SettingToggle(
                    label = "Music",
                    initialValue = musicEnabled,
                    onCheckedChange = { viewModel.audioManager.setMusicEnabled(it) }
                )
                VolumeSetting("Music volume", musicVolume, viewModel.audioManager::setMusicVolume)
                VolumeSetting("Sound effects volume", sfxVolume, viewModel.audioManager::setSfxVolume)
                VolumeSetting("Crowd volume", crowdVolume, viewModel.audioManager::setCrowdVolume)

                Text("LANGUAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FM_GREEN)
                LanguageSetting(language, viewModel.localeManager::setLanguage)
                SettingToggle(
                    label = "Sound Effects (SFX)",
                    initialValue = sfxEnabled,
                    onCheckedChange = { viewModel.audioManager.setSfxEnabled(it) }
                )
                
                Spacer(Modifier.height(10.dp))
                
                Button(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESET CAREER DATA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
      }
      val aboutContent: @Composable ColumnScope.() -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Football Manager Simulation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text("Version 1.9.0", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Text("Developed by Mountsa", fontSize = 10.sp, color = Color.Gray)
                Text("© 2024 All Rights Reserved", fontSize = 10.sp, color = Color.Gray)
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Support & Feedback",
                    fontSize = 11.sp,
                    color = FM_GREEN,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
      }

      if (compact) {
        Column(
          modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AppColumn(modifier = Modifier.fillMaxWidth().heightIn(min = 560.dp), title = "GAME SETTINGS", content = settingsContent)
          AppColumn(modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp), title = "ABOUT", content = aboutContent)
        }
      } else {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AppColumn(modifier = Modifier.weight(2f), title = "GAME SETTINGS", content = settingsContent)
          AppColumn(modifier = Modifier.weight(1f), title = "ABOUT", content = aboutContent)
        }
      }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Career Data?") },
            text = { Text("This permanently deletes your current career, match history, and standings. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    viewModel.resetCareer()
                }) {
                    Text("RESET", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun VolumeSetting(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = Color.White)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, color = FM_GREEN)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
    }
}

@Composable
private fun LanguageSetting(selected: String, onLanguageSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val languages = linkedMapOf("system" to "System", "id" to "Bahasa Indonesia", "en" to "English", "pt" to "Português", "ja" to "日本語")
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(languages[selected] ?: "System", modifier = Modifier.weight(1f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { (tag, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    expanded = false
                    onLanguageSelected(tag)
                })
            }
        }
    }
}

@Composable
fun SettingToggle(label: String, initialValue: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    var checked by remember(initialValue) { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = FM_GREEN,
                checkedTrackColor = FM_GREEN.copy(alpha = 0.3f)
            )
        )
    }
}
