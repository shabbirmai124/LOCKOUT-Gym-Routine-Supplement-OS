package com.example.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppPrefsEntity
import com.example.data.DailyNutritionEntity
import com.example.data.DateUtils
import com.example.data.ExerciseEntity
import com.example.data.LockoutRepository
import com.example.data.LockoutSeedData
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.data.SetType
import com.example.data.SkillMilestoneEntity
import com.example.data.SupplementEntity
import com.example.data.SupplementTiming
import com.example.data.VaultItemEntity
import com.example.domain.ClinicalGuardrail
import com.example.domain.LockoutDomainEngine
import com.example.domain.RecoveryStatus
import com.example.domain.SwapAlternative
import com.example.engine.HoldTimerTick
import com.example.engine.RestTimerEngine
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
    val isLowStock: Boolean
)

@Immutable
data class TimerOverlayState(
    val restSecondsRemaining: Int = 0,
    val restTotalSeconds: Int = 0,
    val restExerciseName: String = "",
    val isRestRunning: Boolean = false,
    val holdExerciseName: String = "",
    val holdTick: HoldTimerTick? = null,
    val isHoldRunning: Boolean = false
)

@Immutable
data class LockoutUiState(
    val isLoaded: Boolean = false,
    val prefs: AppPrefsEntity = AppPrefsEntity(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val activeDayExercises: List<ExerciseEntity> = emptyList(),
    val supplements: List<SupplementDisplayItem> = emptyList(),
    val nutrition: DailyNutritionEntity = DailyNutritionEntity(),
    val effectiveWaterTargetMl: Int = 3500,
    val vaultItems: List<VaultItemEntity> = emptyList(),
    val ownedGearNames: Set<String> = emptySet(),
    val skillMilestones: List<SkillMilestoneEntity> = emptyList(),
    val clinicalGuardrails: List<ClinicalGuardrail> = emptyList(),
    val muscleRecovery: Map<MuscleGroup, RecoveryStatus> = emptyMap(),
    val readinessScore: Int = 82,
    val creatineStreakDays: Int = 1,
    val flaggedJoints: Set<String> = emptySet(),
    val timerState: TimerOverlayState = TimerOverlayState()
)

class LockoutViewModel(
    private val repository: LockoutRepository,
    private val timerEngine: RestTimerEngine
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerOverlayState())
    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var restTimerJob: Job? = null
    private var holdTimerJob: Job? = null

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
        CoreBundle(exercises, supplements, todayLogs.map { it.supplementId }.toSet(), allLogs.size, nutrition)
    }

    private val metaDataFlow = combine(
        repository.allVaultItems,
        repository.allSkillMilestones,
        repository.appPrefs,
        _timerState
    ) { vaultItems, skills, prefs, timer ->
        MetaBundle(vaultItems, skills, prefs, timer)
    }

    val uiState: StateFlow<LockoutUiState> = combine(coreDataFlow, metaDataFlow) { core, meta ->
        val prefs = meta.prefs
        val activeDayExercises = core.exercises.filter {
            it.programId == prefs.selectedProgramId && it.dayTitle == prefs.selectedDayTitle
        }

        val suppItems = core.supplements.map { sup ->
            SupplementDisplayItem(
                entity = sup,
                isTakenToday = sup.id in core.todayTakenIds,
                runOutDateLabel = DateUtils.addDaysDisplay(sup.servingsLeft),
                isLowStock = sup.servingsLeft <= 7
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
        val recoveryMap = LockoutDomainEngine.calculateMuscleRecoveryMap(core.exercises)
        val takenCount = suppItems.count { it.isTakenToday }
        val readiness = LockoutDomainEngine.calculateReadinessScore(
            nutrition = core.nutrition,
            effectiveWaterTargetMl = effectiveWaterTarget,
            supplementsTaken = takenCount,
            totalSupplements = suppItems.size,
            recoveryMap = recoveryMap
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

        val creatineTakenToday = suppItems.any {
            it.isTakenToday && (it.entity.interactionKey == "CREATINE" || it.entity.name.contains("Creatine", true))
        }

        LockoutUiState(
            isLoaded = true,
            prefs = prefs,
            allExercises = core.exercises,
            activeDayExercises = activeDayExercises,
            supplements = suppItems,
            nutrition = core.nutrition,
            effectiveWaterTargetMl = effectiveWaterTarget,
            vaultItems = meta.vaultItems,
            ownedGearNames = ownedGear,
            skillMilestones = meta.skills,
            clinicalGuardrails = guardrails,
            muscleRecovery = recoveryMap,
            readinessScore = readiness,
            creatineStreakDays = (if (creatineTakenToday) 7 else 6) + (core.totalLogEntries / 12),
            flaggedJoints = flaggedJoints,
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
            _snackbarEvents.tryEmit(decision.summaryMessage)
        }
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
            _snackbarEvents.tryEmit("Swapped to ${alternative.name} (${LockoutDomainEngine.formatKg(alternative.convertedWeightKg)})")
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

    // ==================== PROGRAM & PREFS ====================

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

    // ==================== SUPPLEMENTS ====================

    fun toggleSupplement(item: SupplementDisplayItem) {
        viewModelScope.launch {
            repository.toggleSupplementForToday(item.entity, item.isTakenToday)
        }
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
                    interactionKey = key
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
                    interactionKey = key
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
            _snackbarEvents.tryEmit("Logged $name (+${proteinGrams}g protein, +${calories} kcal)")
        }
    }

    fun setNutritionGoal(goal: NutritionGoal) {
        viewModelScope.launch {
            val updated = LockoutDomainEngine.recalculateMacroTargets(
                current = uiState.value.nutrition,
                goal = goal,
                isTrainingDay = uiState.value.nutrition.isTrainingDay
            )
            repository.upsertNutrition(updated)
        }
    }

    fun toggleTrainingDayCarbCycle() {
        viewModelScope.launch {
            val current = uiState.value.nutrition
            val updated = LockoutDomainEngine.recalculateMacroTargets(
                current = current,
                goal = current.goal,
                isTrainingDay = !current.isTrainingDay
            )
            repository.upsertNutrition(updated)
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
        val totalLogEntries: Int,
        val nutrition: DailyNutritionEntity
    )

    private data class MetaBundle(
        val vaultItems: List<VaultItemEntity>,
        val skills: List<SkillMilestoneEntity>,
        val prefs: AppPrefsEntity,
        val timer: TimerOverlayState
    )
}

class LockoutViewModelFactory(
    private val repository: LockoutRepository,
    private val timerEngine: RestTimerEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LockoutViewModel::class.java)) {
            return LockoutViewModel(repository, timerEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
