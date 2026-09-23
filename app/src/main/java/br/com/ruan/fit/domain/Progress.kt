package br.com.ruan.fit.domain

import br.com.ruan.fit.data.SetLog

data class PersonalRecord(val exerciseId: Long, val load: Double, val recordedAt: Long)

object Progress {
    // A load PR is the heaviest completed set with at least one repetition.
    fun records(logs: List<SetLog>): Map<Long, PersonalRecord> = logs
        .filter { it.completed && it.reps > 0 }
        .groupBy { it.exerciseId }
        .mapValues { (exerciseId, sets) ->
            val best = sets.maxWith(compareBy<SetLog> { it.load }.thenBy { -it.recordedAt })
            PersonalRecord(exerciseId, best.load, best.recordedAt)
        }

    fun isNewRecord(candidate: SetLog, previous: List<SetLog>): Boolean =
        candidate.completed && candidate.reps > 0 &&
            candidate.load > (records(previous.filter { it.recordedAt < candidate.recordedAt })[candidate.exerciseId]?.load ?: Double.NEGATIVE_INFINITY)

    fun suggestedLoad(targetReps: Int, plannedLoad: Double, recentSets: List<SetLog>, increment: Double = 2.5): Double? =
        if (recentSets.isNotEmpty() && recentSets.all { it.completed && it.reps >= targetReps && it.load >= plannedLoad }) plannedLoad + increment else null
}
