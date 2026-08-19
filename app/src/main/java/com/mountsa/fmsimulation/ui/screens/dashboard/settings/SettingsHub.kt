package com.mountsa.fmsimulation.ui.screens.dashboard.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun SettingsHub(viewModel: DashboardViewModel) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(2f), title = "GAME SETTINGS") {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SettingToggle("Auto-Save after Match", true)
                SettingToggle("Show Attributes as Progress Bars", true)
                SettingToggle("Enable Match Commentary", true)
                SettingToggle("Show Player Faces", false)
                
                Spacer(Modifier.height(10.dp))
                
                Button(
                    onClick = { /* Reset Game */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESET CAREER DATA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "ABOUT") {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Football Manager Simulation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text("Version 1.9.0", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Text("Developed by Mountsa", fontSize = 10.sp, color = Color.Gray)
                Text("© 2024 All Rights Reserved", fontSize = 10.sp, color = Color.Gray)
                
                Spacer(Modifier.weight(1f))
                
                Text(
                    "Support & Feedback",
                    fontSize = 11.sp,
                    color = FM_GREEN,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingToggle(label: String, initialValue: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.White)
        Switch(
            checked = initialValue,
            onCheckedChange = { },
            colors = SwitchDefaults.colors(
                checkedThumbColor = FM_GREEN,
                checkedTrackColor = FM_GREEN.copy(alpha = 0.3f)
            )
        )
    }
}
