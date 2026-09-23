package br.com.ruan.fit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val split: String,
    // Reserved for a future playlist association; no provider integration in the MVP.
    val playlistUri: String? = null
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val instructions: String,
    val demoUrl: String? = null
)

@Entity(tableName = "plan_exercises", indices = [Index("planId"), Index("exerciseId")],
    foreignKeys = [
        ForeignKey(entity = WorkoutPlan::class, parentColumns = ["id"], childColumns = ["planId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE)
    ])
data class PlanExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val exerciseId: Long,
    val position: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetLoad: Double
)

@Entity(tableName = "sessions", indices = [Index("planId")])
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val planName: String,
    val startedAt: Long,
    val endedAt: Long? = null
)

@Entity(tableName = "set_logs", indices = [Index("sessionId"), Index("exerciseId"), Index(value = ["sessionId", "planExerciseId", "setNumber"], unique = true)],
    foreignKeys = [ForeignKey(entity = WorkoutSession::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)])
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val planExerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val load: Double,
    val reps: Int,
    val rpe: Double?,
    val completed: Boolean,
    val recordedAt: Long
)

@Dao
interface FitDao {
    @Query("SELECT * FROM plans ORDER BY name") fun plans(): Flow<List<WorkoutPlan>>
    @Query("SELECT * FROM exercises ORDER BY name") fun exercises(): Flow<List<Exercise>>
    @Query("SELECT * FROM plan_exercises ORDER BY position, id") fun planExercises(): Flow<List<PlanExercise>>
    @Query("SELECT * FROM sessions ORDER BY startedAt DESC") fun sessions(): Flow<List<WorkoutSession>>
    @Query("SELECT * FROM set_logs ORDER BY recordedAt, id") fun setLogs(): Flow<List<SetLog>>
    @Query("SELECT * FROM sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1") suspend fun activeSession(): WorkoutSession?
    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId AND planExerciseId = :planExerciseId AND setNumber = :setNumber LIMIT 1") suspend fun setFor(sessionId: Long, planExerciseId: Long, setNumber: Int): SetLog?
    @Insert suspend fun insertPlan(value: WorkoutPlan): Long
    @Update suspend fun updatePlan(value: WorkoutPlan)
    @Delete suspend fun deletePlan(value: WorkoutPlan)
    @Insert suspend fun insertExercise(value: Exercise): Long
    @Update suspend fun updateExercise(value: Exercise)
    @Delete suspend fun deleteExercise(value: Exercise)
    @Insert suspend fun insertPlanExercise(value: PlanExercise): Long
    @Update suspend fun updatePlanExercise(value: PlanExercise)
    @Delete suspend fun deletePlanExercise(value: PlanExercise)
    @Insert suspend fun insertSession(value: WorkoutSession): Long
    @Query("UPDATE sessions SET endedAt = :endedAt WHERE id = :id") suspend fun finishSession(id: Long, endedAt: Long)
    @Insert suspend fun insertSet(value: SetLog)
    @Update suspend fun updateSet(value: SetLog)
}

@Database(entities = [WorkoutPlan::class, Exercise::class, PlanExercise::class, WorkoutSession::class, SetLog::class], version = 1, exportSchema = false)
abstract class FitDatabase : RoomDatabase() { abstract fun dao(): FitDao }
