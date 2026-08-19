package com.mountsa.fmsimulation.ui.screens.dashboard.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountsa.fmsimulation.ui.components.AppColumn
import com.mountsa.fmsimulation.ui.screens.dashboard.FM_GREEN
import com.mountsa.fmsimulation.ui.screens.dashboard.home.FixtureCard
import com.mountsa.fmsimulation.ui.viewmodel.MatchUiModel

@Composable
fun UpcomingFixturesSection(
    modifier: Modifier = Modifier,
    fixtures: List<MatchUiModel>,
    onNavigateToCalendar: () -> Unit
) {

    val containerBgColor =
        Color(0xFF0F0F0F)

    AppColumn(
        modifier = modifier,
        title = "UPCOMING FIXTURES",
        headerAction = {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                modifier =
                    Modifier.clickable {
                        onNavigateToCalendar()
                    }
            ) {

                Text(
                    "View All",
                    fontSize = 10.sp,
                    color = FM_GREEN
                )

                Icon(
                    Icons.AutoMirrored.Filled.NavigateNext,
                    null,
                    tint = FM_GREEN,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 6.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                items(fixtures) { match ->

                    FixtureCard(
                        date = match.matchDate,
                        opponent =
                            match.opponentShortName,
                        opponentClubId =
                            match.opponentClubId
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(20.dp)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(
                                    containerBgColor,
                                    Color.Transparent
                                )
                            )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(20.dp)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    containerBgColor
                                )
                            )
                    )
            )
        }
    }
}