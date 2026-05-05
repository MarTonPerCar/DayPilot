package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.model.ReactionType
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import kotlin.collections.listOf

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

            Text(
                text     = stringResource(reaction.emojiRes),
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

@Preview(showBackground = true)
@Composable
fun DayPilotReactionBadgePreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            DayPilotReactionBadgeRow(
                reactions = listOf("Ana" to ReactionType.CLAP, "Carlos" to ReactionType.FIRE, "Laura" to ReactionType.STAR)
            )
        }
    }
}