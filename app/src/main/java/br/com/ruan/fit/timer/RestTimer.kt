package br.com.ruan.fit.timer

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

fun interface ElapsedClock { fun now(): Long }

data class RestState(val running: Boolean = false, val remainingSeconds: Int = 0, val durationSeconds: Int = 0)

class RestCountdown(private val clock: ElapsedClock) {
    private var deadline = 0L
    private var duration = 0
    fun start(seconds: Int): RestState {
        duration = seconds.coerceIn(5, 3600)
        deadline = clock.now() + duration * 1000L
        return current()
    }
    fun current(): RestState {
        if (deadline == 0L) return RestState()
        val remaining = ((deadline - clock.now() + 999L) / 1000L).coerceAtLeast(0).toInt()
        return RestState(remaining > 0, remaining, duration)
    }
    fun cancel(): RestState { deadline = 0L; duration = 0; return RestState() }
}

@Singleton
class RestTimerStore @Inject constructor() {
    val countdown = RestCountdown(ElapsedClock { SystemClock.elapsedRealtime() })
    private val mutable = MutableStateFlow(RestState())
    val state = mutable.asStateFlow()
    fun publish(value: RestState) { mutable.value = value }
}
