package com.mountsa.fmsimulation.ui.screens.dashboard.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.components.InboxCard
import com.mountsa.fmsimulation.ui.screens.dashboard.components.getDateString
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel

@Composable
fun InboxHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = uiState.inboxMessages
    val selectedMessage = uiState.selectedInboxMessage

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(1.5f), title = "INBOX") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(messages) { message ->
                    InboxCard(
                        sender = message.sender,
                        subject = message.subject,
                        date = getDateString(message.timestamp),
                        isUnread = !message.isRead,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = {
                            viewModel.selectInboxMessage(message)
                        }
                    )
                }
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "MESSAGE DETAILS") {
            if (selectedMessage != null) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text(
                        text = selectedMessage.subject,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "From: ${selectedMessage.sender}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = getDateString(selectedMessage.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = selectedMessage.message,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a message to read", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
