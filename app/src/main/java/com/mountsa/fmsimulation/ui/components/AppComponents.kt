package com.mountsa.fmsimulation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppColumn(
    title: String,
    modifier: Modifier = Modifier,
    headerAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = Color(0xFF0F0F0F),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, start = 6.dp, end = 6.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.mountsa.fmsimulation.ui.localization.localized(title).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00FF5F),
                    letterSpacing = 0.5.sp
                )
                headerAction?.invoke()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF00FF5F),
    contentColor: Color = Color.Black,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(6.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Text(text = com.mountsa.fmsimulation.ui.localization.localized(text), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LineupPlayerCard(
    name: String,
    shirtNumber: Int,
    position: String,
    overall: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3))
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shirtNumber.toString(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name.uppercase(),
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 1.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = position, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = " | ", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
            Text(text = overall.toString(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun InboxCard(
    sender: String,
    subject: String,
    date: String,
    isUnread: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isUnread) Color(0xFF1A1A1A) else Color.Transparent,
        border = if (isUnread) BorderStroke(0.5.dp, Color(0xFF00FF5F).copy(alpha = 0.2f)) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUnread) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00FF5F)))
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = sender, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = date, fontSize = 9.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                Text(text = subject, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun ShopItemCard(
    title: String,
    description: String,
    price: String,
    modifier: Modifier = Modifier,
    icon: String = "💰",
    onPurchase: () -> Unit = {}
) {
    Surface(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF151515),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.White)
            Text(text = description, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, minLines = 2, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            AppButton(text = price, onClick = onPurchase, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun FacilityItem(name: String, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontSize = 10.sp, color = Color.White)
            Text(com.mountsa.fmsimulation.ui.localization.localized("${(progress * 100).toInt()}%"), fontSize = 9.sp, color = Color(0xFF00FF5F))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF00FF5F),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}
