package com.example.domain

import com.example.data.ActivityLevel
import com.example.data.DailyNutritionEntity
import com.example.data.DateUtils
import com.example.data.ExerciseEntity
import com.example.data.GymProfile
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.data.SupplementEntity
import kotlin.math.pow
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

data class BiofeedbackReport(
    val muscleGroup: MuscleGroup,
    val pumpScore: Int,       // 1 = Low, 2 = Solid, 3 = Skin-splitting
    val sorenessScore: Int,   // 1 = Fresh, 2 = Moderate, 3 = Very sore
    val jointPainScore: Int,  // 0 = None, 1 = Mild, 2 = Moderate, 3 = Sharp
    val avgRir: Int           // 0..4 Reps in Reserve
)

data class VolumePrescription(
    val setDelta: Int,
    val triggerDeload: Boolean,
    val coachingNote: String
)

enum class StrengthTier(val label: String, val badge: String) {
    BEGINNER("Beginner", "TIER I"),
    NOVICE("Novice", "TIER II"),
    INTERMEDIATE("Intermediate", "TIER III"),
    ADVANCED("Advanced", "TIER IV"),
    ELITE("Elite", "TIER V")
}

data class MeetAttempts(
    val liftName: String,
    val openerKg: Float,     // 90%
    val secondKg: Float,     // 96%
    val thirdKg: Float       // 102%
)

data class MobilityDrill(
    val name: String,
    val prescription: String,
    val cue: String,
    val durationSeconds: Int
)

data class AdherenceStats(
    val totalDays: Int,
    val currentStreakDays: Int,
    val last7DaysMask: List<Boolean>
)

data class CaloriePlanResult(
    val bmrKcal: Int,
    val tdeeKcal: Int,
    val targetCalories: Int,
    val trainingDayCalories: Int,
    val restDayCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatsGrams: Int,
    val waterTargetMl: Int,
    val bmi: Float,
    val bmiCategory: String
)

object LockoutDomainEngine {

    private const val KG_TO_LBS = 2.20462f

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

    fun formatWeight(weightKg: Float, useLbs: Boolean): String {
        if (weightKg <= 0f) return "BW"
        return if (useLbs) {
            val lbs = (weightKg * KG_TO_LBS / 2.5f).roundToInt() * 2.5f
            if (lbs % 1f == 0f) "${lbs.toInt()} lbs" else "${"%.1f".format(lbs)} lbs"
        } else {
            formatKg(weightKg)
        }
    }

    fun formatKg(kg: Float): String {
        return if (kg % 1f == 0f) "${kg.toInt()} kg" else "${"%.1f".format(kg)} kg"
    }

    fun estimateOneRepMax(weightKg: Float, targetRepsOrSec: String): Float {
        if (weightKg <= 0f) return 0f
        val reps = Regex("\\d+").find(targetRepsOrSec)?.value?.toIntOrNull()?.coerceIn(1, 20) ?: 8
        return estimate1RM(weightKg, reps)
    }

    fun estimate1RM(weightKg: Float, reps: Int): Float {
        if (weightKg <= 0f) return 0f
        if (reps <= 1) return weightKg
        val epley = weightKg * (1f + reps / 30f)
        return (epley * 2f).roundToInt() / 2f
    }

    /** Official DOTS Strength Score */
    fun calculateDotsScore(
        totalKg: Float,
        bodyWeightKg: Float,
        isMale: Boolean = true
    ): Float {
        if (totalKg <= 0f) return 0f
        val bw = bodyWeightKg.toDouble().coerceIn(40.0, 210.0)
        val (a, b, c, d, e) = if (isMale) {
            listOf(-307.75076, 24.0900756, -0.1918759221, 0.0007391293, -0.000001093)
        } else {
            listOf(-57.96288, 13.6175032, -0.1126655495, 0.0005158568, -0.0000010706)
        }
        val denom = a + b * bw + c * bw.pow(2) + d * bw.pow(3) + e * bw.pow(4)
        val coeff = 500.0 / denom
        return ((totalKg * coeff) * 10.0).roundToInt() / 10f
    }

