package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val shortDisplayFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM d", Locale.US)

    fun todayIso(): String = isoFormat.format(Date())

    fun addDaysDisplay(daysFromNow: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow.coerceAtLeast(0))
        return shortDisplayFormat.format(cal.time)
    }

    fun daysAgoIso(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return isoFormat.format(cal.time)
    }
}

enum class SupplementTiming(val label: String, val defaultWindowHint: String) {
    MORNING("Upon Waking", "7:00 AM • With Breakfast"),
    PRE_WORKOUT("Pre-Workout", "30 mins Pre-Session"),
    INTRA_WORKOUT("Intra-Workout", "During Training"),
    POST_WORKOUT("Post-Workout", "Within 45 mins Post"),
    NIGHT("Before Sleep", "45 mins Pre-Bed")
}

enum class VaultCategory(val title: String, val shortLabel: String) {
    INSTRUMENT("Gym Instruments", "Instruments"),
    ACCESSORY("Accessories", "Gear"),
    SUPPLEMENT("Supplements", "Supps"),
    APPAREL("Gym Clothes & Shoes", "Apparel")
}

enum class SetType(val badge: String, val label: String) {
    WORKING("WORK", "Working Set"),
    WARMUP("W", "Warm-Up"),
    DROP("D", "Drop Set"),
    REST_PAUSE("RP", "Rest-Pause"),
    MYO_REPS("M", "Myo-Reps"),
    FAILURE("F", "To Failure")
}

enum class NutritionGoal(val label: String, val calorieDelta: Int, val proteinPerKg: Float) {
    LEAN_BULK("Lean Bulk", +250, 2.0f),
    RECOMP("Body Recomp", 0, 2.2f),
    MAINTAIN("Maintain", 0, 1.9f),
    FAT_LOSS("Fat Loss Cut", -400, 2.2f)
}

enum class MuscleGroup(val label: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    CORE("Core"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings")
}

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val programId: String,
    val dayTitle: String,
    val name: String,
    val category: String,
    val primaryMuscle: MuscleGroup,
    val targetSets: Int,
    val targetRepsOrSec: String,
    val isIsometric: Boolean = false,
    val holdTargetSec: Int = 0,
    val weightKg: Float = 0f,
    val previousWeightKg: Float = 0f,
    val targetRpe: Float = 8.5f,
    val lastLoggedRpe: Float = 0f,
    val restSeconds: Int = 90,
    val completedSets: Int = 0,
    val setType: SetType = SetType.WORKING,
    val isCompoundBarbell: Boolean = false,
    val recommendedGear: String = "",
    val coachingCue: String = "",
    val jointRiskArea: String = "",
    val lastTrainedMillis: Long = 0L
)

@Entity(tableName = "supplements")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val timing: SupplementTiming,
    val servingsLeft: Int = 30,
    val tier: String = "Tier A",
    val cycleNote: String = "",
    val interactionKey: String = "" // e.g. "CREATINE", "CAFFEINE", "VIT_D3", "OMEGA3", "ZINC", "CALCIUM", "IRON", "MAGNESIUM"
)

// Date-stamped log solves midnight reset & tracks 30-day streaks
@Entity(
    tableName = "supplement_logs",
    primaryKeys = ["supplementId", "dateIso"]
)
data class SupplementLogEntity(
    val supplementId: Int,
    val dateIso: String = DateUtils.todayIso(),
    val takenAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_nutrition")
data class DailyNutritionEntity(
    @PrimaryKey val dateIso: String = DateUtils.todayIso(),
    val caloriesConsumed: Int = 1850,
    val caloriesTarget: Int = 2650,
    val proteinGrams: Int = 138,
    val proteinTarget: Int = 180,
    val carbsGrams: Int = 210,
    val carbsTarget: Int = 310,
    val fatsGrams: Int = 52,
    val fatsTarget: Int = 72,
    val waterMl: Int = 2000,
    val waterTargetMl: Int = 3500,
    val isTrainingDay: Boolean = true,
    val goal: NutritionGoal = NutritionGoal.LEAN_BULK,
    val bodyWeightKg: Float = 78.5f
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: VaultCategory,
    val name: String,
    val subTag: String,
    val primaryBenefit: String,
    val howToUseOrSetup: String,
    val commonMistake: String,
    val proRecommendation: String,
    val defaultDosage: String = "",
    val defaultTiming: SupplementTiming = SupplementTiming.POST_WORKOUT,
    val isInMyGearBag: Boolean = false
)

@Entity(tableName = "skill_milestones")
data class SkillMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val treeName: String,
    val stepOrder: Int,
    val title: String,
    val requirement: String,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "app_prefs")
data class AppPrefsEntity(
    @PrimaryKey val id: Int = 1,
    val selectedProgramId: String = "ppl_6day",
    val selectedDayTitle: String = "Push Day",
    val isDaylightMode: Boolean = false,
    val plannedWorkoutHour: Int = 18, // 6:00 PM default for Caffeine Cutoff Guard
    val flaggedJointsCsv: String = "", // e.g. "Left Shoulder,Lower Back"
    val barbellWeightKg: Float = 20f,
    val trainingStreakDays: Int = 6
)

class LockoutTypeConverters {
    @TypeConverter
    fun fromTiming(value: SupplementTiming): String = value.name

    @TypeConverter
    fun toTiming(value: String): SupplementTiming =
        runCatching { SupplementTiming.valueOf(value) }.getOrDefault(SupplementTiming.POST_WORKOUT)

    @TypeConverter
    fun fromVaultCategory(value: VaultCategory): String = value.name

    @TypeConverter
    fun toVaultCategory(value: String): VaultCategory =
        runCatching { VaultCategory.valueOf(value) }.getOrDefault(VaultCategory.INSTRUMENT)

    @TypeConverter
    fun fromSetType(value: SetType): String = value.name

    @TypeConverter
    fun toSetType(value: String): SetType =
        runCatching { SetType.valueOf(value) }.getOrDefault(SetType.WORKING)

    @TypeConverter
    fun fromNutritionGoal(value: NutritionGoal): String = value.name

    @TypeConverter
    fun toNutritionGoal(value: String): NutritionGoal =
        runCatching { NutritionGoal.valueOf(value) }.getOrDefault(NutritionGoal.LEAN_BULK)

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup =
        runCatching { MuscleGroup.valueOf(value) }.getOrDefault(MuscleGroup.CHEST)
}
