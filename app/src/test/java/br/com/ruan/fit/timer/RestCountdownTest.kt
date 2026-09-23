package br.com.ruan.fit.timer

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class RestCountdownTest {
    @Test fun countdownUsesElapsedTimeAcrossUiPausesAndCanBeCancelled() {
        val clock = mockk<ElapsedClock>()
        var now = 1_000L
        every { clock.now() } answers { now }
        val countdown = RestCountdown(clock)
        assertEquals(90, countdown.start(90).remainingSeconds)
        now += 30_100L
        assertEquals(60, countdown.current().remainingSeconds)
        now += 60_000L
        assertFalse(countdown.current().running)
        assertEquals(0, countdown.current().remainingSeconds)
        countdown.start(30)
        assertFalse(countdown.cancel().running)
    }
}