    /** Official Wilks Strength Score */
    fun calculateWilksScore(
        totalKg: Float,
        bodyWeightKg: Float,
        isMale: Boolean = true
    ): Float {
        if (totalKg <= 0f) return 0f
        val bw = bodyWeightKg.toDouble().coerceIn(40.0, 200.0)
        val coeffs = if (isMale) {
            doubleArrayOf(-216.0475144, 16.2606339, -0.002388645, -0.00113732, 0.00000701863, -0.00000001291)
        } else {
            doubleArrayOf(594.31747775582, -27.23842536447, 0.82112226871, -0.00930733913, 0.00004731582, -0.00000009054)
        }
        val denom = coeffs[0] + coeffs[1] * bw + coeffs[2] * bw.pow(2) +
            coeffs[3] * bw.pow(3) + coeffs[4] * bw.pow(4) + coeffs[5] * bw.pow(5)
        val coeff = 500.0 / denom
        return ((totalKg * coeff) * 10.0).roundToInt() / 10f
    }

    /** RPG Strength Tier Classifier based on Bodyweight Multipliers */
    fun classifyStrengthTier(
        liftType: String,
        oneRepMaxKg: Float,
        bodyWeightKg: Float,
        isMale: Boolean = true
    ): StrengthTier {
        val ratio = oneRepMaxKg / bodyWeightKg.coerceAtLeast(40f)
        val genderScale = if (isMale) 1f else 0.72f
        val thresholds = when (liftType.lowercase()) {
            "squat" -> listOf(1.0f, 1.35f, 1.75f, 2.25f)
            "bench" -> listOf(0.75f, 1.0f, 1.35f, 1.75f)
            "deadlift" -> listOf(1.25f, 1.65f, 2.15f, 2.65f)
            else -> listOf(0.8f, 1.1f, 1.4f, 1.8f)
        }.map { it * genderScale }

        return when {
            ratio >= thresholds[3] -> StrengthTier.ELITE
            ratio >= thresholds[2] -> StrengthTier.ADVANCED
            ratio >= thresholds[1] -> StrengthTier.INTERMEDIATE
            ratio >= thresholds[0] -> StrengthTier.NOVICE
            else -> StrengthTier.BEGINNER
        }
    }

    /** Competition Meet Attempt Selector: 90% Opener / 96% Second / 102% Third */
    fun calculateMeetAttempts(liftName: String, estimated1RmKg: Float): MeetAttempts {
        fun r25(v: Float) = ((v / 2.5f).roundToInt() * 2.5f).coerceAtLeast(20f)
        return MeetAttempts(
            liftName = liftName,
            openerKg = r25(estimated1RmKg * 0.90f),
            secondKg = r25(estimated1RmKg * 0.96f),
            thirdKg = r25(estimated1RmKg * 1.02f)
        )
    }

    /** RP-Style Mesocycle Biofeedback Volume Auto-Regulation (MEV -> MRV) */
    fun evaluateMesocycleFeedback(report: BiofeedbackReport): VolumePrescription {
        val muscle = report.muscleGroup.label
        return when {
            report.jointPainScore >= 2 -> VolumePrescription(
                setDelta = -2,
                triggerDeload = true,
                coachingNote = "Joint irritation detected in $muscle (Pain ${report.jointPainScore}/3). Swapping to joint-friendly variations & applying a 50% volume deload."
            )
            report.sorenessScore == 3 && report.avgRir == 0 -> VolumePrescription(
                setDelta = -1,
                triggerDeload = false,
                coachingNote = "Recovery lagging for $muscle (Soreness 3/3, RIR 0). Trimmed -1 working set next session to stay inside productive MRV."
            )
            report.sorenessScore <= 1 && report.pumpScore <= 2 && report.avgRir >= 2 -> VolumePrescription(
                setDelta = +1,
                triggerDeload = false,
                coachingNote = "High recovery & room to grow in $muscle. Added +1 working set to your next session."
            )
            else -> VolumePrescription(
                setDelta = 0,
                triggerDeload = false,
                coachingNote = "Sweet spot achieved for $muscle. Keep working sets steady and beat reps or load next week."
            )
        }
    }

