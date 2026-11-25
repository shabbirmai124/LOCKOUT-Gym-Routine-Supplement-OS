package com.example.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLevel
import com.example.data.AppPrefsEntity
import com.example.data.ConditioningMode
import com.example.data.DailyHabitEntity
import com.example.data.DailyNutritionEntity
import com.example.data.DateUtils
import com.example.data.ExerciseEntity
import com.example.data.GymProfile
import com.example.data.HabitLogEntity
import com.example.data.LockoutRepository
import com.example.data.LockoutSeedData
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.data.SetType
import com.example.data.SkillMilestoneEntity
import com.example.data.SupplementEntity
import com.example.data.SupplementLogEntity
import com.example.data.SupplementTiming
import com.example.data.VaultItemEntity
import com.example.domain.BiofeedbackReport
import com.example.domain.CaloriePlanResult
import com.example.domain.ClinicalGuardrail
import com.example.domain.LockoutDomainEngine
import com.example.domain.RecoveryStatus
import com.example.domain.SwapAlternative
import com.example.engine.ConditioningTick
import com.example.engine.HoldTimerTick
import com.example.engine.RestTimerEngine
import com.example.engine.SupplementAlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SupplementDisplayItem(
    val entity: SupplementEntity,
    val isTakenToday: Boolean,
    val runOutDateLabel: String,
    val isLowStock: Boolean,
    val totalDaysTaken: Int = 0,
    val streakDays: Int = 0,
    val last7DaysTaken: List<Boolean> = List(7) { false },
    val reminderTimeLabel: String = DateUtils.formatTime12h(entity.reminderHour, entity.reminderMinute)
)

@Immutable
data class HabitDisplayItem(
    val entity: DailyHabitEntity,
    val isCompletedToday: Boolean,
    val totalDaysCompleted: Int,
    val streakDays: Int,
    val last7DaysMask: List<Boolean>
)

@Immutable
data class TimerOverlayState(
    val restSecondsRemaining: Int = 0,
    val restTotalSeconds: Int = 0,
    val restExerciseName: String = "",
    val isRestRunning: Boolean = false,
    val holdExerciseName: String = "",
    val holdTick: HoldTimerTick? = null,
    val isHoldRunning: Boolean = false,
    val conditioningTick: ConditioningTick? = null,
    val isConditioningRunning: Boolean = false,
    val amrapRoundsCompleted: Int = 0
)

