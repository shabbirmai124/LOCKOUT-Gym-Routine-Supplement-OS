package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LockoutRepository(private val dao: LockoutDao) {

    val allExercises: Flow<List<ExerciseEntity>> = dao.getAllExercises()
    val allSupplements: Flow<List<SupplementEntity>> = dao.getAllSupplements()
    val allSupplementLogs: Flow<List<SupplementLogEntity>> = dao.getAllSupplementLogs()
    val allHabits: Flow<List<DailyHabitEntity>> = dao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = dao.getAllHabitLogs()
    val allVaultItems: Flow<List<VaultItemEntity>> = dao.getAllVaultItems()
    val allSkillMilestones: Flow<List<SkillMilestoneEntity>> = dao.getAllSkillMilestones()

    fun todaySupplementLogs(todayIso: String = DateUtils.todayIso()): Flow<List<SupplementLogEntity>> =
        dao.getLogsForDate(todayIso)

    fun todayHabitLogs(todayIso: String = DateUtils.todayIso()): Flow<List<HabitLogEntity>> =
        dao.getHabitLogsForDate(todayIso)

    fun todayNutrition(todayIso: String = DateUtils.todayIso()): Flow<DailyNutritionEntity> =
        dao.getNutritionForDate(todayIso).map { it ?: DailyNutritionEntity(dateIso = todayIso) }

    val appPrefs: Flow<AppPrefsEntity> =
        dao.getAppPrefs().map { it ?: AppPrefsEntity() }

    suspend fun ensureSeeded() {
        if (dao.countExercises() == 0) {
            dao.insertAllExercises(LockoutSeedData.seedExercises())
        }
        if (dao.countSupplements() == 0) {
            dao.insertAllSupplements(LockoutSeedData.seedSupplements())
        }
        if (dao.countHabits() == 0) {
            dao.insertAllHabits(LockoutSeedData.seedHealthyHabits())
        }
        if (dao.countVaultItems() == 0) {
            dao.insertAllVaultItems(LockoutSeedData.seedVaultItems())
        }
        if (dao.countSkillMilestones() == 0) {
            dao.insertAllSkillMilestones(LockoutSeedData.seedSkillMilestones())
        }
        val today = DateUtils.todayIso()
        if (dao.getNutritionForDateOnce(today) == null) {
            dao.upsertNutrition(DailyNutritionEntity(dateIso = today))
        }
        if (dao.getAppPrefsOnce() == null) {
            dao.upsertAppPrefs(AppPrefsEntity())
        }
    }

    suspend fun upsertExercise(exercise: ExerciseEntity) = dao.upsertExercise(exercise)

    suspend fun deleteExercise(id: Int) = dao.deleteExerciseById(id)

    suspend fun upsertSupplement(supplement: SupplementEntity) = dao.upsertSupplement(supplement)

    suspend fun deleteSupplement(id: Int) = dao.deleteSupplementById(id)

    suspend fun replaceWithGoalStack(presetId: String) {
        dao.clearSupplements()
        dao.insertAllSupplements(LockoutSeedData.goalStackPreset(presetId))
    }

    suspend fun toggleSupplementForToday(
        supplement: SupplementEntity,
        currentlyTaken: Boolean,
        todayIso: String = DateUtils.todayIso()
    ) {
        if (currentlyTaken) {
            dao.unmarkSupplement(supplement.id, todayIso)
            dao.upsertSupplement(supplement.copy(servingsLeft = supplement.servingsLeft + 1))
        } else {
            dao.markSupplementTaken(SupplementLogEntity(supplementId = supplement.id, dateIso = todayIso))
            dao.upsertSupplement(
                supplement.copy(servingsLeft = (supplement.servingsLeft - 1).coerceAtLeast(0))
            )
        }
    }

    suspend fun updateSupplementReminder(
        supplement: SupplementEntity,
        enabled: Boolean,
        hour: Int,
        minute: Int
    ): SupplementEntity {
        val updated = supplement.copy(
            reminderEnabled = enabled,
            reminderHour = hour.coerceIn(0, 23),
            reminderMinute = minute.coerceIn(0, 59)
        )
        dao.upsertSupplement(updated)
        return updated
    }

    suspend fun restockSupplement(supplement: SupplementEntity, addServings: Int = 30) {
        dao.upsertSupplement(supplement.copy(servingsLeft = supplement.servingsLeft + addServings))
    }

    suspend fun toggleHabitForToday(
        habitId: Int,
        currentlyDone: Boolean,
        todayIso: String = DateUtils.todayIso()
    ) {
        if (currentlyDone) {
            dao.unmarkHabit(habitId, todayIso)
        } else {
            dao.markHabitCompleted(HabitLogEntity(habitId = habitId, dateIso = todayIso))
        }
    }

    suspend fun upsertHabit(habit: DailyHabitEntity) = dao.upsertHabit(habit)

    suspend fun deleteHabit(habitId: Int) = dao.deleteHabitById(habitId)

    suspend fun upsertNutrition(nutrition: DailyNutritionEntity) = dao.upsertNutrition(nutrition)

    suspend fun setGearBagStatus(vaultItemId: Int, owned: Boolean) =
        dao.setGearBagStatus(vaultItemId, owned)

    suspend fun toggleSkillMilestone(id: Int, unlocked: Boolean) =
        dao.setSkillUnlocked(id, unlocked)

    suspend fun upsertAppPrefs(prefs: AppPrefsEntity) = dao.upsertAppPrefs(prefs)
}
