package com.example

import com.example.data.ActivityLevel
import com.example.data.DateUtils
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.domain.BiofeedbackReport
import com.example.domain.LockoutDomainEngine
import com.example.domain.RecoveryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun mifflinStJeorCalorieCalculator_computesAccurateBmrTdeeAndMacros() {
        val maleLeanBulk = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = 80f,
            heightCm = 180,
            ageYears = 25,
            isMale = true,
            activityLevel = ActivityLevel.MODERATE,
            goal = NutritionGoal.LEAN_BULK
        )
        // BMR = 10*80 + 6.25*180 - 5*25 + 5 = 800 + 1125 - 125 + 5 = 1805
        assertEquals(1805, maleLeanBulk.bmrKcal)
        assertEquals(2798, maleLeanBulk.tdeeKcal)
        assertEquals(3048, maleLeanBulk.targetCalories)
        assertEquals(160, maleLeanBulk.proteinGrams)
        assertTrue(maleLeanBulk.trainingDayCalories > maleLeanBulk.restDayCalories)
        assertEquals(24.7f, maleLeanBulk.bmi, 0.1f)

        val femaleCut = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = 62f,
            heightCm = 165,
            ageYears = 28,
            isMale = false,
            activityLevel = ActivityLevel.LIGHT,
            goal = NutritionGoal.FAT_LOSS
        )
        // BMR = 620 + 1031.25 - 140 - 161 = 1350.25 -> 1350
        assertEquals(1350, femaleCut.bmrKcal)
        assertTrue(femaleCut.targetCalories < femaleCut.tdeeKcal)
        assertTrue(femaleCut.targetCalories >= 1350)
    }

    @Test
    fun adherenceStats_calculatesTotalDaysStreakAnd7DayMask() {
        val logged = setOf(
            DateUtils.daysAgoIso(5),
            DateUtils.daysAgoIso(2),
            DateUtils.daysAgoIso(1),
            DateUtils.todayIso()
        )
        val stats = LockoutDomainEngine.computeAdherenceStats(logged)
        assertEquals(4, stats.totalDays)
        assertEquals(3, stats.currentStreakDays)
        assertEquals(7, stats.last7DaysMask.size)
        assertTrue(stats.last7DaysMask[6]) // today
        assertTrue(stats.last7DaysMask[5]) // yesterday
        assertFalse(stats.last7DaysMask[3]) // 3 days ago
    }

    @Test
    fun muscleRecoveryOverrides_serializeParseAndMergeCleanly() {
        val overrides = mapOf(
            MuscleGroup.CHEST to RecoveryStatus.FATIGUED,
            MuscleGroup.QUADS to RecoveryStatus.RECOVERING
        )
        val csv = LockoutDomainEngine.serializeMuscleOverrides(overrides)
        val parsed = LockoutDomainEngine.parseMuscleOverrides(csv)
        assertEquals(RecoveryStatus.FATIGUED, parsed[MuscleGroup.CHEST])
        assertEquals(RecoveryStatus.RECOVERING, parsed[MuscleGroup.QUADS])

        val merged = LockoutDomainEngine.mergeMuscleRecovery(
            autoMap = mapOf(MuscleGroup.CHEST to RecoveryStatus.RECOVERED),
            overrides = parsed
        )
        assertEquals(RecoveryStatus.FATIGUED, merged[MuscleGroup.CHEST])
        assertEquals(RecoveryStatus.RECOVERED, merged[MuscleGroup.BACK])
    }

    @Test
    fun strengthLabs_dots1rmAndBiofeedbackDeloadWorkCorrectly() {
        assertEquals(120f, LockoutDomainEngine.estimate1RM(100f, 6), 0.1f)
        val dots = LockoutDomainEngine.calculateDotsScore(totalKg = 500f, bodyWeightKg = 80f, isMale = true)
        assertTrue(dots in 320f..370f)

        val deload = LockoutDomainEngine.evaluateMesocycleFeedback(
            BiofeedbackReport(
                muscleGroup = MuscleGroup.SHOULDERS,
                pumpScore = 1,
                sorenessScore = 3,
                jointPainScore = 3,
                avgRir = 0
            )
        )
        assertTrue(deload.triggerDeload)
    }

    @Test
    fun pasteRoutineParser_extractsStructuredExercises() {
        val raw = """
            Incline Bench Press 4x8 75kg
            Pull-Ups 3x10
            Barbell Squat 5x5 120kg
        """.trimIndent()
        val parsed = LockoutDomainEngine.parsePastedRoutine(raw, "ppl_6day", "Push Day")
        assertEquals(3, parsed.size)
        assertEquals(4, parsed[0].targetSets)
        assertEquals(75f, parsed[0].weightKg, 0.1f)
        assertEquals(MuscleGroup.CHEST, parsed[0].primaryMuscle)
    }
}
