package br.com.ruan.fit.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FitRepository @Inject constructor(private val dao: FitDao) {
    val plans: Flow<List<WorkoutPlan>> = dao.plans()
    val exercises: Flow<List<Exercise>> = dao.exercises()
    val planExercises: Flow<List<PlanExercise>> = dao.planExercises()
    val sessions: Flow<List<WorkoutSession>> = dao.sessions()
    val setLogs: Flow<List<SetLog>> = dao.setLogs()

    suspend fun savePlan(value: WorkoutPlan) = withContext(Dispatchers.IO) {
        if (value.id == 0L) dao.insertPlan(value) else { dao.updatePlan(value); value.id }
    }
    suspend fun deletePlan(value: WorkoutPlan) = withContext(Dispatchers.IO) { dao.deletePlan(value) }
    suspend fun saveExercise(value: Exercise) = withContext(Dispatchers.IO) {
        if (value.id == 0L) dao.insertExercise(value) else { dao.updateExercise(value); value.id }
    }
    suspend fun deleteExercise(value: Exercise) = withContext(Dispatchers.IO) { dao.deleteExercise(value) }
    suspend fun savePlanExercise(value: PlanExercise) = withContext(Dispatchers.IO) {
        if (value.id == 0L) dao.insertPlanExercise(value) else { dao.updatePlanExercise(value); value.id }
    }
    suspend fun deletePlanExercise(value: PlanExercise) = withContext(Dispatchers.IO) { dao.deletePlanExercise(value) }
    suspend fun activeSession() = withContext(Dispatchers.IO) { dao.activeSession() }
    suspend fun startSession(plan: WorkoutPlan): Long = withContext(Dispatchers.IO) {
        dao.insertSession(WorkoutSession(planId = plan.id, planName = plan.name, startedAt = System.currentTimeMillis()))
    }
    suspend fun finishSession(id: Long) = withContext(Dispatchers.IO) { dao.finishSession(id, System.currentTimeMillis()) }
    suspend fun saveSet(value: SetLog) = withContext(Dispatchers.IO) {
        val existing = dao.setFor(value.sessionId, value.planExerciseId, value.setNumber)
        if (existing == null) dao.insertSet(value) else dao.updateSet(value.copy(id = existing.id))
    }
}
