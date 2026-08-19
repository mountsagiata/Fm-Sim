package com.mountsa.fmsimulation.ui.screens.dashboard.home.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.StandingEntity
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.components.ClubLogo

@Composable
fun StandingCard(
    modifier: Modifier = Modifier,
    standing: StandingEntity?,
    club: ClubEntity?
) {

    AppColumn(
        modifier = modifier,
        title = "STANDING"
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {

                Text(
                    "POSITION",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Text(
                    text = "#${standing?.position ?: "-"}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Pts: ${standing?.points ?: 0}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FM_GREEN
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                val currentPos =
                    standing?.position ?: 15

                val isGoingUp =
                    currentPos <= 5

                if (standing != null) {
                    Text(
                        text =
                            if (isGoingUp)
                                "▲"
                            else
                                "▼",
                        color =
                            if (isGoingUp)
                                FM_GREEN
                            else
                                Color.Red,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier.size(48.dp)
                ) {

                    if (club != null) {
                        ClubLogo(
                            clubId = club.id,
                            size = 48.dp
                        )
                    } else {
                        Text(
                            "📈",
                            fontSize = 24.sp,
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }
                }
            }
        }
    }
}