    /** Pre-Lift Dynamic Mobility & Joint Prehab Flows matched to active training day */
    fun getPreLiftMobilityFlow(dayTitle: String): List<MobilityDrill> {
        val key = dayTitle.lowercase()
        return when {
            "leg" in key || "lower" in key -> listOf(
                MobilityDrill("90/90 Hip Internal/External Switches", "10 reps / side", "Keep chest tall; drive lead knee softly into floor.", 60),
                MobilityDrill("Weighted Ankle Dorsiflexion Rocks", "12 rocks / side", "Track knee far past toes while keeping heel glued down.", 60),
                MobilityDrill("World's Greatest Stretch + T-Spine Twist", "6 reps / side", "Drop back knee, open chest cleanly to ceiling.", 90),
                MobilityDrill("Glute Bridge + Hamstring Walkouts", "2 × 10 reps", "Squeeze glutes hard before initiating squats or RDLs.", 60)
            )
            "skill" in key || "ring" in key || "conditioning" in key -> listOf(
                MobilityDrill("Quadruped Wrist Circles & First-Knuckle Raises", "2 × 12 reps", "Warm flexor tendons thoroughly before handstands & parallettes.", 75),
                MobilityDrill("Scapular Push-Ups + Protraction Hold", "2 × 12 reps", "Lock elbows straight; push floor away for planche prep.", 60),
                MobilityDrill("Shoulder Dislocates (PVC / Band)", "15 slow reps", "Keep ribs tucked down; zero lumbar arch.", 60),
                MobilityDrill("Hollow Body Rock Activation", "30s hold", "Glue lower back flat against floor.", 45)
            )
            "pull" in key -> listOf(
                MobilityDrill("Active Dead Hangs -> Scapular Pull-Ups", "2 × 10 reps", "Decompress spine, then depress lats with straight arms.", 60),
                MobilityDrill("Thoracic Foam Roller Extensions", "10 slow extensions", "Open upper back before heavy rows and pulldowns.", 60),
                MobilityDrill("Band Face Pulls with External Rotation", "2 × 15 reps", "Prime rear delts and rotator cuff.", 60)
            )
            else -> listOf(
                MobilityDrill("Thoracic Openers (Open Book)", "8 reps / side", "Anchor knees together; trace trailing hand across floor.", 60),
                MobilityDrill("Band Pull-Aparts (Supinated)", "2 × 15 reps", "Squeeze mid-traps and warm posterior shoulder capsule.", 60),
                MobilityDrill("Cable / Band External Rotations", "12 reps / arm", "Pin elbow to ribs; rotate forearm smoothly outward.", 60),
                MobilityDrill("Scapular Wall Slides", "10 slow reps", "Keep wrists and lower back flush to wall.", 60)
            )
        }
    }

    /** Zone 2 Heart Rate Target Range (60-70% of 220 - age) */
    fun zone2HeartRateRange(age: Int): Pair<Int, Int> {
        val maxHr = (220 - age.coerceIn(15, 80))
        return (maxHr * 0.60f).roundToInt() to (maxHr * 0.70f).roundToInt()
    }

