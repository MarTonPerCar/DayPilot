package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Tipos de reacción ────────────────────────────────────────────
enum class ReactionType(val emoji: String, val label: String) {
    FIRE("🔥", "Fuego"),
    CLAP("👏", "Aplausos"),
    STRONG("💪", "Fuerza"),
    STAR("⭐", "Estrella")
}

// ── Barra de reacciones ──────────────────────────────────────────
@Composable
fun DayPilotReactionBar(
    selectedReaction: ReactionType? = null,
    onReact: (ReactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReactionType.entries.forEach { reaction ->
            ReactionButton(
                reaction   = reaction,
                isSelected = reaction == selectedReaction,
                onReact    = onReact
            )
        }
    }
}

// ── Botón individual de reacción ─────────────────────────────────
@Composable
fun ReactionButton(
    reaction: ReactionType,
    isSelected: Boolean,
    onReact: (ReactionType) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed  -> 1.3f
            isSelected -> 1.15f
            else       -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "reaction_scale_${reaction.name}"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isPressed) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "reaction_offset_${reaction.name}"
    )

    Box(contentAlignment = Alignment.TopCenter) {
        // Tooltip
        if (showTooltip) {
            Box(
                modifier = Modifier
                    .offset(y = (-32).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = reaction.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Botón
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(scale)
                .offset(y = offsetY.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface
                )
                .clickable {
                    scope.launch {
                        isPressed   = true
                        showTooltip = true
                        delay(150)
                        isPressed = false
                        onReact(reaction)
                        delay(1000)
                        showTooltip = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text      = reaction.emoji,
                fontSize  = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Contador de reacciones ───────────────────────────────────────
@Composable
fun DayPilotReactionSummary(
    reactions: Map<ReactionType, Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.filter { it.value > 0 }.forEach { (reaction, count) ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = reaction.emoji, fontSize = 14.sp)
                Text(
                    text  = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DayPilotReactionsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        var selected by remember { mutableStateOf<ReactionType?>(null) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Reacciona al resumen",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DayPilotReactionBar(
                selectedReaction = selected,
                onReact = { selected = it }
            )

            DayPilotReactionSummary(
                reactions = mapOf(
                    ReactionType.FIRE   to 3,
                    ReactionType.CLAP   to 1,
                    ReactionType.STRONG to 5,
                    ReactionType.STAR   to 2
                )
            )
        }
    }
}