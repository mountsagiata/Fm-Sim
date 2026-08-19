package com.mountsa.fmsimulation.ui.screens.dashboard.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import com.mountsa.fmsimulation.core.enums.TransferStatus
import com.mountsa.fmsimulation.ui.components.AppButton
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.TransferOfferUiModel
import java.util.Locale

@Composable
fun TransferHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val offers = uiState.transferOffers

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(2f), title = "TRANSFER MARKET") {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Text(
                    text = "INCOMING OFFERS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = FM_GREEN,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (offers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active offers", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(offers) { uiModel ->
                            TransferOfferItem(
                                uiModel = uiModel,
                                onAccept = { viewModel.updateTransferOfferStatus(uiModel.offer, TransferStatus.ACCEPTED) },
                                onReject = { viewModel.updateTransferOfferStatus(uiModel.offer, TransferStatus.REJECTED) }
                            )
                        }
                    }
                }
            }
        }
        AppColumn(modifier = Modifier.weight(1f), title = "SCOUTING") {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoutingReportCard("Top Targets", "3 Players Found")
                ScoutingReportCard("Youth Intake", "Next in 4 months")
                ScoutingReportCard("Transfer List", "12 Available")
            }
        }
    }
}

@Composable
fun TransferOfferItem(
    uiModel: TransferOfferUiModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val offer = uiModel.offer
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiModel.playerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "From: ${uiModel.buyerClubName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    color = when(offer.status) {
                        TransferStatus.PENDING -> Color.Yellow.copy(alpha = 0.2f)
                        TransferStatus.ACCEPTED -> FM_GREEN.copy(alpha = 0.2f)
                        TransferStatus.REJECTED -> Color.Red.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = offer.status.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(offer.status) {
                            TransferStatus.PENDING -> Color.Yellow
                            TransferStatus.ACCEPTED -> FM_GREEN
                            TransferStatus.REJECTED -> Color.Red
                            else -> Color.White
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offer: €${String.format(Locale.getDefault(), "%,d", offer.offerAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FM_GREEN
                )
                
                if (offer.status == TransferStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppButton(
                            text = "Reject",
                            onClick = onReject,
                            containerColor = Color.Red.copy(alpha = 0.8f),
                            contentColor = Color.White,
                            modifier = Modifier.height(28.dp)
                        )
                        AppButton(
                            text = "Accept",
                            onClick = onAccept,
                            containerColor = FM_GREEN,
                            contentColor = Color.Black,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoutingReportCard(title: String, subtitle: String) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 9.sp, color = Color.Gray)
        }
    }
}
