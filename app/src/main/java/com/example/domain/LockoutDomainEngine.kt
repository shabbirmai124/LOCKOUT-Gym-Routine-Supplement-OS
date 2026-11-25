package com.example.domain

import com.example.data.DailyNutritionEntity
import com.example.data.ExerciseEntity
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.data.SupplementEntity
import kotlin.math.roundToInt

data class WarmUpSet(
    val stepNumber: Int,
    val percentage: Int,
    val weightKg: Float,
    val reps: Int,
    val restSeconds: Int,
    val purpose: String
)

data class PlateSpec(
    val weightKg: Float,
    val label: String,
    val colorHex: Long
)

data class BarbellLoadPlan(
    val targetWeightKg: Float,
    val barWeightKg: Float,
    val achievedWeightKg: Float,
    val platesPerSide: List<PlateSpec>,
    val isBarOnly: Boolean
)

data class SwapAlternative(
    val name: String,
    val category: String,
    val convertedWeightKg: Float,
    val note: String
)

enum class AlertSeverity { INFO, WARNING, CRITICAL }

data class ClinicalGuardrail(
    val title: String,
    val detail: String,
    val severity: AlertSeverity
)

enum class RecoveryStatus(val label: String) {
    RECOVERED("Recovered"),
    RECOVERING("Recovering"),
    FATIGUED("Fatigued")
}

data class OverloadDecision(
    val updatedExercise: ExerciseEntity,
    val summaryMessage: String,
    val isUpgrade: Boolean
)

object LockoutDomainEngine {

    private val standardPlates = listOf(
        PlateSpec(25f, "25", 0xFFEF4444),     // Red
        PlateSpec(20f, "20", 0xFF3B82F6),     // Blue
        PlateSpec(15f, "15", 0xFFFACC15),     // Yellow
        PlateSpec(10f, "10", 0xFF22C55E),     // Green
        PlateSpec(5f, "5", 0xFFF8FAFC),       // White
        PlateSpec(2.5f, "2.5", 0xFF94A3B8),   // Steel
        PlateSpec(1.25f, "1.25", 0xFFC6FF00), // Volt Micro
        PlateSpec(0.5f, "0.5", 0xFF38BDF8)    // Cyan Micro
    )

    fun estimateOneRepMax(weightKg: Float, targetRepsOrSec: String): Float {
        if (weightKg <= 0f) return 0f
        val reps = Regex("\\d+").find(targetRepsOrSec)?.value?.toIntOrNull()?.coerceIn(1, 20) ?: 8
        if (reps == 1) return weightKg
        val epley = weightKg * (1f + reps / 30f)
        return (epley * 2f).roundToInt() / 2f
    }

    fun generateWarmUpRamp(workingWeightKg: Float, barWeightKg: Float = 20f): List<WarmUpSet> {
        val base = workingWeightKg.coerceAtLeast(barWeightKg)
        fun round25(v: Float): Float = ((v / 2.5f).roundToInt() * 2.5f).coerceAtLeast(barWeightKg)
        return listOf(
            WarmUpSet(1, 50, round25(base * 0.50f), 8, 45, "Joint Lubrication"),
            WarmUpSet(2, 70, round25(base * 0.70f), 5, 60, "Neural Priming"),
            WarmUpSet(3, 85, round25(base * 0.85f), 2, 90, "Heavy Potentiation")
        )
    }

    fun calculateBarbellPlates(targetWeightKg: Float, barWeightKg: Float = 20f): BarbellLoadPlan {
        if (targetWeightKg <= barWeightKg) {
            return BarbellLoadPlan(
                targetWeightKg = targetWeightKg,
                barWeightKg = barWeightKg,
                achievedWeightKg = barWeightKg,
                platesPerSide = emptyList(),
                isBarOnly = true
            )
        }
        var remainingPerSide = (targetWeightKg - barWeightKg) / 2f
        val chosen = mutableListOf<PlateSpec>()
        for (plate in standardPlates) {
            while (remainingPerSide + 0.01f >= plate.weightKg) {
                chosen.add(plate)
                remainingPerSide -= plate.weightKg
            }
        }
        val sideSum = chosen.sumOf { it.weightKg.toDouble() }.toFloat()
        val achieved = barWeightKg + (sideSum * 2f)
        return BarbellLoadPlan(
            targetWeightKg = targetWeightKg,
            barWeightKg = barWeightKg,
            achievedWeightKg = achieved,
            platesPerSide = chosen,
            isBarOnly = chosen.isEmpty()
        )
    }

