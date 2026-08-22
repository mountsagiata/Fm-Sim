package com.mountsa.fmsimulation.ui.screens.dashboard.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mountsa.fmsimulation.ui.screens.dashboard.components.PlayerAvatar
import com.mountsa.fmsimulation.ui.viewmodel.DashboardViewModel
import com.mountsa.fmsimulation.ui.viewmodel.TransferOfferUiModel
import java.util.Locale

@Composable
fun TransferHub(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val offers = uiState.transferOffers
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var shortlist by remember { mutableStateOf(setOf<Long>()) }

    Column(Modifier.fillMaxSize()) {
    TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
        listOf("OFFERS", "PLAYER MARKET", "SHORTLIST").forEachIndexed { index, title -> Tab(tab == index, { tab = index }, text = { Text(title, fontSize = 10.sp) }) }
    }
    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppColumn(modifier = Modifier.weight(2f), title = "TRANSFER MARKET") {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Text(
                    text = when(tab) { 0 -> "INCOMING OFFERS"; 1 -> "AVAILABLE PLAYERS"; else -> "SHORTLIST" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = FM_GREEN,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (tab != 0) OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), singleLine = true, placeholder = { Text("Search name or position") })
                val marketPlayers = allPlayers.filter {
                    it.clubId != uiState.club?.id &&
                        (query.isBlank() || it.name.contains(query.trim(), true) || it.shortName.contains(query.trim(), true) || it.position.contains(query.trim(), true)) &&
                        (tab != 2 || it.id in shortlist)
                }
                if (tab == 0 && offers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(com.mountsa.fmsimulation.ui.localization.localized("No active offers"), color = Color.Gray, fontSize = 12.sp)
                    }
                } else if (tab == 0) {
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
                } else LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(marketPlayers) { player ->
                        ListItem(
                            leadingContent = { PlayerAvatar(player, 34.dp) },
                            headlineContent = { Text(player.shortName, color = Color.White, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${player.position} • OVR ${player.overall} • €${String.format(Locale.getDefault(), "%,d", player.marketValue)}", color = Color.Gray) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { shortlist = if (player.id in shortlist) shortlist - player.id else shortlist + player.id }) {
                                        Text(if (player.id in shortlist) "REMOVE" else "SHORTLIST", color = Color.Gray, fontSize = 8.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.buyPlayer(player.id) },
                                        enabled = (uiState.club?.budget ?: 0L) >= (player.releaseClause.takeIf { it > 0L } ?: player.marketValue),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FM_GREEN)
                                    ) { Text("BUY", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.White.copy(.025f))
                        )
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
                            text = com.mountsa.fmsimulation.ui.localization.localized("Reject"),
                            onClick = onReject,
                            containerColor = Color.Red.copy(alpha = 0.8f),
                            contentColor = Color.White,
                            modifier = Modifier.height(28.dp)
                        )
                        AppButton(
                            text = com.mountsa.fmsimulation.ui.localization.localized("Accept"),
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
