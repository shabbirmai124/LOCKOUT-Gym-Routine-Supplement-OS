package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LockoutRepository(private val dao: LockoutDao) {

    val allExercises: Flow<List<ExerciseEntity>> = dao.getAllExercises()
    val allSupplements: Flow<List<SupplementEntity>> = dao.getAllSupplements()
    val allSupplementLogs: Flow<List<SupplementLogEntity>> = dao.getAllSupplementLogs()
    val allVaultItems: Flow<List<VaultItemEntity>> = dao.getAllVaultItems()
    val allSkillMilestones: Flow<List<SkillMilestoneEntity>> = dao.getAllSkillMilestones()

    fun todaySupplementLogs(todayIso: String = DateUtils.todayIso()): Flow<List<SupplementLogEntity>> =
        dao.getLogsForDate(todayIso)

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

    suspend fun toggleSupplementForToday(supplement: SupplementEntity, currentlyTaken: Boolean) {
        val today = DateUtils.todayIso()
        if (currentlyTaken) {
            dao.unmarkSupplement(supplement.id, today)
            dao.upsertSupplement(supplement.copy(servingsLeft = supplement.servingsLeft + 1))
        } else {
            dao.markSupplementTaken(SupplementLogEntity(supplementId = supplement.id, dateIso = today))
            dao.upsertSupplement(
                supplement.copy(servingsLeft = (supplement.servingsLeft - 1).coerceAtLeast(0))
            )
        }
    }

    suspend fun restockSupplement(supplement: SupplementEntity, addServings: Int = 30) {
        dao.upsertSupplement(supplement.copy(servingsLeft = supplement.servingsLeft + addServings))
    }

    suspend fun upsertNutrition(nutrition: DailyNutritionEntity) = dao.upsertNutrition(nutrition)

    suspend fun setGearBagStatus(vaultItemId: Int, owned: Boolean) =
        dao.setGearBagStatus(vaultItemId, owned)

    suspend fun toggleSkillMilestone(id: Int, unlocked: Boolean) =
        dao.setSkillUnlocked(id, unlocked)

    suspend fun upsertAppPrefs(prefs: AppPrefsEntity) = dao.upsertAppPrefs(prefs)
}
