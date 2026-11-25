package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LockoutDao {

    // Exercises
    @Query("SELECT * FROM exercises ORDER BY id ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Int)

    // Supplements & Date-Stamped Logs (automatic midnight reset + streak tracking)
    @Query("SELECT * FROM supplements ORDER BY id ASC")
    fun getAllSupplements(): Flow<List<SupplementEntity>>

    @Query("SELECT COUNT(*) FROM supplements")
    suspend fun countSupplements(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplement(supplement: SupplementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSupplements(supplements: List<SupplementEntity>)

    @Query("DELETE FROM supplements")
    suspend fun clearSupplements()

    @Query("DELETE FROM supplements WHERE id = :id")
    suspend fun deleteSupplementById(id: Int)

    @Query("SELECT * FROM supplement_logs WHERE dateIso = :today")
    fun getLogsForDate(today: String): Flow<List<SupplementLogEntity>>

    @Query("SELECT * FROM supplement_logs ORDER BY dateIso DESC")
    fun getAllSupplementLogs(): Flow<List<SupplementLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markSupplementTaken(log: SupplementLogEntity)

    @Query("DELETE FROM supplement_logs WHERE supplementId = :id AND dateIso = :today")
    suspend fun unmarkSupplement(id: Int, today: String)

    // Daily Nutrition (keyed by today's ISO date)
    @Query("SELECT * FROM daily_nutrition WHERE dateIso = :today")
    fun getNutritionForDate(today: String): Flow<DailyNutritionEntity?>

    @Query("SELECT * FROM daily_nutrition WHERE dateIso = :today")
    suspend fun getNutritionForDateOnce(today: String): DailyNutritionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNutrition(nutrition: DailyNutritionEntity)

    // Offline Gym Vault
    @Query("SELECT * FROM vault_items ORDER BY id ASC")
    fun getAllVaultItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun countVaultItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVaultItems(items: List<VaultItemEntity>)

    @Query("UPDATE vault_items SET isInMyGearBag = :owned WHERE id = :id")
    suspend fun setGearBagStatus(id: Int, owned: Boolean)

    // Gymnastic Skill Tree
    @Query("SELECT * FROM skill_milestones ORDER BY treeName ASC, stepOrder ASC")
    fun getAllSkillMilestones(): Flow<List<SkillMilestoneEntity>>

    @Query("SELECT COUNT(*) FROM skill_milestones")
    suspend fun countSkillMilestones(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSkillMilestones(milestones: List<SkillMilestoneEntity>)

    @Query("UPDATE skill_milestones SET isUnlocked = :unlocked WHERE id = :id")
    suspend fun setSkillUnlocked(id: Int, unlocked: Boolean)

    // App Preferences (Program, Day, Theme, Workout Hour, Joint Injury Flags)
    @Query("SELECT * FROM app_prefs WHERE id = 1")
    fun getAppPrefs(): Flow<AppPrefsEntity?>

    @Query("SELECT * FROM app_prefs WHERE id = 1")
    suspend fun getAppPrefsOnce(): AppPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppPrefs(prefs: AppPrefsEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
}
