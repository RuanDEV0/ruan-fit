package br.com.ruan.fit.domain

import br.com.ruan.fit.data.SetLog
import org.junit.Assert.*
import org.junit.Test

class ProgressTest {
    private fun set(id: Long, exercise: Long, load: Double, reps: Int, time: Long, completed: Boolean = true) =
        SetLog(id, 1, exercise, 1, "Agachamento", 1, load, reps, null, completed, time)

    @Test fun detectsLoadPrPerExerciseAndIgnoresIncompleteSets() {
        val previous = listOf(set(1, 10, 80.0, 5, 100), set(2, 10, 100.0, 5, 200, false), set(3, 11, 120.0, 5, 200))
        val candidate = set(4, 10, 82.5, 5, 300)
        assertTrue(Progress.isNewRecord(candidate, previous))
        assertEquals(80.0, Progress.records(previous)[10]?.load ?: 0.0, 0.001)
        assertFalse(Progress.isNewRecord(set(5, 10, 75.0, 5, 400), previous))
    }

    @Test fun suggestsProgressionOnlyWhenEverySetHitsTarget() {
        val good = listOf(set(1, 10, 50.0, 10, 100), set(2, 10, 50.0, 11, 200))
        assertEquals(52.5, Progress.suggestedLoad(10, 50.0, good) ?: 0.0, 0.001)
        assertNull(Progress.suggestedLoad(10, 50.0, good + set(3, 10, 50.0, 8, 300)))
        assertNull(Progress.suggestedLoad(10, 50.0, emptyList()))
    }
}