    fun applyProgressiveOverload(exercise: ExerciseEntity, loggedRpe: Float): OverloadDecision {
        return when {
            loggedRpe < 8.0f -> {
                if (exercise.isIsometric) {
                    val nextSec = (exercise.holdTargetSec + 5).coerceAtMost(180)
                    val updated = exercise.copy(
                        holdTargetSec = nextSec,
                        targetRepsOrSec = "${nextSec}s hold",
                        lastLoggedRpe = loggedRpe
                    )
                    OverloadDecision(
                        updatedExercise = updated,
                        summaryMessage = "RPE $loggedRpe < 8.0 — Next target auto-upgraded to ${nextSec}s hold (+5s).",
                        isUpgrade = true
                    )
                } else {
                    val increment = if (exercise.weightKg >= 60f) 2.5f else 1.25f
                    val nextWeight = exercise.weightKg + increment
                    val updated = exercise.copy(
                        previousWeightKg = exercise.weightKg,
                        weightKg = nextWeight,
                        lastLoggedRpe = loggedRpe
                    )
                    OverloadDecision(
                        updatedExercise = updated,
                        summaryMessage = "RPE $loggedRpe < 8.0 — Next session auto-upgraded to ${formatKg(nextWeight)} (+${formatKg(increment)}).",
                        isUpgrade = true
                    )
                }
            }
            loggedRpe >= 9.5f -> {
                val updated = exercise.copy(lastLoggedRpe = loggedRpe)
                OverloadDecision(
                    updatedExercise = updated,
                    summaryMessage = "High CNS demand (RPE $loggedRpe) — Hold current load next session and monitor recovery.",
                    isUpgrade = false
                )
            }
            else -> {
                val updated = exercise.copy(lastLoggedRpe = loggedRpe)
                OverloadDecision(
                    updatedExercise = updated,
                    summaryMessage = "RPE $loggedRpe on target — Lock in exact form and push +1 rep next week.",
                    isUpgrade = false
                )
            }
        }
    }

    fun getSmartSwapAlternatives(exercise: ExerciseEntity): List<SwapAlternative> {
        fun roundLoad(factor: Float): Float {
            if (exercise.weightKg <= 0f) return 0f
            return ((exercise.weightKg * factor) / 2.5f).roundToInt().coerceAtLeast(1) * 2.5f
        }

        val lowerName = exercise.name.lowercase()
        return when {
            "incline" in lowerName || "bench" in lowerName -> listOf(
                SwapAlternative("Incline Smith Machine Press", "Chest • Guided Rail", roundLoad(0.95f), "Fixed bar path • safe to push near failure"),
                SwapAlternative("Converging Chest Press Machine", "Chest • Plate-Loaded", roundLoad(0.90f), "Natural arc • zero setup wait"),
                SwapAlternative("Incline Dumbbell Press", "Chest • Unilateral", roundLoad(0.42f), "Per dumbbell • shoulder-friendly neutral grip"),
                SwapAlternative("Low-to-High Cable Fly", "Upper Chest • Cable", roundLoad(0.30f), "Constant tension • upper clavicular fibers")
            )
            "squat" in lowerName -> listOf(
                SwapAlternative("Hack Squat", "Quads • Plate-Loaded", roundLoad(0.85f), "Deep quad bias • zero spinal shear"),
                SwapAlternative("45-Degree Leg Press", "Quads & Glutes", roundLoad(1.55f), "High output • lower back supported"),
                SwapAlternative("Smith Machine Squat", "Quads • Guided", roundLoad(0.90f), "Step feet 6–8\" forward for quad sweep"),
                SwapAlternative("Bulgarian Split Squat", "Quads • Unilateral", roundLoad(0.22f), "Per dumbbell • fixes side imbalances")
            )
            "deadlift" in lowerName || "rdl" in lowerName -> listOf(
                SwapAlternative("Dumbbell Romanian Deadlift", "Hamstrings • Hinge", roundLoad(0.35f), "Per hand • zero barbell rack required"),
                SwapAlternative("45° Back Extension (Weighted)", "Posterior Chain", roundLoad(0.25f), "Lower-back friendly glute & hamstring focus"),
                SwapAlternative("Seated Leg Curl", "Hamstrings • Machine", roundLoad(0.55f), "Maximal stretched-position hamstring tension")
            )
            "pull-up" in lowerName || "chin-up" in lowerName || "pulldown" in lowerName -> listOf(
                SwapAlternative("Neutral-Grip Lat Pulldown", "Lats • Cable", roundLoad(1.0f).coerceAtLeast(55f), "Joint-friendly shoulder rotation"),
                SwapAlternative("Single-Arm Cable Lat Pulldown", "Lats • Unilateral", roundLoad(0.55f).coerceAtLeast(30f), "Peak iliac lat contraction"),
                SwapAlternative("Plate-Loaded High Row", "Back • Machine", roundLoad(1.0f).coerceAtLeast(60f), "Zero grip bottleneck")
            )
            "row" in lowerName -> listOf(
                SwapAlternative("Chest-Supported Machine Row", "Upper Back", roundLoad(0.95f), "Eliminates lower-back fatigue"),
                SwapAlternative("Seated Cable Row (Mag Grip)", "Mid-Back", roundLoad(0.90f), "Smooth resistance curve"),
                SwapAlternative("Single-Arm Dumbbell Row", "Lats & Traps", roundLoad(0.45f), "Bench-braced stability")
            )
            else -> when (exercise.primaryMuscle) {
                MuscleGroup.CHEST -> listOf(
                    SwapAlternative("Pec Deck Machine", "Chest • Isolation", roundLoad(0.85f), "Constant peak tension"),
                    SwapAlternative("Cable Chest Press", "Chest • Cable", roundLoad(0.65f), "Smooth scapular motion")
                )
                MuscleGroup.SHOULDERS -> listOf(
                    SwapAlternative("Machine Shoulder Press", "Delts • Machine", roundLoad(0.95f), "Safe lockout stability"),
                    SwapAlternative("Cable Lateral Raises", "Side Delts", roundLoad(0.5f).coerceAtLeast(10f), "Zero bottom dead-spot")
                )
                MuscleGroup.BACK -> listOf(
                    SwapAlternative("Seated Cable Row", "Back • Cable", roundLoad(0.9f).coerceAtLeast(50f), "Full stretch and squeeze"),
                    SwapAlternative("Straight-Arm Cable Pulldown", "Lats • Isolation", roundLoad(0.5f).coerceAtLeast(25f), "Pure lat mind-muscle link")
                )
                MuscleGroup.ARMS -> listOf(
                    SwapAlternative("EZ-Bar Cable Curl", "Biceps • Cable", roundLoad(0.9f).coerceAtLeast(25f), "Wrist-friendly camber"),
                    SwapAlternative("Single-Arm Cable Pushdown", "Triceps • Cable", roundLoad(0.5f).coerceAtLeast(15f), "Fixes side dominance")
                )
                MuscleGroup.CORE -> listOf(
                    SwapAlternative("Weighted Cable Crunch", "Core • Flexion", 35f, "Easy progressive overload"),
                    SwapAlternative("Ab Wheel Rollout", "Core • Anti-Extension", 0f, "Elite anterior stability")
                )
                else -> listOf(
                    SwapAlternative("45-Degree Leg Press", "Legs • Machine", roundLoad(1.2f).coerceAtLeast(120f), "Stable heavy leg volume"),
                    SwapAlternative("Walking Lunges", "Legs • Unilateral", roundLoad(0.3f).coerceAtLeast(16f), "Athletic knee & hip control")
                )
            }
        }
    }