    /** Paste-to-Routine Text Parser (WhatsApp / Reddit / Coach messages -> structured ExerciseEntity list) */
    fun parsePastedRoutine(
        rawText: String,
        programId: String,
        dayTitle: String
    ): List<ExerciseEntity> {
        val lines = rawText.lines().map { it.trim() }.filter { it.length >= 3 }
        val result = mutableListOf<ExerciseEntity>()

        val setRepRegex = Regex("(\\d+)\\s*[xX×]\\s*(\\d+(?:-\\d+)?)\\s*(s|sec|secs|hold|reps?)?", RegexOption.IGNORE_CASE)
        val weightRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(kg|lbs?|lb)", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val cleaned = line.trimStart('-', '*', '•', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.', ')', ' ')
            if (cleaned.length < 3) continue

            val srMatch = setRepRegex.find(cleaned)
            val wMatch = weightRegex.find(cleaned)

            val sets = srMatch?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(1, 12) ?: 3
            val repPart = srMatch?.groupValues?.get(2) ?: "10"
            val unitPart = srMatch?.groupValues?.get(3)?.lowercase() ?: ""
            val isHold = unitPart.startsWith("s") || unitPart.contains("hold") || cleaned.lowercase().contains("hold") || cleaned.lowercase().contains("plank")
            val holdSec = if (isHold) repPart.substringBefore("-").toIntOrNull() ?: 30 else 0
            val repsLabel = if (isHold) "${holdSec}s hold" else "$repPart reps"

            var weightKg = 0f
            if (wMatch != null) {
                val rawNum = wMatch.groupValues[1].toFloatOrNull() ?: 0f
                val unit = wMatch.groupValues[2].lowercase()
                weightKg = if (unit.startsWith("lb")) ((rawNum / KG_TO_LBS) / 2.5f).roundToInt() * 2.5f else rawNum
            }

            var name = cleaned
            if (srMatch != null) name = name.replace(srMatch.value, "")
            if (wMatch != null) name = name.replace(wMatch.value, "")
            name = name.replace(Regex("[@•|]+"), " ").replace(Regex("\\s+"), " ").trim(' ', '-', ':', ',')
            if (name.length < 2) continue

            val muscle = inferMuscleGroup(name)
            result.add(
                ExerciseEntity(
                    programId = programId,
                    dayTitle = dayTitle,
                    name = name,
                    category = "Imported • ${muscle.label}",
                    primaryMuscle = muscle,
                    targetSets = sets,
                    targetRepsOrSec = repsLabel,
                    isIsometric = isHold,
                    holdTargetSec = holdSec,
                    weightKg = weightKg,
                    previousWeightKg = weightKg,
                    restSeconds = if (isHold) 75 else if (weightKg >= 60f) 150 else 90,
                    isCompoundBarbell = weightKg >= 40f && !isHold
                )
            )
        }
        return result
    }

    private fun inferMuscleGroup(name: String): MuscleGroup {
        val l = name.lowercase()
        return when {
            "squat" in l || "quad" in l || "leg press" in l || "lunge" in l || "extension" in l -> MuscleGroup.QUADS
            "deadlift" in l || "rdl" in l || "hamstring" in l || "leg curl" in l || "calf" in l -> MuscleGroup.HAMSTRINGS
            "bench" in l || "chest" in l || "fly" in l || "pec" in l || "dip" in l || "push-up" in l -> MuscleGroup.CHEST
            "pull" in l || "row" in l || "lat" in l || "chin" in l || "lever" in l -> MuscleGroup.BACK
            "press" in l || "delt" in l || "lateral" in l || "handstand" in l || "planche" in l -> MuscleGroup.SHOULDERS
            "curl" in l || "tricep" in l || "pushdown" in l || "bicep" in l -> MuscleGroup.ARMS
            else -> MuscleGroup.CORE
        }
    }

    /** Full Data Sovereignty CSV Exporter */
    fun exportWorkoutCsv(exercises: List<ExerciseEntity>): String {
        val today = DateUtils.todayIso()
        val header = "Date,Program,Day,Exercise,Muscle,WeightKg,SetsDone,TargetSets,RepsOrHold,Est1RM_Kg\n"
        val body = exercises.joinToString("\n") { ex ->
            val est = estimateOneRepMax(ex.weightKg, ex.targetRepsOrSec)
            val safeName = ex.name.replace(",", " ")
            "$today,${ex.programId},${ex.dayTitle},$safeName,${ex.primaryMuscle.label},${ex.weightKg},${ex.completedSets},${ex.targetSets},${ex.targetRepsOrSec},$est"
        }
        return header + body
    }

    /** Full Data Sovereignty JSON Exporter */
    fun exportFullJson(
        exercises: List<ExerciseEntity>,
        supplements: List<SupplementEntity>,
        nutrition: DailyNutritionEntity
    ): String {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"app\": \"LOCKOUT\",")
        sb.appendLine("  \"exportDate\": \"${DateUtils.todayIso()}\",")
        sb.appendLine("  \"nutrition\": { \"calories\": ${nutrition.caloriesConsumed}, \"proteinG\": ${nutrition.proteinGrams}, \"waterMl\": ${nutrition.waterMl} },")
        sb.appendLine("  \"exercisesCount\": ${exercises.size},")
        sb.appendLine("  \"supplementsCount\": ${supplements.size},")
        sb.appendLine("  \"exercises\": [")
        exercises.forEachIndexed { idx, ex ->
            val comma = if (idx < exercises.lastIndex) "," else ""
            sb.appendLine("    { \"name\": \"${ex.name.replace("\"", "'")}\", \"day\": \"${ex.dayTitle}\", \"weightKg\": ${ex.weightKg}, \"sets\": \"${ex.completedSets}/${ex.targetSets}\", \"target\": \"${ex.targetRepsOrSec}\" }$comma")
        }
        sb.appendLine("  ]")
        sb.appendLine("}")
        return sb.toString()
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

    /** Profile-Aware 1-Tap Biomechanical Smart Machine Swapper */
    fun getSmartSwapAlternatives(
        exercise: ExerciseEntity,
        profile: GymProfile = GymProfile.COMMERCIAL
    ): List<SwapAlternative> {
        fun roundLoad(factor: Float): Float {
            if (exercise.weightKg <= 0f) return 0f
            return ((exercise.weightKg * factor) / 2.5f).roundToInt().coerceAtLeast(1) * 2.5f
        }

        val lowerName = exercise.name.lowercase()
        return when {
            "incline" in lowerName || "bench" in lowerName || "chest" in lowerName -> when (profile) {
                GymProfile.CALISTHENICS -> listOf(
                    SwapAlternative("Weighted Ring Dips", "Chest • Rings", roundLoad(0.25f), "Deep pec stretch + RTO stability"),
                    SwapAlternative("Deficit Archer Push-Ups", "Chest • Bodyweight", 0f, "High-tension unilateral pressing"),
                    SwapAlternative("Pseudo-Planche Push-Ups", "Chest & Delts", 0f, "Lean forward to scale mechanical load")
                )
                GymProfile.HOTEL -> listOf(
                    SwapAlternative("Flat Dumbbell Press", "Chest • Hotel DB", roundLoad(0.42f), "Per dumbbell • available in any hotel gym"),
                    SwapAlternative("Floor Dumbbell Press", "Chest • Joint Safe", roundLoad(0.40f), "No bench required • locks shoulder safety"),
                    SwapAlternative("Feet-Elevated Push-Ups", "Upper Chest • BW", 0f, "Slow 3-second eccentric")
                )
                GymProfile.HOME_GARAGE -> listOf(
                    SwapAlternative("Incline Dumbbell Press", "Chest • Home DB", roundLoad(0.42f), "Per hand • natural shoulder path"),
                    SwapAlternative("Barbell Floor Press", "Chest • Power", roundLoad(0.90f), "Heavy lockout overload")
                )
                GymProfile.COMMERCIAL -> listOf(
                    SwapAlternative("Incline Smith Machine Press", "Chest • Guided Rail", roundLoad(0.95f), "Fixed bar path • safe to push near failure"),
                    SwapAlternative("Converging Chest Press Machine", "Chest • Plate-Loaded", roundLoad(0.90f), "Natural arc • zero setup wait"),
                    SwapAlternative("Incline Dumbbell Press", "Chest • Unilateral", roundLoad(0.42f), "Per dumbbell • neutral shoulder grip"),
                    SwapAlternative("Low-to-High Cable Fly", "Upper Chest • Cable", roundLoad(0.30f), "Constant tension • upper fibers")
                )
            }
            "squat" in lowerName || "leg press" in lowerName -> when (profile) {
                GymProfile.CALISTHENICS -> listOf(
                    SwapAlternative("Pistol Squat Progression", "Quads • Bodyweight", 0f, "Single-leg balance & deep knee flexion"),
                    SwapAlternative("Shrimp Squat", "Quads & Glutes", 0f, "High quad leverage with zero iron"),
                    SwapAlternative("Sissy Squat (Assisted)", "Quads • Isolation", 0f, "Extreme rectus femoris stretch")
                )
                GymProfile.HOTEL -> listOf(
                    SwapAlternative("Heavy Goblet Squat", "Quads • Hotel DB", roundLoad(0.35f), "Heels elevated on small plate for quad bias"),
                    SwapAlternative("DB Bulgarian Split Squat", "Quads & Glutes", roundLoad(0.22f), "Per dumbbell • brutal single-leg stimulus")
                )
                GymProfile.HOME_GARAGE -> listOf(
                    SwapAlternative("Front Squat", "Quads • Barbell", roundLoad(0.80f), "Upright torso • high quad demand"),
                    SwapAlternative("Bulgarian Split Squat", "Quads • Unilateral", roundLoad(0.22f), "Per hand • home bench friendly")
                )
                GymProfile.COMMERCIAL -> listOf(
                    SwapAlternative("Hack Squat", "Quads • Plate-Loaded", roundLoad(0.85f), "Deep quad bias • zero spinal shear"),
                    SwapAlternative("45-Degree Leg Press", "Quads & Glutes", roundLoad(1.55f), "High output • lower back supported"),
                    SwapAlternative("Smith Machine Squat", "Quads • Guided", roundLoad(0.90f), "Step feet 6–8\" forward for quad sweep"),
                    SwapAlternative("Bulgarian Split Squat", "Quads • Unilateral", roundLoad(0.22f), "Per dumbbell • fixes side imbalances")
                )
            }
            "deadlift" in lowerName || "rdl" in lowerName -> when (profile) {
                GymProfile.CALISTHENICS -> listOf(
                    SwapAlternative("Nordic Hamstring Curls", "Hamstrings • Elite", 0f, "Supramaximal eccentric hamstring overload"),
                    SwapAlternative("Single-Leg Sliding Curl", "Hamstrings • BW", 0f, "Zero equipment posterior chain fire")
                )
                GymProfile.HOTEL -> listOf(
                    SwapAlternative("Dumbbell Romanian Deadlift", "Hamstrings • Hotel DB", roundLoad(0.35f), "Per hand • slow 3s hinge"),
                    SwapAlternative("Single-Leg DB RDL", "Hamstrings • Balance", roundLoad(0.22f), "Doubles effective tension with light hotel DBs")
                )
                else -> listOf(
                    SwapAlternative("Dumbbell Romanian Deadlift", "Hamstrings • Hinge", roundLoad(0.35f), "Per hand • zero barbell rack required"),
                    SwapAlternative("45° Back Extension (Weighted)", "Posterior Chain", roundLoad(0.25f), "Lower-back friendly glute & hamstring focus"),
                    SwapAlternative("Seated Leg Curl", "Hamstrings • Machine", roundLoad(0.55f), "Maximal stretched-position hamstring tension")
                )
            }
            "pull" in lowerName || "chin" in lowerName || "row" in lowerName -> when (profile) {
                GymProfile.CALISTHENICS -> listOf(
                    SwapAlternative("Ring Inverted Rows", "Upper Back • Rings", 0f, "Elevate feet to scale difficulty"),
                    SwapAlternative("Strict Pull-Ups", "Lats • Bodyweight", 0f, "Full dead-hang to chest-to-bar"),
                    SwapAlternative("Front Lever Tuck Rows", "Back • Gymnastics", 0f, "Elite horizontal pulling strength")
                )
                GymProfile.HOTEL -> listOf(
                    SwapAlternative("Single-Arm DB Row", "Lats • Hotel DB", roundLoad(0.50f), "Bench-supported heavy pull"),
                    SwapAlternative("Chest-Supported Incline DB Row", "Upper Back", roundLoad(0.35f), "Per dumbbell • zero lower-back fatigue")
                )
                else -> listOf(
                    SwapAlternative("Chest-Supported T-Bar Row", "Upper Back", roundLoad(0.95f), "Eliminates lower-back fatigue"),
                    SwapAlternative("Neutral-Grip Lat Pulldown", "Lats • Cable", roundLoad(0.95f).coerceAtLeast(55f), "Joint-friendly shoulder rotation"),
                    SwapAlternative("Seated Cable Row (Mag Grip)", "Mid-Back", roundLoad(0.90f), "Smooth resistance curve")
                )
            }
            else -> when (exercise.primaryMuscle) {
                MuscleGroup.CHEST -> listOf(
                    SwapAlternative("Pec Deck Machine", "Chest • Isolation", roundLoad(0.85f), "Constant peak tension"),
                    SwapAlternative("Dumbbell Chest Fly", "Chest • Free Weight", roundLoad(0.30f), "Deep eccentric stretch")
                )
                MuscleGroup.SHOULDERS -> listOf(
                    SwapAlternative("Dumbbell Lateral Raise", "Side Delts • DB", roundLoad(0.6f).coerceAtLeast(8f), "Available in every gym profile"),
                    SwapAlternative("Pike Push-Up", "Delts • Bodyweight", 0f, "Zero-equipment overhead strength")
                )
                MuscleGroup.BACK -> listOf(
                    SwapAlternative("Seated Cable Row", "Back • Cable", roundLoad(0.9f).coerceAtLeast(50f), "Full stretch and squeeze"),
                    SwapAlternative("Single-Arm DB Row", "Back • Dumbbell", roundLoad(0.45f).coerceAtLeast(22.5f), "Universal profile swap")
                )
                MuscleGroup.ARMS -> listOf(
                    SwapAlternative("Dumbbell Hammer Curl", "Arms • DB", roundLoad(0.45f).coerceAtLeast(12f), "Neutral wrist comfort"),
                    SwapAlternative("Overhead DB Tricep Extension", "Triceps • DB", roundLoad(0.6f).coerceAtLeast(18f), "Full long-head stretch")
                )
                MuscleGroup.CORE -> listOf(
                    SwapAlternative("Hanging Knee / Leg Raises", "Core • Pull-Up Bar", 0f, "Pelvic curl focus"),
                    SwapAlternative("Hollow Body Hold", "Core • Gymnastics", 0f, "Zero equipment anterior brace")
                )
                else -> listOf(
                    SwapAlternative("DB Walking Lunges", "Legs • Dumbbell", roundLoad(0.3f).coerceAtLeast(16f), "Athletic knee & hip control"),
                    SwapAlternative("45-Degree Leg Press", "Legs • Machine", roundLoad(1.2f).coerceAtLeast(120f), "Stable heavy leg volume")
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

    /** Computes exact total days logged, consecutive streak, and 7-day timeline (oldest -> today). */
    fun computeAdherenceStats(
        loggedDates: Set<String>,
        todayIso: String = DateUtils.todayIso()
    ): AdherenceStats {
        val last7 = (6 downTo 0).map { offset ->
            DateUtils.daysAgoIso(offset) in loggedDates
        }
        var streak = 0
        var offset = 0
        // If not logged today yet, check if yesterday started an active streak
        if (todayIso !in loggedDates && DateUtils.daysAgoIso(1) in loggedDates) {
            offset = 1
        }
        while (offset < 365 && DateUtils.daysAgoIso(offset) in loggedDates) {
            streak++
            offset++
        }
        return AdherenceStats(
            totalDays = loggedDates.size,
            currentStreakDays = streak,
            last7DaysMask = last7
        )
    }

    /** Clinical Mifflin-St Jeor BMR + TDEE + Macro & Hydration Calculator. */
    fun calculateCaloriePlan(
        weightKg: Float,
        heightCm: Int,
        ageYears: Int,
        isMale: Boolean,
        activityLevel: ActivityLevel,
        goal: NutritionGoal
    ): CaloriePlanResult {
        val w = weightKg.coerceIn(35f, 220f)
        val h = heightCm.coerceIn(130, 230)
        val age = ageYears.coerceIn(14, 90)

        val bmrRaw = (10f * w) + (6.25f * h) - (5f * age) + if (isMale) 5f else -161f
        val bmr = bmrRaw.roundToInt().coerceAtLeast(1100)
        val tdee = (bmr * activityLevel.multiplier).roundToInt()
        val target = (tdee + goal.calorieDelta).coerceAtLeast(1350)
        val trainingKcal = target + 220
        val restKcal = (target - 160).coerceAtLeast(1300)

        val protein = (w * goal.proteinPerKg).roundToInt().coerceAtLeast(90)
        val fats = (w * 0.85f).roundToInt().coerceAtLeast(45)
        val remainingKcal = (target - (protein * 4) - (fats * 9)).coerceAtLeast(160)
        val carbs = remainingKcal / 4
        val waterMl = ((w * 42f) / 100f).roundToInt() * 100

        val heightM = h / 100f
        val bmiVal = ((w / (heightM * heightM)) * 10f).roundToInt() / 10f
        val bmiCategory = when {
            bmiVal < 18.5f -> "Lean / Underweight"
            bmiVal < 25f -> "Athletic Optimal"
            bmiVal < 30f -> "Muscular / Elevated"
            else -> "High Mass"
        }

        return CaloriePlanResult(
            bmrKcal = bmr,
            tdeeKcal = tdee,
            targetCalories = target,
            trainingDayCalories = trainingKcal,
            restDayCalories = restKcal,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatsGrams = fats,
            waterTargetMl = waterMl.coerceIn(2400, 5000),
            bmi = bmiVal,
            bmiCategory = bmiCategory
        )
    }

    fun parseMuscleOverrides(csv: String): Map<MuscleGroup, RecoveryStatus> {
        if (csv.isBlank()) return emptyMap()
        val map = mutableMapOf<MuscleGroup, RecoveryStatus>()
        csv.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val muscle = runCatching { MuscleGroup.valueOf(parts[0].trim()) }.getOrNull()
                val status = runCatching { RecoveryStatus.valueOf(parts[1].trim()) }.getOrNull()
                if (muscle != null && status != null) {
                    map[muscle] = status
                }
            }
        }
        return map
    }

    fun serializeMuscleOverrides(map: Map<MuscleGroup, RecoveryStatus>): String =
        map.entries.joinToString(",") { "${it.key.name}:${it.value.name}" }

    fun mergeMuscleRecovery(
        autoMap: Map<MuscleGroup, RecoveryStatus>,
        overrides: Map<MuscleGroup, RecoveryStatus>
    ): Map<MuscleGroup, RecoveryStatus> {
        return MuscleGroup.entries.associateWith { muscle ->
            overrides[muscle] ?: autoMap[muscle] ?: RecoveryStatus.RECOVERED
        }
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
}
