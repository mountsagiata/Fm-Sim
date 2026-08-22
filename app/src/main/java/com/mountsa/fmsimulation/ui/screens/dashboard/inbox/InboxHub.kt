package com.mountsa.fmsimulation.ui.screens.dashboard.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.mountsa.fmsimulation.domain.services.PressOption
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                    val pressOptions = remember(selectedMessage.id, selectedMessage.actionData) {
                        if (selectedMessage.actionData.isBlank()) emptyList() else runCatching {
                            Gson().fromJson<List<PressOption>>(
                                selectedMessage.actionData,
                                object : TypeToken<List<PressOption>>() {}.type
                            ) ?: emptyList()
                        }.getOrDefault(emptyList())
                    }
                    if (pressOptions.isNotEmpty() && !selectedMessage.isActioned) {
                        Spacer(Modifier.height(14.dp))
                        Text("YOUR ANSWER", color = FM_GREEN, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(6.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(pressOptions, key = { it.id }) { option ->
                                Button(
                                    onClick = { viewModel.answerPressInterview(selectedMessage, option) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(.07f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(option.text, color = Color.White, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(com.mountsa.fmsimulation.ui.localization.localized("Select a message to read"), color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
