package br.com.ruan.fit.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import br.com.ruan.fit.timer.RestTimerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: FitViewModel by viewModels()
    private var showMediaExplanation by mutableStateOf(false)
    private var showNotificationExplanation by mutableStateOf(false)
    private var showNotificationDenied by mutableStateOf(false)
    private var pendingRestSeconds: Int? = null
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingRestSeconds?.let { RestTimerService.start(this, it) }
        else showNotificationDenied = true
        pendingRestSeconds = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(245, 247, 242)
        window.navigationBarColor = android.graphics.Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContent {
            FitTheme {
                FitApp(model, onRestStart = ::startRest, onRestStop = { RestTimerService.stop(this) },
                    onMediaPermission = { showMediaExplanation = true })
                if (showMediaExplanation) AlertDialog(
                    onDismissRequest = { showMediaExplanation = false }, title = { Text("Acesso ao controle de mídia") },
                    text = { Text("Para mostrar e controlar a música de outro app durante o treino, o Android exige acesso às notificações. O Ruan Fit consulta apenas sessões de mídia; não lê nem guarda outras notificações. Você pode revogar o acesso nas configurações.") },
                    confirmButton = { TextButton(onClick = {
                        showMediaExplanation = false
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }) { Text("Abrir configurações") } },
                    dismissButton = { TextButton(onClick = { showMediaExplanation = false }) { Text("Agora não") } }
                )
                if (showNotificationDenied) AlertDialog(
                    onDismissRequest = { showNotificationDenied = false },
                    title = { Text("Permissão não concedida") },
                    text = { Text("O descanso não foi iniciado. Permita notificações para manter o cronômetro visível em segundo plano.") },
                    confirmButton = { TextButton(onClick = { showNotificationDenied = false }) { Text("Entendi") } }
                )
                if (showNotificationExplanation) AlertDialog(
                    onDismissRequest = { showNotificationExplanation = false; pendingRestSeconds = null },
                    title = { Text("Notificação do descanso") },
                    text = { Text("O cronômetro precisa mostrar o tempo restante enquanto o app está em segundo plano e avisar quando terminar.") },
                    confirmButton = { TextButton(onClick = {
                        showNotificationExplanation = false
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text("Permitir") } },
                    dismissButton = { TextButton(onClick = { showNotificationExplanation = false; pendingRestSeconds = null }) { Text("Cancelar") } }
                )
            }
        }
    }

    override fun onResume() { super.onResume(); model.media.refresh() }

    private fun startRest(seconds: Int) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingRestSeconds = seconds
            showNotificationExplanation = true
        } else RestTimerService.start(this, seconds)
    }
}
