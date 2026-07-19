package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.core.ui.components.basic.HomeSectionIndicator
import com.example.daypilot_test_desing.core.data.model.DayProgress
import com.example.daypilot_test_desing.core.data.model.HomeSection
import com.example.daypilot_test_desing.core.data.model.HomeSectionData
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Composable
fun HomeMenuCard(
    section: HomeSection,
    data: HomeSectionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                section.accentColor.copy(alpha = 0.12f),
                                section.accentColor.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = section.accentColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .rotate(-15f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(section.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = section.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HomeSectionIndicator(data = data, accentColor = section.accentColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeMenuCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMenuCard(
                    section = HomeSection.CALENDAR,
                    data = HomeSectionData.Calendar(pendingTasks = 2, completedTasks = 3),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section = HomeSection.PROGRESS,
                    data = HomeSectionData.Progress(
                        data = List(7) { index ->
                            DayProgress(
                                dayOfMonth = index + 1,
                                points = listOf(8, 12, 5, 15, 20, 10, 7)[index],
                                steps = listOf(1200, 2500, 800, 3000, 2200, 1500, 900)[index],
                                tasksCompleted = listOf(3, 5, 2, 6, 8, 4, 2)[index]
                            )
                        }
                    ),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMenuCard(
                    section = HomeSection.HABITS,
                    data = HomeSectionData.Habits(stepsProgress = 0.6f, timerDone = true),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section = HomeSection.RIVALRY,
                    data = HomeSectionData.Rivalry(position = 10, totalFriends = 15),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}