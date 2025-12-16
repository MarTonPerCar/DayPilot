package com.example.daypilot.main.mainZone.habits.tech

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class TechHealthForegroundService : Service() {

    companion object {
        private const val ACTION_START = "tech_health_action_start"
        private const val ACTION_STOP = "tech_health_action_stop"
        private const val EXTRA_TICK_MS = "tick_ms"

        fun start(context: Context, tickMs: Long = 5_000L) {
            val i = Intent(context, TechHealthForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TICK_MS, tickMs)
            }
            if (Build.VERSION.SDK_INT >= 26) {
                ContextCompat.startForegroundService(context, i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            val i = Intent(context, TechHealthForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(i)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var loopJob: Job? = null

    private lateinit var monitor: TechHealthMonitor
    private var noWorkTicks = 0

    override fun onCreate() {
        super.onCreate()

        TechHealthNotifier.ensureChannels(this)
        monitor = TechHealthMonitor(applicationContext)

        startForeground(9001, TechHealthNotifier.ongoingMonitor(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val tickMs = (intent?.getLongExtra(EXTRA_TICK_MS, 5_000L) ?: 5_000L)
            .coerceIn(1_000L, 60_000L)

        if (loopJob?.isActive != true) {
            loopJob = scope.launch {
                while (isActive) {
                    val hasWork = runCatching { monitor.tick() }.getOrDefault(false)
                    android.util.Log.d("TechHealth", "tick() ok, hasWork=$hasWork")

                    // Si no hay permisos/restricciones activas, nos paramos solos para no molestar
                    if (!hasWork) {
                        noWorkTicks++
                        if (noWorkTicks >= 12) { // ~1 min si tick=5s
                            stopSelf()
                            break
                        }
                    } else {
                        noWorkTicks = 0
                    }

                    delay(tickMs)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        loopJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}