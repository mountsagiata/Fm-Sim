package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.ui.components.AppColumn

@Composable
fun NewsCard(
    modifier: Modifier = Modifier,
    messages: List<InboxEntity>
) {
    AppColumn(
        modifier = modifier,
        title = "LATEST NEWS"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No news available", color = Color.Gray, fontSize = 10.sp)
                }
            } else {
                messages.take(4).forEach { message ->
                    NewsRowItem(message)
                }
            }
        }
    }
}

@Composable
fun NewsRowItem(message: InboxEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Dot color based on category
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(getCategoryColor(message.category), CircleShape)
            )
            
            Text(
                text = message.subject,
                fontSize = 9.sp,
                fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Normal,
                color = if (!message.isRead) Color.White else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (!message.isRead) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(4.dp)
                    .background(Color(0xFF448AFF), CircleShape)
            )
        }
    }
}

fun getCategoryColor(category: InboxCategory): Color {
    return when (category) {
        InboxCategory.BOARD -> Color(0xFFFF5252) // Red
        InboxCategory.MEDICAL -> Color(0xFFFF4081) // Pink
        InboxCategory.TRANSFER -> Color(0xFF448AFF) // Blue
        InboxCategory.MATCH -> Color(0xFF00E676) // Green
        InboxCategory.PLAYER -> Color(0xFFFFD740) // Yellow
        InboxCategory.SCOUTING -> Color(0xFF18FFFF) // Cyan
        InboxCategory.CONTRACT -> Color(0xFFFFAB40) // Orange
        InboxCategory.MEDIA -> Color(0xFFE0E0E0)
        InboxCategory.NEWS -> Color.LightGray
    }
}
