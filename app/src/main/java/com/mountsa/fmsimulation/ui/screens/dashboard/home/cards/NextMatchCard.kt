package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_DARK_BG
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo
import com.mountsa.fmsimulation.ui.screens.dashboard.components.getDateString
import com.mountsa.fmsimulation.ui.screens.dashboard.components.getDayName
import com.mountsa.fmsimulation.ui.viewmodel.MatchUiModel

@Composable
fun NextMatchCard(
    modifier: Modifier = Modifier,
    nextMatch: MatchUiModel?,
    isLoading: Boolean = false,
    onContinue: () -> Unit
) {

    AppColumn(
        modifier = modifier,
        title = "NEXT MATCH TODAY"
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Text(
                text = if (nextMatch != null) {
                    "${getDayName(nextMatch.matchDate)}, ${
                        getDateString(nextMatch.matchDate)
                    }"
                } else {
                    "-"
                },
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (nextMatch != null) {
                        ClubLogo(
                            clubId = nextMatch.homeClubId,
                            size = 32.dp
                        )
                    } else {
                        Text(com.mountsa.fmsimulation.ui.localization.localized("🛡️"), fontSize = 24.sp)
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        "HOME",
                        fontSize = 8.sp,
                        color = Color(0xFF00BCD4),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text =
                        if (nextMatch != null)
                            "${nextMatch.homeShortName} vs ${nextMatch.awayShortName}"
                        else
                            "NO MATCH",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    if (nextMatch != null) {
                        ClubLogo(
                            clubId = nextMatch.awayClubId,
                            size = 32.dp
                        )
                    } else {
                        Text(com.mountsa.fmsimulation.ui.localization.localized("🛡️"), fontSize = 24.sp)
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        "AWAY",
                        fontSize = 8.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Surface(
                onClick = { if (!isLoading) onContinue() },
                color = if (isLoading) Color.Gray else Color(0xFF4CAF50),
                shape = RoundedCornerShape(4.dp),
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FM_DARK_BG,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Matchday",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FM_DARK_BG
                        )
                    }
                }
            }
        }
    }
}
