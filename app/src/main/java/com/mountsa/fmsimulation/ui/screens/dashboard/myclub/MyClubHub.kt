package com.mountsa.fmsimulation.ui.screens.dashboard.myclub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.components.FacilityItem
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun MyClubHub(viewModel: DashboardViewModel) {
    val club by viewModel.club.collectAsStateWithLifecycle()
    
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(1.5f), title = "CLUB INFO") {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ClubDetailRow("Stadium", club?.stadium ?: "N/A")
                ClubDetailRow("Reputation", "${club?.reputation}/100")
                ClubDetailRow("Youth Academy", "Level 4")
                ClubDetailRow("Training Ground", "Excellent")
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "FACILITIES") {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FacilityItem("Medical Center", 0.85f)
                FacilityItem("Training Pitch", 0.90f)
                FacilityItem("Data Hub", 0.60f)
                FacilityItem("Youth Academy", 0.75f)
            }
        }
    }
}

@Composable
fun ClubDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
