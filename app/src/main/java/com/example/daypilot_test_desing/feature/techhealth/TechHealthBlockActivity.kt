package com.example.daypilot_test_desing.feature.techhealth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

class TechHealthBlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_PACKAGE  = "package_name"
    }

    // Set to true before intentional exits so onUserLeaveHint doesn't re-show the screen
    private var intentionalExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // sin esto en algunos móviles la pantalla no aparece encima del bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
        val prefs   = AppPreferences(this)
        val theme   = DayPilotTheme.entries.find { it.name == prefs.themeId } ?: DayPilotTheme.SAGE_GREEN
        val isDark  = if (theme == DayPilotTheme.AMOLED) true else prefs.isDarkMode

        setContent {
            DayPilotTheme(theme = theme, darkMode = isDark) {
                BlockScreen(appName = appName, onGoHome = ::goHome)
            }
        }
    }

    @Deprecated("Deprecated in Java") // hay que mantenerlo aunque esté deprecated, el nuevo predictive back no aplica aquí
    override fun onBackPressed() {
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!intentionalExit) {
            startActivity(
                Intent(this, TechHealthBlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(EXTRA_APP_NAME, intent.getStringExtra(EXTRA_APP_NAME))
                    putExtra(EXTRA_PACKAGE,  intent.getStringExtra(EXTRA_PACKAGE))
                }
            )
        }
    }

    private fun goHome() {
        intentionalExit = true // importante: esto evita el bucle con onUserLeaveHint
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
        )
        finish()
    }
}

@Composable
private fun BlockScreen(appName: String, onGoHome: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Block,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(52.dp)
                )
            }

            Text(
                text       = stringResource(R.string.block_limit_reached),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center
            )

            if (appName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text     = appName,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style    = MaterialTheme.typography.labelLarge,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text      = stringResource(R.string.block_motivational),
                style     = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text      = stringResource(R.string.block_explanation, appName),
                    modifier  = Modifier.padding(16.dp),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick  = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text  = stringResource(R.string.block_go_home),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
