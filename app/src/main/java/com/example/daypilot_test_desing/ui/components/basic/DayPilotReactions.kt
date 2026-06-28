package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ── Barra de reacciones ──────────────────────────────────────────
@Composable
fun DayPilotReactionBar(
    modifier: Modifier = Modifier,
    selectedReaction: ReactionType? = null,
    onReact: (ReactionType) -> Unit
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
                reaction = reaction,
                isSelected = reaction == selectedReaction,
                onReact = onReact
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
            isPressed -> 1.3f
            isSelected -> 1.15f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reaction_scale_${reaction.name}"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isPressed) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reaction_offset_${reaction.name}"
    )

    Box(contentAlignment = Alignment.TopCenter) {
        if (showTooltip) {
            Box(
                modifier = Modifier
                    .offset(y = (-32).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(reaction.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

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
                        isPressed = true
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
                text = stringResource(reaction.emojiRes),
                fontSize = 20.sp,
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
                Text(text = stringResource(reaction.emojiRes), fontSize = 14.sp)
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Botón de reacción flotante ───────────────────────────────────
@Composable
fun DayPilotReactionButton(
    selectedReaction: ReactionType? = null,
    onReact: (ReactionType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(200),
        label = "icon_rotation"
    )

    Box(modifier = modifier) {
        // ── Botón + ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (selectedReaction != null)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.primary
                )
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
        }

        // ── Panel flotante ────────────────────────────────────
        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, -120)
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(150)) + scaleIn(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        initialScale = 0f,
                        transformOrigin = TransformOrigin(1f, 1f)
                    ),
                    exit = fadeOut(tween(100)) + scaleOut(
                        animationSpec = tween(100),
                        targetScale = 0f,
                        transformOrigin = TransformOrigin(1f, 1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReactionType.entries.forEach { reaction ->
                            var pressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (pressed) 1.4f else 1f,
                                animationSpec = tween(150),
                                label = "reaction_scale_${reaction.name}",
                                finishedListener = {
                                    if (pressed) {
                                        onReact(reaction)
                                        expanded = false
                                        pressed = false
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedReaction == reaction)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            Color.Transparent
                                    )
                                    .clickable { pressed = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(reaction.emojiRes),
                                    fontSize = 20.sp,
                                    modifier = Modifier.scale(scale)
                                )
                            }
                        }
                    }
                }
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
                text = "Reacciona al resumen",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DayPilotReactionBar(
                selectedReaction = selected,
                onReact = { selected = it }
            )

            DayPilotReactionSummary(
                reactions = mapOf(
                    ReactionType.FIRE to 3,
                    ReactionType.CLAP to 1,
                    ReactionType.STRONG to 5,
                    ReactionType.STAR to 2
                )
            )
        }
    }
}