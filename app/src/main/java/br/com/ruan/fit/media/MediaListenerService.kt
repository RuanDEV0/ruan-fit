package br.com.ruan.fit.media

import android.service.notification.NotificationListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaListenerService : NotificationListenerService() {
    @Inject lateinit var media: MediaGateway
    override fun onListenerConnected() { super.onListenerConnected(); media.refresh() }
    override fun onListenerDisconnected() { media.refresh(); super.onListenerDisconnected() }
}