    fun evaluateSupplementGuardrails(
        supplements: List<SupplementEntity>,
        plannedWorkoutHour: Int
    ): List<ClinicalGuardrail> {
        val alerts = mutableListOf<ClinicalGuardrail>()
        val keys = supplements.map { it.interactionKey.uppercase() }.toSet()
        val namesLower = supplements.map { it.name.lowercase() }

        val hasCaffeine = "CAFFEINE" in keys || namesLower.any { "caffeine" in it || "pre-workout" in it }
        if (hasCaffeine && plannedWorkoutHour >= 15) {
            val activeAtMidnight = 100
            val hourDisplay = if (plannedWorkoutHour > 12) "${plannedWorkoutHour - 12}:00 PM" else "${plannedWorkoutHour}:00"
            alerts.add(
                ClinicalGuardrail(
                    title = "Caffeine Sleep Cutoff Guard",
                    detail = "Training at $hourDisplay with caffeine leaves ~${activeAtMidnight} mg active past midnight. Switch to stim-free L-Citrulline to protect REM sleep.",
                    severity = AlertSeverity.WARNING
                )
            )
        }

        val hasZinc = "ZINC" in keys || namesLower.any { "zinc" in it }
        val hasCalciumOrIron = "CALCIUM" in keys || "IRON" in keys || namesLower.any { "calcium" in it || "iron" in it }
        if (hasZinc && hasCalciumOrIron) {
            alerts.add(
                ClinicalGuardrail(
                    title = "Mineral Absorption Conflict",
                    detail = "Avoid taking Zinc with Calcium or Iron in the same timing window—separate by at least 2 hours.",
                    severity = AlertSeverity.CRITICAL
                )
            )
        }

        val hasVitD = "VIT_D3" in keys || namesLower.any { "vitamin d" in it || "d3" in it }
        if (hasVitD) {
            alerts.add(
                ClinicalGuardrail(
                    title = "Fat-Soluble Synergy (D3 + Omega-3)",
                    detail = "Take Vitamin D3 + K2 alongside breakfast fats or Omega-3 Fish Oil for maximum absorption.",
                    severity = AlertSeverity.INFO
                )
            )
        }

        val hasCreatine = "CREATINE" in keys || namesLower.any { "creatine" in it }
        if (hasCreatine) {
            alerts.add(
                ClinicalGuardrail(
                    title = "Creatine Hydration Boost Active",
                    detail = "Daily 5g Creatine saturation detected — Hydration target automatically bumped by +500 ml.",
                    severity = AlertSeverity.INFO
                )
            )
        }

        val lowStock = supplements.filter { it.servingsLeft <= 7 }
        if (lowStock.isNotEmpty()) {
            val names = lowStock.joinToString(", ") { "${it.name} (${it.servingsLeft} left)" }
            alerts.add(
                ClinicalGuardrail(
                    title = "7-Day Low-Stock Restock Alert",
                    detail = "Running low: $names. Tap Restock on any card when your fresh tub arrives.",
                    severity = AlertSeverity.WARNING
                )
            )
        }

        return alerts
    }

