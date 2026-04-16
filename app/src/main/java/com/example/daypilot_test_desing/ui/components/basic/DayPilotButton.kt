package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

// ── Press scale compartido ───────────────────────────────────────
@Composable
private fun pressScale(pressed: Boolean): Float {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )
    return scale
}

// ── Shake compartido ─────────────────────────────────────────────
@Composable
private fun rememberShakeOffset(trigger: Boolean): Float {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            repeat(4) {
                offset.animateTo(8f,  animationSpec = tween(50))
                offset.animateTo(-8f, animationSpec = tween(50))
            }
            offset.animateTo(0f, animationSpec = tween(50))
        }
    }
    return offset.value
}

// ── 1. Botón primario ────────────────────────────────────────────
@Composable
fun DayPilotButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    hasError: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)
    val shakeOffset = rememberShakeOffset(hasError)

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .offset(x = shakeOffset.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── 2. Botón de texto plano ──────────────────────────────────────
@Composable
fun DayPilotButtonText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)

    TextButton(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled,
        modifier = modifier.scale(scale),
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── 3. Botón outlined ────────────────────────────────────────────
@Composable
fun DayPilotButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)

    OutlinedButton(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── 4. Botón de error ────────────────────────────────────────────
@Composable
fun DayPilotButtonError(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor   = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── 5. FloatingActionButton ──────────────────────────────────────
@Composable
fun DayPilotFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = ""
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)

    FloatingActionButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor   = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp)
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

// ── 6. IconButton ────────────────────────────────────────────────
@Composable
fun DayPilotIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    var pressed by remember { mutableStateOf(false) }
    val scale = pressScale(pressed)

    IconButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.scale(scale)
    ) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(100)
                pressed = false
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}