@Immutable
data class LockoutUiState(
    val isLoaded: Boolean = false,
    val prefs: AppPrefsEntity = AppPrefsEntity(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val activeDayExercises: List<ExerciseEntity> = emptyList(),
    val supplements: List<SupplementDisplayItem> = emptyList(),
    val habits: List<HabitDisplayItem> = emptyList(),
    val nutrition: DailyNutritionEntity = DailyNutritionEntity(),
    val caloriePlan: CaloriePlanResult = LockoutDomainEngine.calculateCaloriePlan(
        weightKg = 78.5f,
        heightCm = 178,
        ageYears = 26,
        isMale = true,
        activityLevel = ActivityLevel.MODERATE,
        goal = NutritionGoal.LEAN_BULK
    ),
    val effectiveWaterTargetMl: Int = 3500,
    val vaultItems: List<VaultItemEntity> = emptyList(),
    val ownedGearNames: Set<String> = emptySet(),
    val skillMilestones: List<SkillMilestoneEntity> = emptyList(),
    val clinicalGuardrails: List<ClinicalGuardrail> = emptyList(),
    val muscleRecovery: Map<MuscleGroup, RecoveryStatus> = emptyMap(),
    val readinessScore: Int = 84,
    val creatineStreakDays: Int = 1,
    val flaggedJoints: Set<String> = emptySet(),
    val activeCoachingDirective: String? = null,
    val timerState: TimerOverlayState = TimerOverlayState()
)

class LockoutViewModel(
    private val repository: LockoutRepository,
    private val timerEngine: RestTimerEngine,
    private val alarmScheduler: SupplementAlarmScheduler
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerOverlayState())
    private val _coachingDirective = MutableStateFlow<String?>(null)
    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 6)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var restTimerJob: Job? = null
    private var holdTimerJob: Job? = null
    private var conditioningJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val coreDataFlow = combine(
        repository.allExercises,
        repository.allSupplements,
        repository.todaySupplementLogs(),
        repository.allSupplementLogs,
        repository.todayNutrition()
    ) { exercises, supplements, todayLogs, allLogs, nutrition ->
        CoreBundle(
            exercises = exercises,
            supplements = supplements,
            todayTakenIds = todayLogs.map { it.supplementId }.toSet(),
            allSupplementLogs = allLogs,
            nutrition = nutrition
        )
    }

    private val habitsFlow = combine(
        repository.allHabits,
        repository.todayHabitLogs(),
        repository.allHabitLogs
    ) { habits, todayHabitLogs, allHabitLogs ->
        HabitsBundle(
            habits = habits,
            todayHabitIds = todayHabitLogs.map { it.habitId }.toSet(),
            allHabitLogs = allHabitLogs
        )
    }

    private val metaDataFlow = combine(
        repository.allVaultItems,
        repository.allSkillMilestones,
        repository.appPrefs,
        _timerState,
        _coachingDirective
    ) { vaultItems, skills, prefs, timer, directive ->
        MetaBundle(vaultItems, skills, prefs, timer, directive)
    }

    val uiState: StateFlow<LockoutUiState> = combine(coreDataFlow, habitsFlow, metaDataFlow) { core, habitsBundle, meta ->
        val prefs = meta.prefs
        val todayIso = DateUtils.todayIso()
        val activeDayExercises = core.exercises.filter {
            it.programId == prefs.selectedProgramId && it.dayTitle == prefs.selectedDayTitle
        }

        val suppDatesById = core.allSupplementLogs
            .groupBy { it.supplementId }
            .mapValues { (_, list) -> list.map { it.dateIso }.toSet() }

        val suppItems = core.supplements.map { sup ->
            val dates = suppDatesById[sup.id].orEmpty()
            val stats = LockoutDomainEngine.computeAdherenceStats(dates, todayIso)
            SupplementDisplayItem(
                entity = sup,
                isTakenToday = sup.id in core.todayTakenIds,
                runOutDateLabel = DateUtils.addDaysDisplay(sup.servingsLeft),
                isLowStock = sup.servingsLeft <= 7,
                totalDaysTaken = stats.totalDays,
                streakDays = stats.currentStreakDays,
                last7DaysTaken = stats.last7DaysMask,
                reminderTimeLabel = DateUtils.formatTime12h(sup.reminderHour, sup.reminderMinute)
            )
        }

        val habitDatesById = habitsBundle.allHabitLogs
            .groupBy { it.habitId }
            .mapValues { (_, list) -> list.map { it.dateIso }.toSet() }

        val habitItems = habitsBundle.habits.map { habit ->
            val dates = habitDatesById[habit.id].orEmpty()
            val stats = LockoutDomainEngine.computeAdherenceStats(dates, todayIso)
            HabitDisplayItem(
                entity = habit,
                isCompletedToday = habit.id in habitsBundle.todayHabitIds,
                totalDaysCompleted = stats.totalDays,
                streakDays = stats.currentStreakDays,
                last7DaysMask = stats.last7DaysMask
            )
        }

        val hasCreatine = core.supplements.any {
            it.interactionKey.equals("CREATINE", ignoreCase = true) ||
                it.name.contains("creatine", ignoreCase = true)
        }
        val effectiveWaterTarget = core.nutrition.waterTargetMl + if (hasCreatine) 500 else 0

        val guardrails = LockoutDomainEngine.evaluateSupplementGuardrails(
            supplements = core.supplements,
            plannedWorkoutHour = prefs.plannedWorkoutHour
        )
        val autoRecoveryMap = LockoutDomainEngine.calculateMuscleRecoveryMap(core.exercises)
        val manualOverrides = LockoutDomainEngine.parseMuscleOverrides(prefs.muscleOverrideCsv)
        val recoveryMap = LockoutDomainEngine.mergeMuscleRecovery(autoRecoveryMap, manualOverrides)

        val takenCount = suppItems.count { it.isTakenToday }
        val baseReadiness = LockoutDomainEngine.calculateReadinessScore(
            nutrition = core.nutrition,
            effectiveWaterTargetMl = effectiveWaterTarget,
            supplementsTaken = takenCount,
            totalSupplements = suppItems.size,
            recoveryMap = recoveryMap
        )
        val habitBonus = if (habitItems.isNotEmpty()) {
            val ratio = habitItems.count { it.isCompletedToday }.toFloat() / habitItems.size
            (ratio * 6f).toInt()
        } else 0
        val readiness = (baseReadiness + habitBonus).coerceIn(40, 100)

        val caloriePlan = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = core.nutrition.bodyWeightKg,
            heightCm = prefs.athleteHeightCm,
            ageYears = prefs.athleteAge,
            isMale = prefs.isMale,
            activityLevel = prefs.activityLevel,
            goal = core.nutrition.goal
        )

        val ownedGear = meta.vaultItems
            .filter { it.isInMyGearBag }
            .map { it.name }
            .toSet()

        val flaggedJoints = prefs.flaggedJointsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val creatineItem = suppItems.firstOrNull {
            it.entity.interactionKey == "CREATINE" || it.entity.name.contains("Creatine", true)
        }
        val creatineStreak = (creatineItem?.streakDays ?: 0).coerceAtLeast(if (creatineItem?.isTakenToday == true) 1 else 0)

        LockoutUiState(
            isLoaded = true,
            prefs = prefs,
            allExercises = core.exercises,
            activeDayExercises = activeDayExercises,
            supplements = suppItems,
            habits = habitItems,
            nutrition = core.nutrition,
            caloriePlan = caloriePlan,
            effectiveWaterTargetMl = effectiveWaterTarget,
            vaultItems = meta.vaultItems,
            ownedGearNames = ownedGear,
            skillMilestones = meta.skills,
            clinicalGuardrails = guardrails,
            muscleRecovery = recoveryMap,
            readinessScore = readiness,
            creatineStreakDays = creatineStreak,
            flaggedJoints = flaggedJoints,
            activeCoachingDirective = meta.coachingDirective,
            timerState = meta.timer
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LockoutUiState()
    )

    // ==================== WORKOUT & TIMERS ====================

    fun logSetCompleted(exercise: ExerciseEntity) {
        viewModelScope.launch {
            val nextSet = if (exercise.completedSets < exercise.targetSets) {
                exercise.completedSets + 1
            } else {
                0
            }
            val updated = exercise.copy(
                completedSets = nextSet,
                lastTrainedMillis = if (nextSet > 0) System.currentTimeMillis() else exercise.lastTrainedMillis
            )
            repository.upsertExercise(updated)
            if (nextSet > 0) {
                startRestTimer(exercise.restSeconds, exercise.name)
            }
        }
    }

    fun adjustExerciseWeight(exercise: ExerciseEntity, deltaKg: Float) {
        viewModelScope.launch {
            val nextKg = (exercise.weightKg + deltaKg).coerceAtLeast(0f)
            repository.upsertExercise(exercise.copy(weightKg = nextKg))
        }
    }

    fun cycleSetType(exercise: ExerciseEntity) {
        viewModelScope.launch {
            val types = SetType.entries
            val nextType = types[(types.indexOf(exercise.setType) + 1) % types.size]
            repository.upsertExercise(exercise.copy(setType = nextType))
        }
    }

    fun submitRpeCheckIn(exercise: ExerciseEntity, rpe: Float) {
        viewModelScope.launch {
            val decision = LockoutDomainEngine.applyProgressiveOverload(exercise, rpe)
            repository.upsertExercise(decision.updatedExercise)
            _coachingDirective.value = decision.summaryMessage
            _snackbarEvents.tryEmit(decision.summaryMessage)
        }
    }

    fun submitBiofeedback(report: BiofeedbackReport) {
        viewModelScope.launch {
            val prescription = LockoutDomainEngine.evaluateMesocycleFeedback(report)
            val targets = uiState.value.allExercises.filter { it.primaryMuscle == report.muscleGroup }
            targets.forEach { ex ->
                val newSets = if (prescription.triggerDeload) {
                    (ex.targetSets / 2).coerceAtLeast(2)
                } else {
                    (ex.targetSets + prescription.setDelta).coerceIn(2, 8)
                }
                val newWeight = if (prescription.triggerDeload && !ex.isIsometric) {
                    ((ex.weightKg * 0.60f) / 2.5f).toInt() * 2.5f
                } else ex.weightKg
                repository.upsertExercise(ex.copy(targetSets = newSets, weightKg = newWeight))
            }
            _coachingDirective.value = prescription.coachingNote
            _snackbarEvents.tryEmit(prescription.coachingNote)
        }
    }

    fun setMuscleRecoveryOverride(muscle: MuscleGroup, status: RecoveryStatus) {
        val currentPrefs = uiState.value.prefs
        val overrides = LockoutDomainEngine.parseMuscleOverrides(currentPrefs.muscleOverrideCsv).toMutableMap()
        overrides[muscle] = status
        viewModelScope.launch {
            repository.upsertAppPrefs(
                currentPrefs.copy(muscleOverrideCsv = LockoutDomainEngine.serializeMuscleOverrides(overrides))
            )
            _snackbarEvents.tryEmit("${muscle.label} recovery updated to ${status.label}")
        }
    }

    fun dismissCoachingDirective() {
        _coachingDirective.value = null
    }

    fun applySmartSwap(exercise: ExerciseEntity, alternative: SwapAlternative) {
        viewModelScope.launch {
            repository.upsertExercise(
                exercise.copy(
                    name = alternative.name,
                    category = alternative.category,
                    weightKg = alternative.convertedWeightKg,
                    coachingCue = alternative.note
                )
            )
            val useLbs = uiState.value.prefs.useLbs
            _snackbarEvents.tryEmit(
                "Swapped to ${alternative.name} (${LockoutDomainEngine.formatWeight(alternative.convertedWeightKg, useLbs)})"
            )
        }
    }

    fun adaptActiveDayToProfile(profile: GymProfile) {
        viewModelScope.launch {
            val currentDay = uiState.value.activeDayExercises
            var swappedCount = 0
            currentDay.forEach { ex ->
                val needsSwap = when (profile) {
                    GymProfile.HOTEL -> ex.isCompoundBarbell || "machine" in ex.name.lowercase() || "cable" in ex.name.lowercase()
                    GymProfile.CALISTHENICS -> ex.isCompoundBarbell || "machine" in ex.name.lowercase() || "cable" in ex.name.lowercase() || "leg press" in ex.name.lowercase()
                    GymProfile.HOME_GARAGE -> "machine" in ex.name.lowercase() || "pec deck" in ex.name.lowercase() || "hack squat" in ex.name.lowercase()
                    GymProfile.COMMERCIAL -> false
                }
                if (needsSwap) {
                    val alt = LockoutDomainEngine.getSmartSwapAlternatives(ex, profile).firstOrNull()
                    if (alt != null) {
                        repository.upsertExercise(
                            ex.copy(
                                name = alt.name,
                                category = alt.category,
                                weightKg = alt.convertedWeightKg,
                                coachingCue = alt.note,
                                isCompoundBarbell = false
                            )
                        )
                        swappedCount++
                    }
                }
            }
            val msg = if (swappedCount > 0) {
                "Adapted $swappedCount lifts to ${profile.label} equipment."
            } else {
                "All lifts in ${uiState.value.prefs.selectedDayTitle} already match ${profile.label}."
            }
            _coachingDirective.value = msg
            _snackbarEvents.tryEmit(msg)
        }
    }

    fun importPastedRoutine(rawText: String) {
        viewModelScope.launch {
            val prefs = uiState.value.prefs
            val parsed = LockoutDomainEngine.parsePastedRoutine(rawText, prefs.selectedProgramId, prefs.selectedDayTitle)
            if (parsed.isEmpty()) {
                _snackbarEvents.tryEmit("No exercises detected. Try format: Bench Press 4x8 80kg")
                return@launch
            }
            parsed.forEach { repository.upsertExercise(it) }
            _snackbarEvents.tryEmit("Imported ${parsed.size} exercises into ${prefs.selectedDayTitle}")
        }
    }

    fun addCustomExercise(
        name: String,
        category: String,
        primaryMuscle: MuscleGroup,
        targetSets: Int,
        targetRepsOrSec: String,
        weightKg: Float,
        restSeconds: Int,
        isIsometric: Boolean,
        holdTargetSec: Int
    ) {
        val prefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertExercise(
                ExerciseEntity(
                    programId = prefs.selectedProgramId,
                    dayTitle = prefs.selectedDayTitle,
                    name = name.trim(),
                    category = category.trim().ifEmpty { primaryMuscle.label },
                    primaryMuscle = primaryMuscle,
                    targetSets = targetSets.coerceIn(1, 12),
                    targetRepsOrSec = targetRepsOrSec.trim().ifEmpty { if (isIsometric) "${holdTargetSec}s hold" else "10 reps" },
                    isIsometric = isIsometric,
                    holdTargetSec = holdTargetSec,
                    weightKg = weightKg,
                    previousWeightKg = weightKg,
                    restSeconds = restSeconds.coerceIn(30, 300),
                    isCompoundBarbell = weightKg >= 40f && !isIsometric
                )
            )
            _snackbarEvents.tryEmit("Added ${name.trim()} to ${prefs.selectedDayTitle}")
        }
    }

    fun resetDaySets() {
        viewModelScope.launch {
            uiState.value.activeDayExercises.forEach { ex ->
                if (ex.completedSets > 0) {
                    repository.upsertExercise(ex.copy(completedSets = 0))
                }
            }
            _snackbarEvents.tryEmit("Session sets reset for ${uiState.value.prefs.selectedDayTitle}")
        }
    }

    fun startRestTimer(seconds: Int, exerciseName: String = "Rest") {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            _timerState.value = _timerState.value.copy(
                restSecondsRemaining = seconds,
                restTotalSeconds = seconds,
                restExerciseName = exerciseName,
                isRestRunning = true
            )
            timerEngine.startCountdown(seconds).collect { remaining ->
                _timerState.value = _timerState.value.copy(
                    restSecondsRemaining = remaining,
                    isRestRunning = remaining > 0
                )
            }
        }
    }

    fun addRestSeconds(delta: Int) {
        val current = _timerState.value
        if (current.isRestRunning) {
            val next = (current.restSecondsRemaining + delta).coerceIn(5, 600)
            startRestTimer(next, current.restExerciseName)
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            restSecondsRemaining = 0,
            restTotalSeconds = 0,
            isRestRunning = false
        )
    }

    fun startIsometricHoldTimer(exercise: ExerciseEntity) {
        holdTimerJob?.cancel()
        val target = exercise.holdTargetSec.takeIf { it > 0 } ?: 30
        holdTimerJob = viewModelScope.launch {
            _timerState.value = _timerState.value.copy(
                holdExerciseName = exercise.name,
                isHoldRunning = true
            )
            timerEngine.startIsometricHold(target).collect { tick ->
                _timerState.value = _timerState.value.copy(
                    holdTick = tick,
                    isHoldRunning = true
                )
            }
        }
    }

    fun stopIsometricHoldTimer() {
        holdTimerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            holdTick = null,
            holdExerciseName = "",
            isHoldRunning = false
        )
    }

    fun startConditioning(mode: ConditioningMode) {
        conditioningJob?.cancel()
        conditioningJob = viewModelScope.launch {
            _timerState.value = _timerState.value.copy(
                isConditioningRunning = true,
                amrapRoundsCompleted = 0
            )
            timerEngine.startConditioning(mode).collect { tick ->
                _timerState.value = _timerState.value.copy(
                    conditioningTick = tick,
                    isConditioningRunning = !tick.isFinished
                )
            }
        }
    }

    fun incrementAmrapRound() {
        _timerState.value = _timerState.value.copy(
            amrapRoundsCompleted = _timerState.value.amrapRoundsCompleted + 1
        )
    }

    fun stopConditioning() {
        conditioningJob?.cancel()
        _timerState.value = _timerState.value.copy(
            conditioningTick = null,
            isConditioningRunning = false,
            amrapRoundsCompleted = 0
        )
    }

    // ==================== PROGRAM, PROFILE, UNITS & ICE ====================

    fun selectProgram(programId: String) {
        val meta = LockoutSeedData.programs.firstOrNull { it.id == programId } ?: return
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(
                currentPrefs.copy(
                    selectedProgramId = meta.id,
                    selectedDayTitle = meta.days.first()
                )
            )
        }
    }

    fun selectProgramDay(dayTitle: String) {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(selectedDayTitle = dayTitle))
        }
    }

    fun selectGymProfile(profile: GymProfile) {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(gymProfile = profile))
            _snackbarEvents.tryEmit("Gym Profile switched to ${profile.label}")
        }
    }

    fun toggleWeightUnit() {
        val currentPrefs = uiState.value.prefs
        val nextLbs = !currentPrefs.useLbs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(useLbs = nextLbs))
            _snackbarEvents.tryEmit(if (nextLbs) "Units switched to Pounds (LBS)" else "Units switched to Kilograms (KG)")
        }
    }

    fun toggleDaylightMode() {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(isDaylightMode = !currentPrefs.isDaylightMode))
        }
    }

    fun setBarbellWeight(weightKg: Float) {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(barbellWeightKg = weightKg))
        }
    }

    fun setPlannedWorkoutHour(hour: Int) {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(plannedWorkoutHour = hour.coerceIn(5, 22)))
        }
    }

    fun toggleJointFlag(jointArea: String) {
        val currentPrefs = uiState.value.prefs
        val set = uiState.value.flaggedJoints.toMutableSet()
        if (jointArea in set) set.remove(jointArea) else set.add(jointArea)
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(flaggedJointsCsv = set.joinToString(",")))
        }
    }

    fun updateIceCard(contactName: String, contactPhone: String, bloodType: String, allergies: String) {
        val currentPrefs = uiState.value.prefs
        viewModelScope.launch {
            repository.upsertAppPrefs(
                currentPrefs.copy(
                    iceContactName = contactName.trim(),
                    iceContactPhone = contactPhone.trim(),
                    iceBloodType = bloodType.trim().ifEmpty { "O+" },
                    iceAllergies = allergies.trim().ifEmpty { "None" }
                )
            )
            _snackbarEvents.tryEmit("Emergency ICE Safety Card saved offline")
        }
    }

    fun updateAthleteProfile(isMale: Boolean, age: Int, bodyWeightKg: Float) {
        val currentPrefs = uiState.value.prefs
        val currentNut = uiState.value.nutrition
        viewModelScope.launch {
            repository.upsertAppPrefs(currentPrefs.copy(isMale = isMale, athleteAge = age.coerceIn(15, 80)))
            val plan = LockoutDomainEngine.calculateCaloriePlan(
                weightKg = bodyWeightKg.coerceIn(40f, 200f),
                heightCm = currentPrefs.athleteHeightCm,
                ageYears = age.coerceIn(15, 80),
                isMale = isMale,
                activityLevel = currentPrefs.activityLevel,
                goal = currentNut.goal
            )
            repository.upsertNutrition(
                currentNut.copy(
                    bodyWeightKg = bodyWeightKg.coerceIn(40f, 200f),
                    caloriesTarget = if (currentNut.isTrainingDay) plan.trainingDayCalories else plan.restDayCalories,
                    proteinTarget = plan.proteinGrams,
                    carbsTarget = plan.carbsGrams,
                    fatsTarget = plan.fatsGrams,
                    waterTargetMl = plan.waterTargetMl
                )
            )
        }
    }

    fun applyCalorieCalculatorPlan(
        weightKg: Float,
        heightCm: Int,
        ageYears: Int,
        isMale: Boolean,
        activityLevel: ActivityLevel,
        goal: NutritionGoal
    ) {
        val currentPrefs = uiState.value.prefs
        val currentNut = uiState.value.nutrition
        val plan = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            isMale = isMale,
            activityLevel = activityLevel,
            goal = goal
        )
        viewModelScope.launch {
            repository.upsertAppPrefs(
                currentPrefs.copy(
                    athleteHeightCm = heightCm.coerceIn(130, 230),
                    athleteAge = ageYears.coerceIn(14, 90),
                    isMale = isMale,
                    activityLevel = activityLevel
                )
            )
            repository.upsertNutrition(
                currentNut.copy(
                    bodyWeightKg = weightKg.coerceIn(35f, 220f),
                    goal = goal,
                    caloriesTarget = if (currentNut.isTrainingDay) plan.trainingDayCalories else plan.restDayCalories,
                    proteinTarget = plan.proteinGrams,
                    carbsTarget = plan.carbsGrams,
                    fatsTarget = plan.fatsGrams,
                    waterTargetMl = plan.waterTargetMl
                )
            )
            _snackbarEvents.tryEmit(
                "Applied ${plan.targetCalories} kcal target (${plan.proteinGrams}g P / ${plan.carbsGrams}g C / ${plan.fatsGrams}g F)"
            )
        }
    }

    fun buildCsvExport(): String = LockoutDomainEngine.exportWorkoutCsv(uiState.value.allExercises)

    fun buildJsonExport(): String = LockoutDomainEngine.exportFullJson(
        exercises = uiState.value.allExercises,
        supplements = uiState.value.supplements.map { it.entity },
        nutrition = uiState.value.nutrition
    )

    // ==================== SUPPLEMENTS & DOSE ALARMS ====================

    fun toggleSupplement(item: SupplementDisplayItem) {
        viewModelScope.launch {
            val willBeTaken = !item.isTakenToday
            repository.toggleSupplementForToday(item.entity, item.isTakenToday)
            if (willBeTaken) {
                alarmScheduler.playDoseTakenChime()
                val nextDays = item.totalDaysTaken + 1
                val alarmSuffix = if (item.entity.reminderEnabled) {
                    " • Next alarm ${item.reminderTimeLabel}"
                } else ""
                _snackbarEvents.tryEmit("${item.entity.name} logged ($nextDays ${if (nextDays == 1) "day" else "days"} tracked)$alarmSuffix")
            }
        }
    }

    fun updateSupplementAlarm(
        item: SupplementDisplayItem,
        enabled: Boolean,
        hour: Int,
        minute: Int
    ) {
        viewModelScope.launch {
            val updated = repository.updateSupplementReminder(item.entity, enabled, hour, minute)
            if (enabled) {
                alarmScheduler.scheduleDailyReminder(updated)
                val formatted = DateUtils.formatTime12h(updated.reminderHour, updated.reminderMinute)
                _snackbarEvents.tryEmit("Daily dose alarm armed for ${updated.name} at $formatted")
            } else {
                alarmScheduler.cancelReminder(updated.id)
                _snackbarEvents.tryEmit("Dose alarm turned off for ${updated.name}")
            }
        }
    }

    fun testSupplementAlarm(item: SupplementDisplayItem) {
        alarmScheduler.triggerImmediateTestAlarm(item.entity)
        _snackbarEvents.tryEmit("Test alarm triggered for ${item.entity.name} (${item.entity.dosage})")
    }

    fun restockSupplement(item: SupplementDisplayItem) {
        viewModelScope.launch {
            repository.restockSupplement(item.entity, 30)
            _snackbarEvents.tryEmit("Restocked ${item.entity.name} (+30 servings)")
        }
    }

    fun addCustomSupplement(name: String, dosage: String, timing: SupplementTiming, servings: Int) {
        viewModelScope.launch {
            val upper = name.uppercase()
            val key = when {
                "CREATINE" in upper -> "CREATINE"
                "CAFFEINE" in upper || "PRE" in upper -> "CAFFEINE"
                "ZINC" in upper -> "ZINC"
                "CALCIUM" in upper -> "CALCIUM"
                "IRON" in upper -> "IRON"
                "D3" in upper || "VITAMIN D" in upper -> "VIT_D3"
                "OMEGA" in upper -> "OMEGA3"
                "MAGNESIUM" in upper -> "MAGNESIUM"
                else -> ""
            }
            repository.upsertSupplement(
                SupplementEntity(
                    name = name.trim(),
                    dosage = dosage.trim().ifEmpty { "1 serving" },
                    timing = timing,
                    servingsLeft = servings.coerceIn(1, 365),
                    tier = "Custom",
                    cycleNote = "Daily Stack",
                    interactionKey = key,
                    reminderEnabled = false,
                    reminderHour = timing.defaultAlarmHour,
                    reminderMinute = timing.defaultAlarmMinute
                )
            )
            _snackbarEvents.tryEmit("Added ${name.trim()} to ${timing.label}")
        }
    }

    fun importVaultSupplementToStack(vaultItem: VaultItemEntity) {
        viewModelScope.launch {
            val upper = vaultItem.name.uppercase()
            val key = when {
                "CREATINE" in upper -> "CREATINE"
                "CAFFEINE" in upper -> "CAFFEINE"
                "D3" in upper -> "VIT_D3"
                "OMEGA" in upper -> "OMEGA3"
                "MAGNESIUM" in upper -> "MAGNESIUM"
                else -> ""
            }
            repository.upsertSupplement(
                SupplementEntity(
                    name = vaultItem.name,
                    dosage = vaultItem.defaultDosage.ifEmpty { "1 serving" },
                    timing = vaultItem.defaultTiming,
                    servingsLeft = 30,
                    tier = vaultItem.subTag.substringBefore(" •"),
                    cycleNote = "From Vault",
                    interactionKey = key,
                    reminderEnabled = false,
                    reminderHour = vaultItem.defaultTiming.defaultAlarmHour,
                    reminderMinute = vaultItem.defaultTiming.defaultAlarmMinute
                )
            )
            _snackbarEvents.tryEmit("Added ${vaultItem.name} to your Daily Stack")
        }
    }

    fun importGoalStack(presetId: String, presetLabel: String) {
        viewModelScope.launch {
            repository.replaceWithGoalStack(presetId)
            _snackbarEvents.tryEmit("Loaded $presetLabel")
        }
    }

    // ==================== HEALTHY BODY HABITS ====================

    fun toggleHabit(item: HabitDisplayItem) {
        viewModelScope.launch {
            val nextDone = !item.isCompletedToday
            repository.toggleHabitForToday(item.entity.id, item.isCompletedToday)
            if (nextDone) {
                val nextTotal = item.totalDaysCompleted + 1
                _snackbarEvents.tryEmit("${item.entity.title} checked ($nextTotal ${if (nextTotal == 1) "day" else "days"} logged)")
            }
        }
    }

    fun addCustomHabit(title: String, category: String, targetLabel: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.upsertHabit(
                DailyHabitEntity(
                    title = title.trim(),
                    category = category.trim().ifEmpty { "Health" },
                    targetLabel = targetLabel.trim().ifEmpty { "Daily body habit" }
                )
            )
            _snackbarEvents.tryEmit("Added habit: ${title.trim()}")
        }
    }

    // ==================== NUTRITION & HYDRATION ====================

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val current = uiState.value.nutrition
            repository.upsertNutrition(current.copy(waterMl = current.waterMl + amountMl))
        }
    }

    fun logQuickMeal(name: String, proteinGrams: Int, carbsGrams: Int, fatsGrams: Int, calories: Int) {
        viewModelScope.launch {
            val current = uiState.value.nutrition
            repository.upsertNutrition(
                current.copy(
                    caloriesConsumed = current.caloriesConsumed + calories,
                    proteinGrams = current.proteinGrams + proteinGrams,
                    carbsGrams = current.carbsGrams + carbsGrams,
                    fatsGrams = current.fatsGrams + fatsGrams
                )
            )
            _snackbarEvents.tryEmit("Logged $name (+${proteinGrams}g P, +${calories} kcal)")
        }
    }

    fun setNutritionGoal(goal: NutritionGoal) {
        val prefs = uiState.value.prefs
        val current = uiState.value.nutrition
        val plan = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = current.bodyWeightKg,
            heightCm = prefs.athleteHeightCm,
            ageYears = prefs.athleteAge,
            isMale = prefs.isMale,
            activityLevel = prefs.activityLevel,
            goal = goal
        )
        viewModelScope.launch {
            repository.upsertNutrition(
                current.copy(
                    goal = goal,
                    caloriesTarget = if (current.isTrainingDay) plan.trainingDayCalories else plan.restDayCalories,
                    proteinTarget = plan.proteinGrams,
                    carbsTarget = plan.carbsGrams,
                    fatsTarget = plan.fatsGrams,
                    waterTargetMl = plan.waterTargetMl
                )
            )
        }
    }

    fun toggleTrainingDayCarbCycle() {
        val prefs = uiState.value.prefs
        val current = uiState.value.nutrition
        val nextTrainingDay = !current.isTrainingDay
        val plan = LockoutDomainEngine.calculateCaloriePlan(
            weightKg = current.bodyWeightKg,
            heightCm = prefs.athleteHeightCm,
            ageYears = prefs.athleteAge,
            isMale = prefs.isMale,
            activityLevel = prefs.activityLevel,
            goal = current.goal
        )
        val newCalories = if (nextTrainingDay) plan.trainingDayCalories else plan.restDayCalories
        val remaining = (newCalories - (plan.proteinGrams * 4) - (plan.fatsGrams * 9)).coerceAtLeast(160)
        viewModelScope.launch {
            repository.upsertNutrition(
                current.copy(
                    isTrainingDay = nextTrainingDay,
                    caloriesTarget = newCalories,
                    proteinTarget = plan.proteinGrams,
                    carbsTarget = remaining / 4,
                    fatsTarget = plan.fatsGrams
                )
            )
        }
    }

    // ==================== VAULT & SKILL TREE ====================

    fun toggleGearBagItem(item: VaultItemEntity) {
        viewModelScope.launch {
            val nextOwned = !item.isInMyGearBag
            repository.setGearBagStatus(item.id, nextOwned)
            val status = if (nextOwned) "Packed in My Gear Bag" else "Removed from Gear Bag"
            _snackbarEvents.tryEmit("${item.name}: $status")
        }
    }

    fun toggleSkillMilestone(milestone: SkillMilestoneEntity) {
        viewModelScope.launch {
            repository.toggleSkillMilestone(milestone.id, !milestone.isUnlocked)
        }
    }

    private data class CoreBundle(
        val exercises: List<ExerciseEntity>,
        val supplements: List<SupplementEntity>,
        val todayTakenIds: Set<Int>,
        val allSupplementLogs: List<SupplementLogEntity>,
        val nutrition: DailyNutritionEntity
    )

    private data class HabitsBundle(
        val habits: List<DailyHabitEntity>,
        val todayHabitIds: Set<Int>,
        val allHabitLogs: List<HabitLogEntity>
    )

    private data class MetaBundle(
        val vaultItems: List<VaultItemEntity>,
        val skills: List<SkillMilestoneEntity>,
        val prefs: AppPrefsEntity,
        val timer: TimerOverlayState,
        val coachingDirective: String?
    )
}

class LockoutViewModelFactory(
    private val repository: LockoutRepository,
    private val timerEngine: RestTimerEngine,
    private val alarmScheduler: SupplementAlarmScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LockoutViewModel::class.java)) {
            return LockoutViewModel(repository, timerEngine, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
