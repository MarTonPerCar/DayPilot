package com.example.daypilot_test_desing.core.ui.components.basic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme
import kotlinx.coroutines.delay

@Composable
private fun pressScale(pressed: Boolean): Float {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )
    return scale
}

@Composable
private fun rememberShakeOffset(trigger: Boolean): Float {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            repeat(4) {
                offset.animateTo(8f, animationSpec = tween(50))
                offset.animateTo(-8f, animationSpec = tween(50))
            }
            offset.animateTo(0f, animationSpec = tween(50))
        }
    }
    return offset.value
}

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
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                color = MaterialTheme.colorScheme.onError,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
        contentColor = MaterialTheme.colorScheme.onPrimary,
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

@Preview(showBackground = true)
@Composable
fun DayPilotButtonPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayPilotButtonPrimary(text = "Primary", onClick = {})
            DayPilotButtonOutlined(text = "Outlined", onClick = {})
            DayPilotButtonError(text = "Error", onClick = {})
            DayPilotButtonText(text = "Text", onClick = {})
        }
    }
}