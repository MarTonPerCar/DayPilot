package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.ReceivedReaction
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

@Composable
fun DayPilotReactionBadge(
    modifier: Modifier = Modifier,
    name: String,
    reaction: ReactionType,
    avatarUrl: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box {
            DayPilotAvatar(
                name = name,
                avatarUrl = avatarUrl,
                size = 36
            )

            Text(
                text = stringResource(reaction.emojiRes),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
            )
        }
        Text(
            text = name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayPilotReactionBadgeRow(
    reactions: List<ReceivedReaction>,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.forEach { r ->
            DayPilotReactionBadge(
                name      = r.fromName,
                reaction  = r.reaction,
                avatarUrl = r.avatarUrl
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayPilotReactionBadgePreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)) {
            DayPilotReactionBadgeRow(
                reactions = listOf(
                    ReceivedReaction("Ana",    ReactionType.CLAP),
                    ReceivedReaction("Carlos", ReactionType.FIRE),
                    ReceivedReaction("Laura",  ReactionType.STAR)
                )
            )
        }
    }
}