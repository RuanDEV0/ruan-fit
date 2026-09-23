package br.com.ruan.fit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ruan.fit.data.*
import br.com.ruan.fit.media.MediaGateway
import br.com.ruan.fit.timer.RestTimerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FitUiState(
    val plans: List<WorkoutPlan> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val planExercises: List<PlanExercise> = emptyList(),
    val sessions: List<WorkoutSession> = emptyList(),
    val sets: List<SetLog> = emptyList()
)

@HiltViewModel
class FitViewModel @Inject constructor(
    private val repository: FitRepository,
    val media: MediaGateway,
    val timer: RestTimerStore
) : ViewModel() {
    val ui = combine(repository.plans, repository.exercises, repository.planExercises, repository.sessions, repository.setLogs) { plans, exercises, links, sessions, sets ->
        FitUiState(plans, exercises, links, sessions, sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FitUiState())
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()

    fun clearError() { mutableError.value = null }
    private fun launchSave(block: suspend () -> Unit) = viewModelScope.launch {
        try { block(); mutableError.value = null }
        catch (error: Exception) { mutableError.value = "Falha ao salvar: ${error.localizedMessage ?: "tente novamente"}" }
    }

    fun savePlan(id: Long, name: String, split: String) = launchSave {
        require(name.isNotBlank()) { "Informe o nome da ficha" }
        repository.savePlan(WorkoutPlan(id, name.trim(), split.trim()))
    }
    fun deletePlan(plan: WorkoutPlan) = launchSave { repository.deletePlan(plan) }
    fun saveExercise(id: Long, name: String, group: String, instructions: String, demoUrl: String) = launchSave {
        require(name.isNotBlank()) { "Informe o nome do exercício" }
        repository.saveExercise(Exercise(id, name.trim(), group.trim(), instructions.trim(), demoUrl.trim().ifBlank { null }))
    }
    fun deleteExercise(exercise: Exercise) = launchSave { repository.deleteExercise(exercise) }
    fun saveLink(id: Long, planId: Long, exerciseId: Long, position: Int, sets: Int, reps: Int, load: Double) = launchSave {
        require(sets in 1..20 && reps in 1..100 && load >= 0) { "Confira séries, repetições e carga" }
        repository.savePlanExercise(PlanExercise(id, planId, exerciseId, position, sets, reps, load))
    }
    fun deleteLink(link: PlanExercise) = launchSave { repository.deletePlanExercise(link) }
    fun start(plan: WorkoutPlan) = launchSave {
        if (repository.activeSession() != null) error("Finalize o treino em andamento antes de iniciar outro")
        repository.startSession(plan)
    }
    fun finish(sessionId: Long) = launchSave { repository.finishSession(sessionId) }
    fun saveSet(sessionId: Long, planExerciseId: Long, exercise: Exercise, number: Int, load: Double, reps: Int, rpe: Double?) = launchSave {
        require(load >= 0 && reps in 1..100 && (rpe == null || rpe in 1.0..10.0)) { "Confira carga, repetições e RPE" }
        repository.saveSet(SetLog(sessionId = sessionId, exerciseId = exercise.id, planExerciseId = planExerciseId, exerciseName = exercise.name,
            setNumber = number, load = load, reps = reps, rpe = rpe, completed = true, recordedAt = System.currentTimeMillis()))
    }
}
