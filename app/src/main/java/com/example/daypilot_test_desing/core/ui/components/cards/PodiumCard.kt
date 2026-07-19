package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotAvatar
import com.example.daypilot_test_desing.core.data.model.PodiumEntry
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Composable
fun PodiumCard(
    first: PodiumEntry,
    second: PodiumEntry,
    third: PodiumEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                PodiumSlot(
                    entry = second,
                    medal = "🥈",
                    position = 2,
                    barHeight = 90.dp,
                    barColor = Color(0xFFB0BEC5),
                    modifier = Modifier.weight(1f)
                )

                PodiumSlot(
                    entry = first,
                    medal = "🥇",
                    position = 1,
                    barHeight = 130.dp,
                    barColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )

                PodiumSlot(
                    entry = third,
                    medal = "🥉",
                    position = 3,
                    barHeight = 70.dp,
                    barColor = Color(0xFFCD7F32),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: PodiumEntry,
    medal: String,
    position: Int,
    barHeight: Dp,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = entry.name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (entry.isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = medal,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            barColor.copy(alpha = 0.9f),
                            barColor.copy(alpha = 0.5f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DayPilotAvatar(
                    name = entry.name,
                    avatarUrl = entry.avatarUrl,
                    size = if (position == 1) 48 else 40
                )

                Text(
                    text = "${entry.points}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${entry.streak}🔥",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PodiumCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            PodiumCard(
                first = PodiumEntry("Ana López", 520, 14),
                second = PodiumEntry("Carlos Ruiz", 480, 9),
                third = PodiumEntry("Laura Sánchez", 430, 6)
            )
        }
    }
}