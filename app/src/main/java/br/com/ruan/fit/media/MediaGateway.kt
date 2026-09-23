package br.com.ruan.fit.media

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.graphics.Bitmap
import br.com.ruan.fit.media.MediaListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(val available: Boolean = false, val title: String = "", val artist: String = "", val playing: Boolean = false, val artwork: Bitmap? = null)

@Singleton
class MediaGateway @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val component = ComponentName(context, MediaListenerService::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val mutable = MutableStateFlow(PlayerState())
    val state = mutable.asStateFlow()
    private val mutableAccess = MutableStateFlow(false)
    val access = mutableAccess.asStateFlow()
    private var controller: MediaController? = null
    private var observing = false

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() = refresh()
    }
    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { refresh() }

    fun hasAccess(): Boolean = context.getSystemService(NotificationManager::class.java).isNotificationListenerAccessGranted(component)

    fun refresh() {
        val granted = hasAccess()
        mutableAccess.value = granted
        if (!granted) { disconnect(); return }
        try {
            // Only active media sessions are queried; other notification contents are never inspected.
            if (!observing) {
                manager.addOnActiveSessionsChangedListener(sessionsChanged, component, handler)
                observing = true
            }
            val next = manager.getActiveSessions(component).firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: manager.getActiveSessions(component).firstOrNull()
            if (next?.sessionToken != controller?.sessionToken) {
                controller?.unregisterCallback(callback)
                controller = next
                next?.registerCallback(callback, handler)
            }
            publish()
        } catch (_: SecurityException) { disconnect() }
    }

    private fun disconnect() {
        controller?.unregisterCallback(callback)
        controller = null
        if (observing) {
            manager.removeOnActiveSessionsChangedListener(sessionsChanged)
            observing = false
        }
        mutable.value = PlayerState()
        mutableAccess.value = false
    }

    private fun publish() {
        val current = controller
        val metadata = current?.metadata
        mutable.value = if (current == null) PlayerState() else PlayerState(
            available = true,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty().ifBlank { "Faixa sem título" },
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            playing = current.playbackState?.state == PlaybackState.STATE_PLAYING,
            artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        )
    }

    fun playPause() { controller?.transportControls?.let { if (state.value.playing) it.pause() else it.play() } }
    fun next() { controller?.transportControls?.skipToNext() }
    fun previous() { controller?.transportControls?.skipToPrevious() }
    fun volume(up: Boolean) { audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI) }
}