    fun calculateMuscleRecoveryMap(exercises: List<ExerciseEntity>): Map<MuscleGroup, RecoveryStatus> {
        val now = System.currentTimeMillis()
        val setsByMuscle = mutableMapOf<MuscleGroup, Int>()
        val recentTrainByMuscle = mutableMapOf<MuscleGroup, Long>()

        for (ex in exercises) {
            if (ex.completedSets > 0) {
                setsByMuscle[ex.primaryMuscle] = (setsByMuscle[ex.primaryMuscle] ?: 0) + ex.completedSets
                val prev = recentTrainByMuscle[ex.primaryMuscle] ?: 0L
                if (ex.lastTrainedMillis > prev) {
                    recentTrainByMuscle[ex.primaryMuscle] = ex.lastTrainedMillis
                }
            }
        }

        return MuscleGroup.entries.associateWith { muscle ->
            val sets = setsByMuscle[muscle] ?: 0
            val lastMs = recentTrainByMuscle[muscle] ?: 0L
            val hoursSince = if (lastMs > 0L) (now - lastMs) / 3_600_000f else 999f
            when {
                sets >= 6 && hoursSince < 36f -> RecoveryStatus.FATIGUED
                sets >= 2 && hoursSince < 48f -> RecoveryStatus.RECOVERING
                else -> RecoveryStatus.RECOVERED
            }
        }
    }

    fun calculateReadinessScore(
        nutrition: DailyNutritionEntity,
        effectiveWaterTargetMl: Int,
        supplementsTaken: Int,
        totalSupplements: Int,
        recoveryMap: Map<MuscleGroup, RecoveryStatus>
    ): Int {
        val hydrationRatio = (nutrition.waterMl.toFloat() / effectiveWaterTargetMl.coerceAtLeast(1)).coerceIn(0f, 1f)
        val proteinRatio = (nutrition.proteinGrams.toFloat() / nutrition.proteinTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
        val suppRatio = if (totalSupplements > 0) (supplementsTaken.toFloat() / totalSupplements).coerceIn(0f, 1f) else 1f
        val recoveredFraction = recoveryMap.values.count { it == RecoveryStatus.RECOVERED }.toFloat() /
            recoveryMap.size.coerceAtLeast(1)

        val score = 35f + (hydrationRatio * 20f) + (proteinRatio * 20f) + (suppRatio * 10f) + (recoveredFraction * 15f)
        return score.roundToInt().coerceIn(40, 100)
    }

    fun recalculateMacroTargets(
        current: DailyNutritionEntity,
        goal: NutritionGoal,
        isTrainingDay: Boolean
    ): DailyNutritionEntity {
        val baseCalories = (current.bodyWeightKg * 31f).roundToInt() + goal.calorieDelta
        val trainingBonus = if (isTrainingDay) 260 else -140
        val caloriesTarget = (baseCalories + trainingBonus).coerceAtLeast(1600)
        val proteinTarget = (current.bodyWeightKg * goal.proteinPerKg).roundToInt().coerceAtLeast(120)
        val fatsTarget = (current.bodyWeightKg * 0.85f).roundToInt().coerceAtLeast(45)
        val remainingKcal = (caloriesTarget - (proteinTarget * 4) - (fatsTarget * 9)).coerceAtLeast(200)
        val carbsTarget = remainingKcal / 4
        return current.copy(
            goal = goal,
            isTrainingDay = isTrainingDay,
            caloriesTarget = caloriesTarget,
            proteinTarget = proteinTarget,
            carbsTarget = carbsTarget,
            fatsTarget = fatsTarget
        )
    }

    fun formatKg(kg: Float): String {
        return if (kg % 1f == 0f) "${kg.toInt()} kg" else "${"%.1f".format(kg)} kg"
    }
}
