package br.com.ruan.fit.timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import br.com.ruan.fit.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class RestTimerService : Service() {
    @Inject lateinit var timer: RestTimerStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Descanso do treino", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            job?.cancel(); timer.publish(timer.countdown.cancel()); stopSelf(); return START_NOT_STICKY
        }
        val seconds = intent?.getIntExtra(EXTRA_SECONDS, 90) ?: 90
        // The deadline uses elapsedRealtime, so app suspension and screen rotation cannot pause it.
        timer.publish(timer.countdown.start(seconds))
        startForeground(NOTIFICATION_ID, notification(timer.state.value.remainingSeconds))
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                val state = timer.countdown.current()
                timer.publish(state)
                if (!state.running) break
                getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(state.remainingSeconds))
                delay(250)
            }
            alert()
            timer.publish(timer.countdown.cancel())
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun notification(seconds: Int): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, javaClass).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Descanso em andamento")
            .setContentText("${seconds}s restantes")
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", stop)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private suspend fun alert() {
        val audio = getSystemService(AudioManager::class.java)
        val original = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        try {
            // Lower music only during the alert and restore exactly the previous stream volume.
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (original / 3), 0)
            val vibrator = getSystemService(Vibrator::class.java)
            if (vibrator.hasVibrator()) vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
            RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
            delay(1800)
        } finally {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, original, 0)
        }
    }

    override fun onDestroy() { job?.cancel(); scope.cancel(); super.onDestroy() }

    companion object {
        private const val CHANNEL = "rest_timer"
        private const val NOTIFICATION_ID = 4001
        private const val EXTRA_SECONDS = "seconds"
        private const val ACTION_STOP = "stop"
        fun start(context: Context, seconds: Int) = context.startForegroundService(Intent(context, RestTimerService::class.java).putExtra(EXTRA_SECONDS, seconds))
        fun stop(context: Context) = context.startService(Intent(context, RestTimerService::class.java).setAction(ACTION_STOP))
    }
}
