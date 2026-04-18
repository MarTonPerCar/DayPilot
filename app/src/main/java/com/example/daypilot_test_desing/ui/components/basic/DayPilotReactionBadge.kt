package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayPilotReactionBadge(
    name: String,
    reaction: ReactionType,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box {
            DayPilotAvatar(
                name      = name,
                avatarUrl = avatarUrl,
                size      = 36
            )
            // Emoji encima esquina inferior derecha
            Text(
                text     = reaction.emoji,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
            )
        }
        Text(
            text  = name.split(" ").first(), // solo el nombre
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayPilotReactionBadgeRow(
    reactions: List<Pair<String, ReactionType>>,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        reactions.forEach { (name, reaction) ->
            DayPilotReactionBadge(
                name     = name,
                reaction = reaction
            )
        }
    }
}