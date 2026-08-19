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
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import java.util.Locale

@Composable
fun FinanceCard(
    modifier: Modifier = Modifier,
    club: ClubEntity?
) {

    AppColumn(
        modifier = modifier,
        title = "FINANCE"
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 4.dp)
            ) {

                Text(
                    "Budget",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Text(
                    text = "€${
                        String.format(
                            Locale.getDefault(),
                            "%,d",
                            club?.budget ?: 0L
                        )
                    }",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FM_GREEN
                )
            }

            Text(
                "💹",
                modifier = Modifier.align(Alignment.CenterEnd),
                fontSize = 32.sp
            )
        }
    }
}