package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotSectionHeader
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.ui.components.cards.CurrentUserRankingCard
import com.example.daypilot_test_desing.ui.components.cards.PodiumCard
import com.example.daypilot_test_desing.ui.components.cards.RankingCard
import com.example.daypilot_test_desing.data.model.PodiumEntry
import com.example.daypilot_test_desing.data.model.RankingData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivalryScreen(
    currentUserName: String,
    currentUserPosition: Int,
    currentUserPoints: Int,
    currentUserStreak: Int,
    ranking: List<RankingData>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DayPilotTopBar(
                title = stringResource(R.string.rivalry_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Tu posición ──────────────────────────────────────
            item {
                CurrentUserRankingCard(
                    name = currentUserName,
                    position = currentUserPosition,
                    points = currentUserPoints,
                    streak = currentUserStreak
                )
            }

            // ── Podio ────────────────────────────────────────────
            if (ranking.size >= 3) {
                item {
                    DayPilotSectionHeader(
                        title = stringResource(R.string.rivalry_ranking)
                    )
                    Spacer(Modifier.height(4.dp))
                    PodiumCard(
                        first = PodiumEntry(
                            name = ranking[0].name,
                            points = ranking[0].points,
                            streak = ranking[0].streak,
                            avatarUrl = ranking[0].avatarUrl,
                            isCurrentUser = ranking[0].name == currentUserName
                        ),
                        second = PodiumEntry(
                            name = ranking[1].name,
                            points = ranking[1].points,
                            streak = ranking[1].streak,
                            avatarUrl = ranking[1].avatarUrl,
                            isCurrentUser = ranking[1].name == currentUserName
                        ),
                        third = PodiumEntry(
                            name = ranking[2].name,
                            points = ranking[2].points,
                            streak = ranking[2].streak,
                            avatarUrl = ranking[2].avatarUrl,
                            isCurrentUser = ranking[2].name == currentUserName
                        )
                    )
                }
            }

            // ── Resto del ranking ────────────────────────────────
            if (ranking.size > 3) {
                item {
                    Spacer(Modifier.height(4.dp))
                }
                itemsIndexed(ranking.drop(3)) { index, entry ->
                    RankingCard(
                        name = entry.name,
                        position = index + 4,
                        points = entry.points,
                        streak = entry.streak,
                        avatarUrl = entry.avatarUrl
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}