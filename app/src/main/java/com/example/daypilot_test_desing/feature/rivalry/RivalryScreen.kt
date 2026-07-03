package com.example.daypilot_test_desing.feature.rivalry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotSectionHeader
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.core.ui.components.cards.CurrentUserRankingCard
import com.example.daypilot_test_desing.core.ui.components.cards.PodiumCard
import com.example.daypilot_test_desing.core.ui.components.cards.RankingCard
import com.example.daypilot_test_desing.core.data.model.PodiumEntry
import com.example.daypilot_test_desing.core.data.model.RankingData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivalryScreen(
    currentUserName: String,
    currentUserId: String,
    currentUserPosition: Int,
    currentUserPoints: Int,
    currentUserStreak: Int,
    currentUserLevel: Int,
    ranking: List<RankingData>,
    onBack: () -> Unit
) {
    val hasFriends = ranking.any { it.id != currentUserId }

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
                    name     = currentUserName,
                    position = currentUserPosition,
                    points   = currentUserPoints,
                    streak   = currentUserStreak,
                    level    = currentUserLevel
                )
            }

            // ── Sección de ranking ────────────────────────────────
            if (!hasFriends) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = stringResource(R.string.rivalry_no_friends),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    DayPilotSectionHeader(title = stringResource(R.string.rivalry_ranking))
                    Spacer(Modifier.height(4.dp))
                }

                if (ranking.size >= 3) {
                    // ── Podio ─────────────────────────────────────
                    item {
                        PodiumCard(
                            first = PodiumEntry(
                                name          = ranking[0].name,
                                points        = ranking[0].points,
                                streak        = ranking[0].streak,
                                avatarUrl     = ranking[0].avatarUrl,
                                isCurrentUser = ranking[0].id == currentUserId
                            ),
                            second = PodiumEntry(
                                name          = ranking[1].name,
                                points        = ranking[1].points,
                                streak        = ranking[1].streak,
                                avatarUrl     = ranking[1].avatarUrl,
                                isCurrentUser = ranking[1].id == currentUserId
                            ),
                            third = PodiumEntry(
                                name          = ranking[2].name,
                                points        = ranking[2].points,
                                streak        = ranking[2].streak,
                                avatarUrl     = ranking[2].avatarUrl,
                                isCurrentUser = ranking[2].id == currentUserId
                            )
                        )
                    }
                    // ── Posiciones 4+ ─────────────────────────────
                    if (ranking.size > 3) {
                        item { Spacer(Modifier.height(4.dp)) }
                        itemsIndexed(ranking.drop(3)) { index, entry ->
                            RankingCard(
                                name          = entry.name,
                                position      = index + 4,
                                points        = entry.points,
                                streak        = entry.streak,
                                level         = entry.level,
                                avatarUrl     = entry.avatarUrl,
                                isCurrentUser = entry.id == currentUserId
                            )
                        }
                    }
                } else {
                    // ── < 3 participantes: lista completa sin podio ─
                    itemsIndexed(ranking) { index, entry ->
                        RankingCard(
                            name          = entry.name,
                            position      = index + 1,
                            points        = entry.points,
                            streak        = entry.streak,
                            level         = entry.level,
                            avatarUrl     = entry.avatarUrl,
                            isCurrentUser = entry.id == currentUserId
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}