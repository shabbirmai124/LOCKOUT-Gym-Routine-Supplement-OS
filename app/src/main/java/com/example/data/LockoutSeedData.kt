package com.example.data

data class ProgramMeta(
    val id: String,
    val title: String,
    val subtitle: String,
    val daysPerWeek: String,
    val days: List<String>
)

object LockoutSeedData {

    val programs: List<ProgramMeta> = listOf(
        ProgramMeta(
            id = "ppl_6day",
            title = "6-Day Push / Pull / Legs",
            subtitle = "Hypertrophy & Aesthetics Flagship",
            daysPerWeek = "6 DAYS",
            days = listOf("Push Day", "Pull Day", "Leg Day")
        ),
        ProgramMeta(
            id = "upper_lower_4day",
            title = "4-Day Upper / Lower",
            subtitle = "Power-Hypertrophy Split",
            daysPerWeek = "4 DAYS",
            days = listOf("Upper Power", "Lower Power", "Upper Hypertrophy", "Lower Hypertrophy")
        ),
        ProgramMeta(
            id = "hybrid_5day",
            title = "5-Day Hybrid Iron + Gymnastics",
            subtitle = "Strength & Elite Bodyweight Skills",
            daysPerWeek = "5 DAYS",
            days = listOf("Skill & Push", "Skill & Pull", "Lower Explosive", "Upper Rings", "Conditioning")
        ),
        ProgramMeta(
            id = "fullbody_3day",
            title = "3-Day Full Body Foundation",
            subtitle = "Linear Progression (+2.5 kg)",
            daysPerWeek = "3 DAYS",
            days = listOf("Workout A", "Workout B")
        )
    )

    fun seedExercises(): List<ExerciseEntity> = listOf(
        // ==========================================================
        // PROGRAM A: 6-Day Push / Pull / Legs (Flagship)
        // ==========================================================
        // Push Day
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Incline Barbell Press", category = "Chest • Compound",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 4, targetRepsOrSec = "6-8 reps",
            weightKg = 80f, previousWeightKg = 80f, targetRpe = 8.5f, restSeconds = 150,
            isCompoundBarbell = true, recommendedGear = "Wrist Wraps",
            coachingCue = "Tuck elbows 45°, touch upper chest, drive explosively.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Flat Machine Chest Press", category = "Chest • Machine",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 3, targetRepsOrSec = "8-10 reps",
            weightKg = 75f, previousWeightKg = 75f, targetRpe = 9.0f, restSeconds = 120,
            coachingCue = "Pin scapulae back, control 3-second eccentric.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Seated Dumbbell Overhead Press", category = "Delts • Compound",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 3, targetRepsOrSec = "8-10 reps",
            weightKg = 28f, previousWeightKg = 28f, targetRpe = 8.5f, restSeconds = 120,
            recommendedGear = "Wrist Wraps",
            coachingCue = "Slight scapular plane (30° forward), brace ribcage down.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Cable Lateral Raises (Behind Back)", category = "Side Delts",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 4, targetRepsOrSec = "12-15 reps",
            weightKg = 10f, previousWeightKg = 10f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "Sweep out wide rather than shrugging traps up."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Pec Deck / Cable Chest Fly", category = "Chest • Isolation",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 3, targetRepsOrSec = "12-15 reps",
            weightKg = 55f, previousWeightKg = 55f, targetRpe = 9.5f, restSeconds = 75,
            coachingCue = "Soft 15° elbow bend; squeeze biceps together across sternum."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Overhead Cable Tricep Extension", category = "Triceps • Long Head",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "10-12 reps",
            weightKg = 27.5f, previousWeightKg = 27.5f, targetRpe = 9.0f, restSeconds = 75,
            coachingCue = "Full stretch behind head, lock elbows cleanly."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Push Day",
            name = "Tricep Rope Pushdown", category = "Triceps",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "12-15 reps",
            weightKg = 25f, previousWeightKg = 25f, targetRpe = 9.5f, restSeconds = 60,
            setType = SetType.DROP,
            coachingCue = "Keep upper arms glued to ribs, spread rope at lockout."
        ),

        // Pull Day
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Weighted Pull-Ups", category = "Lats • Compound",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "6-10 reps",
            weightKg = 15f, previousWeightKg = 15f, targetRpe = 8.5f, restSeconds = 150,
            recommendedGear = "Liquid Chalk, Gymnastics Grips",
            coachingCue = "Drive elbows down to pockets, full dead-hang stretch."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Chest-Supported T-Bar Row", category = "Upper Back",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "8-10 reps",
            weightKg = 60f, previousWeightKg = 60f, targetRpe = 9.0f, restSeconds = 120,
            recommendedGear = "Lifting Straps",
            coachingCue = "Let shoulders protract at bottom, pause 1s at peak squeeze."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Single-Arm Cable Lat Pulldown", category = "Lats • Unilateral",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "10-12 reps",
            weightKg = 35f, previousWeightKg = 35f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Slight 15° lean, drive elbow tight against hip."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Reverse Pec Deck / Face Pulls", category = "Rear Delts",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 4, targetRepsOrSec = "15-20 reps",
            weightKg = 30f, previousWeightKg = 30f, targetRpe = 9.0f, restSeconds = 60,
            coachingCue = "Sweep outward with relaxed traps; external rotation at peak."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Incline Dumbbell Curl", category = "Biceps • Stretch",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "10-12 reps",
            weightKg = 14f, previousWeightKg = 14f, targetRpe = 9.5f, restSeconds = 75,
            coachingCue = "Keep elbows anchored behind torso; zero shoulder swing."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Pull Day",
            name = "Hammer Curls (Cable or DB)", category = "Brachialis",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "10-12 reps",
            weightKg = 16f, previousWeightKg = 16f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "Neutral wrist alignment, slow 2-second negative."
        ),

        // Leg Day
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Barbell Back Squat", category = "Quads • Compound",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 4, targetRepsOrSec = "6-8 reps",
            weightKg = 112.5f, previousWeightKg = 110f, targetRpe = 8.5f, restSeconds = 180,
            isCompoundBarbell = true, recommendedGear = "Lifting Belt, Knee Sleeves",
            coachingCue = "360° Valsalva brace into belt, break parallel cleanly.",
            jointRiskArea = "Knees,Lower Back"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Romanian Deadlift (RDL)", category = "Hamstrings • Hinge",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 4, targetRepsOrSec = "8-10 reps",
            weightKg = 100f, previousWeightKg = 100f, targetRpe = 8.5f, restSeconds = 150,
            isCompoundBarbell = true, recommendedGear = "Lifting Straps, Lifting Belt",
            coachingCue = "Push hips straight back, keep bar shaved against thighs.",
            jointRiskArea = "Lower Back"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Bulgarian Split Squat", category = "Quads & Glutes",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "10 / leg",
            weightKg = 22f, previousWeightKg = 22f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Slight forward torso lean, drive through mid-foot.",
            jointRiskArea = "Knees"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Seated Leg Curl", category = "Hamstrings",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 3, targetRepsOrSec = "10-12 reps",
            weightKg = 55f, previousWeightKg = 55f, targetRpe = 9.5f, restSeconds = 75,
            coachingCue = "Align knee joint directly with red machine pivot dot."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Leg Extension", category = "Quads • Isolation",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "12-15 reps",
            weightKg = 65f, previousWeightKg = 65f, targetRpe = 9.5f, restSeconds = 75,
            coachingCue = "Pause 1s at peak lockout; control descent smoothly.",
            jointRiskArea = "Knees"
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Standing Calf Raise (Full Pause)", category = "Calves",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 4, targetRepsOrSec = "12-15 reps",
            weightKg = 80f, previousWeightKg = 80f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "2-second dead pause at bottom stretch to kill Achilles bounce."
        ),
        ExerciseEntity(
            programId = "ppl_6day", dayTitle = "Leg Day",
            name = "Hanging Leg Raises", category = "Core • Lower Abs",
            primaryMuscle = MuscleGroup.CORE, targetSets = 3, targetRepsOrSec = "12-15 reps",
            weightKg = 0f, previousWeightKg = 0f, targetRpe = 9.0f, restSeconds = 60,
            recommendedGear = "Lifting Straps",
            coachingCue = "Curl pelvis upward toward ribs; zero momentum swing."
        ),

        // ==========================================================
        // PROGRAM B: 4-Day Upper / Lower Power-Hypertrophy
        // ==========================================================
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Flat Barbell Bench Press", category = "Chest • Power",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 4, targetRepsOrSec = "5 reps",
            weightKg = 95f, previousWeightKg = 92.5f, targetRpe = 8.5f, restSeconds = 180,
            isCompoundBarbell = true, recommendedGear = "Wrist Wraps",
            coachingCue = "Retract shoulder blades, drive feet into floor, crisp touch.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Weighted Pull-Ups", category = "Back • Power",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "6 reps",
            weightKg = 20f, previousWeightKg = 17.5f, targetRpe = 8.5f, restSeconds = 150,
            recommendedGear = "Liquid Chalk",
            coachingCue = "Chest toward bar, control full eccentric."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Overhead Barbell Press", category = "Delts • Power",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 3, targetRepsOrSec = "6 reps",
            weightKg = 60f, previousWeightKg = 60f, targetRpe = 8.5f, restSeconds = 150,
            isCompoundBarbell = true, recommendedGear = "Lifting Belt, Wrist Wraps",
            coachingCue = "Squeeze glutes, clear chin, stack bar over mid-foot.",
            jointRiskArea = "Shoulders,Lower Back"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Pendlay Row", category = "Back • Explosive",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "6 reps",
            weightKg = 80f, previousWeightKg = 80f, targetRpe = 8.5f, restSeconds = 120,
            isCompoundBarbell = true, recommendedGear = "Lifting Straps",
            coachingCue = "Torso parallel to floor, pull dead-stop to sternum.",
            jointRiskArea = "Lower Back"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Weighted Dips", category = "Chest & Triceps",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 20f, previousWeightKg = 20f, targetRpe = 8.5f, restSeconds = 120,
            coachingCue = "Slight forward lean, descend to 90° elbow flexion.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Power",
            name = "Barbell Curl", category = "Biceps",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 40f, previousWeightKg = 40f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Strict torso, squeeze hard at top."
        ),

        // Lower Power
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Power",
            name = "Barbell Back Squat", category = "Quads • Power",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 4, targetRepsOrSec = "5 reps",
            weightKg = 120f, previousWeightKg = 117.5f, targetRpe = 8.5f, restSeconds = 180,
            isCompoundBarbell = true, recommendedGear = "Lifting Belt, Knee Sleeves",
            coachingCue = "Brace hard into belt, drive knees out over toes.",
            jointRiskArea = "Knees,Lower Back"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Power",
            name = "Conventional Deadlift", category = "Posterior • Power",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 3, targetRepsOrSec = "5 reps",
            weightKg = 140f, previousWeightKg = 135f, targetRpe = 8.5f, restSeconds = 180,
            isCompoundBarbell = true, recommendedGear = "Lifting Belt, Lifting Straps, Liquid Chalk",
            coachingCue = "Pull slack out of bar first, push floor away with legs.",
            jointRiskArea = "Lower Back"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Power",
            name = "45-Degree Leg Press", category = "Quads",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 180f, previousWeightKg = 180f, targetRpe = 9.0f, restSeconds = 120,
            recommendedGear = "Knee Sleeves",
            coachingCue = "Never peel lower back off pad; never hyper-lock knees.",
            jointRiskArea = "Knees"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Power",
            name = "Lying Leg Curl", category = "Hamstrings",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 50f, previousWeightKg = 50f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Press hips down into pad throughout curl."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Power",
            name = "Weighted Cable Crunch", category = "Core",
            primaryMuscle = MuscleGroup.CORE, targetSets = 3, targetRepsOrSec = "12 reps",
            weightKg = 35f, previousWeightKg = 35f, targetRpe = 9.0f, restSeconds = 60,
            coachingCue = "Round thoracic spine downward; keep hips still."
        ),

        // Upper Hypertrophy
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Hypertrophy",
            name = "Incline Dumbbell Press", category = "Chest",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 4, targetRepsOrSec = "10 reps",
            weightKg = 34f, previousWeightKg = 34f, targetRpe = 9.0f, restSeconds = 120,
            coachingCue = "Deep stretch at bottom, drive dumbbells inward."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Hypertrophy",
            name = "Seated Cable Row", category = "Back",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "10 reps",
            weightKg = 65f, previousWeightKg = 65f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Tall chest, pull cleanly toward lower ribs."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Hypertrophy",
            name = "Lat Pulldown", category = "Lats",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "12 reps",
            weightKg = 60f, previousWeightKg = 60f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Lock thigh pad tight, slight 15° lean."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Hypertrophy",
            name = "Lateral Raises", category = "Side Delts",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 4, targetRepsOrSec = "15 reps",
            weightKg = 12f, previousWeightKg = 12f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "Lead with elbows, control descent."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Upper Hypertrophy",
            name = "Preacher Curl", category = "Biceps",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "12 reps",
            weightKg = 30f, previousWeightKg = 30f, targetRpe = 9.5f, restSeconds = 75,
            coachingCue = "Slow stretch at bottom; keep triceps pressed on pad."
        ),

        // Lower Hypertrophy
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Hypertrophy",
            name = "Hack Squat", category = "Quads",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 4, targetRepsOrSec = "10 reps",
            weightKg = 100f, previousWeightKg = 100f, targetRpe = 9.0f, restSeconds = 120,
            recommendedGear = "Knee Sleeves",
            coachingCue = "Keep heels glued to sled; descend until hamstrings cover calves.",
            jointRiskArea = "Knees"
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Hypertrophy",
            name = "Dumbbell RDL", category = "Hamstrings",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 4, targetRepsOrSec = "10 reps",
            weightKg = 32f, previousWeightKg = 32f, targetRpe = 9.0f, restSeconds = 120,
            coachingCue = "Soft knees, hinge until maximal hamstring tension."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Hypertrophy",
            name = "Walking Lunges", category = "Quads & Glutes",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "12 / leg",
            weightKg = 20f, previousWeightKg = 20f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Long controlled stride, knee kisses floor gently."
        ),
        ExerciseEntity(
            programId = "upper_lower_4day", dayTitle = "Lower Hypertrophy",
            name = "Leg Extension", category = "Quads",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "15 reps",
            weightKg = 60f, previousWeightKg = 60f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "Full peak squeeze every rep."
        ),

        // ==========================================================
        // PROGRAM C: 5-Day Hybrid Iron + Gymnastics / Calisthenics
        // ==========================================================
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Push",
            name = "Freestanding Handstand Hold", category = "Gymnastics • Skill",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 5, targetRepsOrSec = "45s hold",
            isIsometric = true, holdTargetSec = 45, weightKg = 0f, targetRpe = 8.0f, restSeconds = 90,
            recommendedGear = "Wrist Wraps",
            coachingCue = "Push tall through shoulders, stack hips, balance via fingertips."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Push",
            name = "Ring Muscle-Up Drills", category = "Gymnastics • Rings",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "4 reps",
            weightKg = 0f, targetRpe = 8.5f, restSeconds = 150,
            recommendedGear = "Liquid Chalk, Gymnastics Grips",
            coachingCue = "Deep false grip, pull rings to sternum, fast transition."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Push",
            name = "Weighted Ring Dips", category = "Strength • Rings",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 4, targetRepsOrSec = "8 reps",
            weightKg = 20f, previousWeightKg = 20f, targetRpe = 8.5f, restSeconds = 120,
            coachingCue = "Turn rings out (RTO) at top lockout, stabilize straps.",
            jointRiskArea = "Shoulders"
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Push",
            name = "Planche Lean Holds", category = "Gymnastics • Isometric",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 4, targetRepsOrSec = "20s hold",
            isIsometric = true, holdTargetSec = 20, weightKg = 0f, targetRpe = 8.5f, restSeconds = 90,
            recommendedGear = "Wrist Wraps",
            coachingCue = "Protract scapulae hard, lock elbows, lean shoulders past wrists."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Push",
            name = "Cable Lateral Raises", category = "Side Delts",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 4, targetRepsOrSec = "15 reps",
            weightKg = 10f, previousWeightKg = 10f, targetRpe = 9.5f, restSeconds = 60,
            coachingCue = "Strict lateral sweep, pause at shoulder height."
        ),

        // Skill & Pull
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Pull",
            name = "Front Lever Tuck / Straddle Hold", category = "Gymnastics • Isometric",
            primaryMuscle = MuscleGroup.BACK, targetSets = 5, targetRepsOrSec = "20s hold",
            isIsometric = true, holdTargetSec = 20, weightKg = 0f, targetRpe = 8.5f, restSeconds = 120,
            recommendedGear = "Liquid Chalk",
            coachingCue = "Depress lats, straight arms, keep hips level with shoulders."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Pull",
            name = "Weighted Chin-Ups", category = "Back & Biceps",
            primaryMuscle = MuscleGroup.BACK, targetSets = 4, targetRepsOrSec = "6 reps",
            weightKg = 22.5f, previousWeightKg = 22.5f, targetRpe = 8.5f, restSeconds = 150,
            recommendedGear = "Liquid Chalk",
            coachingCue = "Full hang to chin well above bar."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Pull",
            name = "Chest-Supported Row", category = "Upper Back",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 60f, previousWeightKg = 60f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Squeeze rhomboids for 1 second at top."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Skill & Pull",
            name = "Ring Pelican Curls", category = "Biceps • Straight-Arm Prep",
            primaryMuscle = MuscleGroup.ARMS, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 0f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Extend elbows behind torso slowly to condition bicep tendons."
        ),

        // Lower Explosive
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Lower Explosive",
            name = "Barbell Back Squat", category = "Legs • Iron",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 4, targetRepsOrSec = "6 reps",
            weightKg = 105f, previousWeightKg = 105f, targetRpe = 8.5f, restSeconds = 180,
            isCompoundBarbell = true, recommendedGear = "Lifting Belt, Knee Sleeves",
            coachingCue = "Explosive concentric drive out of the hole.",
            jointRiskArea = "Knees,Lower Back"
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Lower Explosive",
            name = "Nordic Hamstring Curls", category = "Posterior • Eccentric",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 3, targetRepsOrSec = "6 reps",
            weightKg = 0f, targetRpe = 9.0f, restSeconds = 120,
            coachingCue = "Keep hips extended; fight gravity as slowly as possible."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Lower Explosive",
            name = "Dragon Flag Negatives", category = "Core • Elite",
            primaryMuscle = MuscleGroup.CORE, targetSets = 3, targetRepsOrSec = "6 reps",
            weightKg = 0f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Body rigid from shoulders to toes; lower in 5 seconds."
        ),

        // Upper Rings
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Upper Rings",
            name = "L-Sit to V-Sit on Parallettes", category = "Gymnastics • Core",
            primaryMuscle = MuscleGroup.CORE, targetSets = 4, targetRepsOrSec = "30s hold",
            isIsometric = true, holdTargetSec = 30, weightKg = 0f, targetRpe = 8.5f, restSeconds = 90,
            coachingCue = "Depress shoulders hard, lock knees, point toes."
        ),
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Upper Rings",
            name = "Ring Archer Push-Ups", category = "Chest • Rings",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 0f, targetRpe = 9.0f, restSeconds = 90,
            coachingCue = "Keep assisting arm locked straight as working arm presses."
        ),

        // Conditioning
        ExerciseEntity(
            programId = "hybrid_5day", dayTitle = "Conditioning",
            name = "Heavy Farmer's Carry", category = "Grip & Conditioning",
            primaryMuscle = MuscleGroup.CORE, targetSets = 4, targetRepsOrSec = "40s hold",
            isIsometric = true, holdTargetSec = 40, weightKg = 40f, previousWeightKg = 40f,
            targetRpe = 8.5f, restSeconds = 90, recommendedGear = "Liquid Chalk",
            coachingCue = "Tall posture, quick crisp steps, brace obliques."
        ),

        // ==========================================================
        // PROGRAM D: 3-Day Full Body Beginner Foundation
        // ==========================================================
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout A",
            name = "Barbell / Goblet Squat", category = "Foundation • Legs",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 60f, previousWeightKg = 57.5f, targetRpe = 8.0f, restSeconds = 120,
            isCompoundBarbell = true,
            coachingCue = "Chest proud, knees track toes, full controlled depth."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout A",
            name = "Flat Dumbbell Bench Press", category = "Foundation • Push",
            primaryMuscle = MuscleGroup.CHEST, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 22f, previousWeightKg = 20f, targetRpe = 8.0f, restSeconds = 90,
            coachingCue = "Elbows 45° angle, touch chest level, press smoothly."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout A",
            name = "Lat Pulldown", category = "Foundation • Pull",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 45f, previousWeightKg = 45f, targetRpe = 8.0f, restSeconds = 90,
            coachingCue = "Pull bar to upper collarbone, squeeze lats."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout A",
            name = "Dumbbell Lateral Raise", category = "Delts",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 3, targetRepsOrSec = "12 reps",
            weightKg = 8f, previousWeightKg = 8f, targetRpe = 8.5f, restSeconds = 60,
            coachingCue = "Raise to parallel with soft elbows."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout A",
            name = "RKC Forearm Plank", category = "Core • Isometric",
            primaryMuscle = MuscleGroup.CORE, targetSets = 3, targetRepsOrSec = "45s hold",
            isIsometric = true, holdTargetSec = 45, weightKg = 0f, targetRpe = 8.5f, restSeconds = 60,
            coachingCue = "Squeeze glutes and pull elbows toward toes for full tension."
        ),

        // Workout B
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout B",
            name = "Romanian Deadlift (RDL)", category = "Foundation • Hinge",
            primaryMuscle = MuscleGroup.HAMSTRINGS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 70f, previousWeightKg = 67.5f, targetRpe = 8.0f, restSeconds = 120,
            isCompoundBarbell = true,
            coachingCue = "Hinge hips back keeping flat neutral spine."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout B",
            name = "Seated Dumbbell Shoulder Press", category = "Foundation • Push",
            primaryMuscle = MuscleGroup.SHOULDERS, targetSets = 3, targetRepsOrSec = "8 reps",
            weightKg = 18f, previousWeightKg = 18f, targetRpe = 8.0f, restSeconds = 90,
            coachingCue = "Press straight overhead without arching lower back."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout B",
            name = "Seated Cable Row", category = "Foundation • Pull",
            primaryMuscle = MuscleGroup.BACK, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 50f, previousWeightKg = 50f, targetRpe = 8.0f, restSeconds = 90,
            coachingCue = "Pull handle to navel, keep chest upright."
        ),
        ExerciseEntity(
            programId = "fullbody_3day", dayTitle = "Workout B",
            name = "45-Degree Leg Press", category = "Legs",
            primaryMuscle = MuscleGroup.QUADS, targetSets = 3, targetRepsOrSec = "10 reps",
            weightKg = 120f, previousWeightKg = 120f, targetRpe = 8.5f, restSeconds = 90,
            coachingCue = "Controlled descent, keep lower back flat against pad."
        )
    )

    fun seedSupplements(): List<SupplementEntity> = listOf(
        SupplementEntity(
            name = "Vitamin D3 + K2", dosage = "5000 IU + 100 mcg",
            timing = SupplementTiming.MORNING, servingsLeft = 42, tier = "Tier B",
            cycleNote = "Daily Year-Round", interactionKey = "VIT_D3"
        ),
        SupplementEntity(
            name = "Omega-3 Fish Oil", dosage = "2500 mg EPA+DHA",
            timing = SupplementTiming.MORNING, servingsLeft = 28, tier = "Tier B",
            cycleNote = "Daily Joint Health", interactionKey = "OMEGA3"
        ),
        SupplementEntity(
            name = "L-Citrulline (Pure)", dosage = "6 g",
            timing = SupplementTiming.PRE_WORKOUT, servingsLeft = 16, tier = "Tier A",
            cycleNote = "Stim-Free Pump", interactionKey = "CITRULLINE"
        ),
        SupplementEntity(
            name = "Caffeine Anhydrous", dosage = "200 mg",
            timing = SupplementTiming.PRE_WORKOUT, servingsLeft = 22, tier = "Tier A",
            cycleNote = "Week 3/4 • Reset Wk 5", interactionKey = "CAFFEINE"
        ),
        SupplementEntity(
            name = "Electrolytes (Na + K)", dosage = "1000 mg Na / 200 mg K",
            timing = SupplementTiming.INTRA_WORKOUT, servingsLeft = 19, tier = "Tier A",
            cycleNote = "Heavy Sweat Sessions", interactionKey = "ELECTROLYTES"
        ),
        SupplementEntity(
            name = "Whey Protein Isolate", dosage = "30 g (1 scoop)",
            timing = SupplementTiming.POST_WORKOUT, servingsLeft = 6, tier = "Tier S",
            cycleNote = "Fast Amino Recovery", interactionKey = "WHEY"
        ),
        SupplementEntity(
            name = "Creatine Monohydrate", dosage = "5 g micronized",
            timing = SupplementTiming.POST_WORKOUT, servingsLeft = 55, tier = "Tier S",
            cycleNote = "Maintenance • Daily", interactionKey = "CREATINE"
        ),
        SupplementEntity(
            name = "Magnesium Glycinate", dosage = "400 mg elemental",
            timing = SupplementTiming.NIGHT, servingsLeft = 11, tier = "Tier B",
            cycleNote = "CNS & Deep Sleep", interactionKey = "MAGNESIUM"
        )
    )

    fun goalStackPreset(presetId: String): List<SupplementEntity> = when (presetId) {
        "budget" -> listOf(
            SupplementEntity(name = "Caffeine / Black Coffee", dosage = "200 mg", timing = SupplementTiming.PRE_WORKOUT, servingsLeft = 30, tier = "Tier A", cycleNote = "4-Wk Cycle", interactionKey = "CAFFEINE"),
            SupplementEntity(name = "Electrolytes (Na + K)", dosage = "1000 mg Na", timing = SupplementTiming.INTRA_WORKOUT, servingsLeft = 30, tier = "Tier A", cycleNote = "Daily Training", interactionKey = "ELECTROLYTES"),
            SupplementEntity(name = "Whey Protein Isolate", dosage = "30 g", timing = SupplementTiming.POST_WORKOUT, servingsLeft = 30, tier = "Tier S", cycleNote = "Protein Target", interactionKey = "WHEY"),
            SupplementEntity(name = "Creatine Monohydrate", dosage = "5 g", timing = SupplementTiming.POST_WORKOUT, servingsLeft = 60, tier = "Tier S", cycleNote = "Daily Saturation", interactionKey = "CREATINE")
        )
        "late_night" -> listOf(
            SupplementEntity(name = "L-Citrulline (Pure)", dosage = "8 g", timing = SupplementTiming.PRE_WORKOUT, servingsLeft = 30, tier = "Tier A", cycleNote = "Zero-Stim Pump", interactionKey = "CITRULLINE"),
            SupplementEntity(name = "Electrolytes + Betaine", dosage = "1000 mg Na / 2.5 g", timing = SupplementTiming.INTRA_WORKOUT, servingsLeft = 30, tier = "Tier A", cycleNote = "Hydration & Power", interactionKey = "ELECTROLYTES"),
            SupplementEntity(name = "Whey Protein Isolate", dosage = "30 g", timing = SupplementTiming.POST_WORKOUT, servingsLeft = 30, tier = "Tier S", cycleNote = "Post-Session", interactionKey = "WHEY"),
            SupplementEntity(name = "Creatine Monohydrate", dosage = "5 g", timing = SupplementTiming.POST_WORKOUT, servingsLeft = 60, tier = "Tier S", cycleNote = "Daily Maintenance", interactionKey = "CREATINE"),
            SupplementEntity(name = "Magnesium Glycinate", dosage = "400 mg", timing = SupplementTiming.NIGHT, servingsLeft = 30, tier = "Tier B", cycleNote = "Sleep Quality", interactionKey = "MAGNESIUM"),
            SupplementEntity(name = "L-Theanine", dosage = "200 mg", timing = SupplementTiming.NIGHT, servingsLeft = 30, tier = "Tier B", cycleNote = "Alpha-Wave Calm", interactionKey = "THEANINE")
        )
        else -> seedSupplements() // "hypertrophy"
    }

    fun seedSkillMilestones(): List<SkillMilestoneEntity> = listOf(
        // Handstand Tree
        SkillMilestoneEntity(treeName = "Handstand", stepOrder = 1, title = "Wall Handstand", requirement = "60s belly-to-wall hold", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Handstand", stepOrder = 2, title = "Chest-to-Wall Line", requirement = "45s stacked ribs + heel pulls", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Handstand", stepOrder = 3, title = "30s Freestanding", requirement = "30s clean freestanding balance", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Handstand", stepOrder = 4, title = "Handstand Push-Up", requirement = "3×5 full ROM HSPU", isUnlocked = false),

        // Planche Tree
        SkillMilestoneEntity(treeName = "Planche", stepOrder = 1, title = "Planche Lean", requirement = "30s hold • wrists past hips", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Planche", stepOrder = 2, title = "Tuck Planche", requirement = "15s clean elevated hold", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Planche", stepOrder = 3, title = "Advanced Tuck", requirement = "12s flat back hold", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Planche", stepOrder = 4, title = "Straddle / Full Planche", requirement = "8s locked-arm lockout", isUnlocked = false),

        // Front Lever Tree
        SkillMilestoneEntity(treeName = "Front Lever", stepOrder = 1, title = "Tuck Front Lever", requirement = "25s horizontal hold", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Front Lever", stepOrder = 2, title = "Adv. Tuck Front Lever", requirement = "15s flat hips", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Front Lever", stepOrder = 3, title = "One-Leg / Straddle", requirement = "10s clean hold", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Front Lever", stepOrder = 4, title = "Full Front Lever", requirement = "8s straight body lock", isUnlocked = false),

        // Ring Muscle-Up Tree
        SkillMilestoneEntity(treeName = "Ring Muscle-Up", stepOrder = 1, title = "False-Grip Dead Hang", requirement = "30s deep false grip", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Ring Muscle-Up", stepOrder = 2, title = "Chest-to-Ring Pull-Up", requirement = "5 strict explosive reps", isUnlocked = true),
        SkillMilestoneEntity(treeName = "Ring Muscle-Up", stepOrder = 3, title = "Slow Negative Transition", requirement = "5s controlled roll-through", isUnlocked = false),
        SkillMilestoneEntity(treeName = "Ring Muscle-Up", stepOrder = 4, title = "Strict Ring Muscle-Up", requirement = "3 consecutive strict reps", isUnlocked = false)
    )

    fun seedVaultItems(): List<VaultItemEntity> = listOf(
        // ==================== 1. GYM INSTRUMENTS ====================
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Olympic Barbell & Power Rack",
            subTag = "Free Weights • 20 kg / 15 kg",
            primaryBenefit = "Full-body compound strength (Squat, Bench, Deadlift, OHP) with maximal overload potential.",
            howToUseOrSetup = "Set J-hooks at mid-sternum height; set safety spotter pins 2 inches below lowest squat or chest depth.",
            commonMistake = "Setting J-hooks too high, forcing lifters to tiptoe heavy weight out of the rack.",
            proRecommendation = "Check center knurling before squats; use standard 20 kg bar math in Plate Loader."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Adjustable & Hex Dumbbells",
            subTag = "Free Weights • Unilateral",
            primaryBenefit = "Fixes left/right strength imbalances and offers joint-friendly natural pressing arcs.",
            howToUseOrSetup = "Kick heavy dumbbells up smoothly with knees on incline benches; align wrists neutral.",
            commonMistake = "Swinging torso momentum instead of controlling a 2–3 second lowering phase.",
            proRecommendation = "Use neutral grip on incline presses whenever anterior shoulders feel tight."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "EZ-Curl Bar",
            subTag = "Free Weights • Arms",
            primaryBenefit = "Angled cambers reduce wrist torque compared to straight barbells on curls and skullcrushers.",
            howToUseOrSetup = "Grip inner camber for long-head biceps/brachialis or outer camber for classic curl path.",
            commonMistake = "Flaring elbows outward and rocking lower back on heavy sets.",
            proRecommendation = "Weighs ~10 kg on average—confirm bar weight before logging PRs."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Kettlebells",
            subTag = "Free Weights • Ballistic",
            primaryBenefit = "Offset center of mass builds explosive hip-hinge power, posterior chain, and grip stability.",
            howToUseOrSetup = "Hike bell high between thighs and snap hips horizontally to float bell to chest height.",
            commonMistake = "Squatting a kettlebell swing instead of hinging explosively at the hips.",
            proRecommendation = "Ideal for athletic conditioning finishers and warm-up hip activation."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Dual Functional Cable Tower",
            subTag = "Cable System • Constant Tension",
            primaryBenefit = "Eliminates gravity dead spots at top and bottom of reps—superior for side delts, flies, and arms.",
            howToUseOrSetup = "Match pulley height to target muscle fiber line (low for laterals/upper chest, high for pushdowns/face pulls).",
            commonMistake = "Standing too far or letting stack slam between reps, losing tension.",
            proRecommendation = "Step 1 foot forward for stability on behind-the-back lateral raises."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Lat Pulldown Station",
            subTag = "Cable System • Back",
            primaryBenefit = "Isolates lats and teres major with scalable load and clean vertical pull mechanics.",
            howToUseOrSetup = "Lock thigh pad tightly so body cannot lift; grip slightly outside shoulder width.",
            commonMistake = "Leaning back 45° and turning a vertical pulldown into a sloppy row.",
            proRecommendation = "Keep a disciplined 15° lean and drive elbows toward your pockets."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Seated Cable Row",
            subTag = "Cable System • Mid-Back",
            primaryBenefit = "Builds thick mid-traps, rhomboids, and lats with zero lower-back axial compression.",
            howToUseOrSetup = "Keep chest tall, soft knee bend, and pull handle cleanly toward lower ribs/navel.",
            commonMistake = "Rounding the lumbar spine forward during the eccentric stretch.",
            proRecommendation = "Use a neutral shoulder-width mag-grip or twin D-handles for maximal retraction."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "45-Degree Leg Press",
            subTag = "Plate-Loaded Machine • Legs",
            primaryBenefit = "High-output quad & glute loading with lower-back stability for pushing close to failure.",
            howToUseOrSetup = "Lower feet on sled = more quads; higher feet = more glutes/hamstrings.",
            commonMistake = "Letting lower back peel off the pad at the bottom or hyper-locking knees at top.",
            proRecommendation = "Grip side handles tightly to anchor glutes into the seat throughout every rep."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Hack Squat / Pendulum Squat",
            subTag = "Plate-Loaded Machine • Quads",
            primaryBenefit = "Unmatched deep knee-flexion stimulus for quad hypertrophy without balancing a barbell.",
            howToUseOrSetup = "Brace shoulders tight into pads; descend slowly until hamstrings cover calves.",
            commonMistake = "Letting heels lift off the platform at the bottom of the squat.",
            proRecommendation = "Pair with 0.75-inch heeled weightlifting shoes for maximal depth."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Smith Machine",
            subTag = "Guided Rail • Multi-Use",
            primaryBenefit = "Fixed bar path allows safe high-RPE sets to failure without a spotter.",
            howToUseOrSetup = "Position bench or feet 6–8 inches ahead of vertical rail for incline presses and squats.",
            commonMistake = "Standing directly underneath the bar during squats, straining lower back.",
            proRecommendation = "Check rail angle (vertical vs 7° angled) and face the upward angle on presses."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Leg Extension & Seated Leg Curl",
            subTag = "Pin-Loaded • Isolation",
            primaryBenefit = "Directly targets rectus femoris (quads) and biarticular hamstrings in a stretched position.",
            howToUseOrSetup = "Align your knee joint directly with the red pivot dot on the machine arm.",
            commonMistake = "Skipping pivot alignment, which puts unnecessary shear stress on knee ligaments.",
            proRecommendation = "Lock lap pad firmly on seated leg curls for 15%+ stronger hamstring recruitment."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Wood Gymnastic Rings (28mm–32mm)",
            subTag = "Gymnastics Apparatus • Instability",
            primaryBenefit = "Extreme upper-body stabilization, tendon strength, chest, lats, and deep core activation.",
            howToUseOrSetup = "Hang straps vertically without twists; master 30s Ring Support Hold before weighted dips.",
            commonMistake = "Flaring straps against arms and skipping Turned-Out (RTO) lockout positions.",
            proRecommendation = "Use the built-in Isometric Hold Timer with 3-2-1 audio cues on every ring hold."
        ),
        VaultItemEntity(
            category = VaultCategory.INSTRUMENT,
            name = "Parallettes & Dip Bars",
            subTag = "Gymnastics Apparatus • Skills",
            primaryBenefit = "Keeps wrists neutral and adds floor clearance for deep HSPU, L-sits, and planche leans.",
            howToUseOrSetup = "Set shoulder-width apart on non-slip flooring; grip bars firmly to engage forearms.",
            commonMistake = "Collapsing shoulders into ears during L-sit holds instead of actively depressing scapulae.",
            proRecommendation = "Track progression milestones inside the LOCKOUT Skill Tree."
        ),

        // ==================== 2. LIFTING ACCESSORIES (MY GEAR BAG) ====================
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Lifting Belt",
            subTag = "10mm Leather • 4-Inch Uniform",
            primaryBenefit = "Amplifies intra-abdominal pressure (Valsalva) for spinal stability on heavy squats, deadlifts & OHP.",
            howToUseOrSetup = "Wear only on top working sets above 80% 1RM; breathe OUT 360° against the leather.",
            commonMistake = "Wearing a belt during warm-ups or curls, or sucking your stomach in.",
            proRecommendation = "Choose lever lock for instant tightness or single-prong for quick waist adjustments.",
            isInMyGearBag = true
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Lifting Straps",
            subTag = "Cotton Lasso / Versa Gripps",
            primaryBenefit = "Prevents grip fatigue from bottlenecking back hypertrophy on heavy RDLs, rows, and pulldowns.",
            howToUseOrSetup = "Loop strap over bar away from palm and twist tight like revving a motorcycle throttle.",
            commonMistake = "Using straps on light warm-up sets, causing natural forearm strength to lag.",
            proRecommendation = "Versa Gripps save 10 seconds per set on fast machine transitions.",
            isInMyGearBag = true
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Wrist Wraps",
            subTag = "18-Inch Medium Stiffness",
            primaryBenefit = "Locks wrist joint stacked over forearm on heavy bench, OHP, handstands, and planche leans.",
            howToUseOrSetup = "Wrap directly OVER the wrist joint crease (not low on forearm) with firm tension.",
            commonMistake = "Keeping wraps cranked tight between sets and cutting off hand blood flow.",
            proRecommendation = "Unwrap immediately after finishing each working set.",
            isInMyGearBag = true
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Knee Sleeves",
            subTag = "5mm / 7mm Neoprene",
            primaryBenefit = "Maintains joint warmth, proprioception, and bottom-end stability on squats and leg presses.",
            howToUseOrSetup = "Fold top half down, slide over knee cap, then flip up snugly without cutting circulation.",
            commonMistake = "Leaving sweat-soaked sleeves zipped inside a gym bag without air-drying.",
            proRecommendation = "7mm for heavy barbell power squats; 5mm for hybrid hypertrophy/cross-training.",
            isInMyGearBag = false
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Liquid Chalk",
            subTag = "Magnesium Carbonate • Mess-Free",
            primaryBenefit = "Instant dry friction for deadlifts, weighted pull-ups, kettlebells, and wooden rings.",
            howToUseOrSetup = "Apply one dime-sized drop, rub across palms/fingers, and let air-dry 15 seconds.",
            commonMistake = "Grabbing the bar while chalk is still wet, leaving slippery residue.",
            proRecommendation = "Allowed in nearly all commercial gyms where loose block chalk is banned.",
            isInMyGearBag = true
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Gymnastics Grips",
            subTag = "2-Hole / 3-Hole Leather",
            primaryBenefit = "Prevents painful palm skin rips during high-volume pull-ups, toes-to-bar, and muscle-ups.",
            howToUseOrSetup = "Wear over cotton wristbands with a small dowel fold over the bar.",
            commonMistake = "Ignoring thick calluses until they tear mid-session.",
            proRecommendation = "Pair with a pumice stone at home weekly to keep calluses smooth.",
            isInMyGearBag = false
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Fractional Micro-Plates",
            subTag = "0.5 kg & 1.25 kg Steel Pair",
            primaryBenefit = "Breaks strength plateaus on OHP, bench press, and weighted pull-ups via micro-loading.",
            howToUseOrSetup = "Clip inside the barbell collar when a +5 kg jump breaks clean rep form.",
            commonMistake = "Forcing 5 kg jumps on upper-body lifts and stalling for months.",
            proRecommendation = "Essential companion for LOCKOUT's +2.5 kg Auto-Progressive Overload engine.",
            isInMyGearBag = false
        ),
        VaultItemEntity(
            category = VaultCategory.ACCESSORY,
            name = "Insulated Steel Shaker",
            subTag = "700 ml Stainless • Odor-Free",
            primaryBenefit = "Keeps intra-workout electrolytes ice-cold and eliminates permanent plastic whey odor.",
            howToUseOrSetup = "Add water first, then powder and wire whisk so whey never clumps at bottom.",
            commonMistake = "Leaving leftover protein inside a warm gym bag overnight.",
            proRecommendation = "Rinse within 5 minutes of finishing your shake.",
            isInMyGearBag = true
        ),

        // ==================== 3. SUPPLEMENT ENCYCLOPEDIA (EVIDENCE TIERS) ====================
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Creatine Monohydrate",
            subTag = "Tier S • Must-Have",
            primaryBenefit = "Increases ATP power output, rep strength, and intracellular muscle hydration.",
            howToUseOrSetup = "Take 5 g daily every single day (even rest days). Drink +500 ml extra water.",
            commonMistake = "Overpaying for fancy buffered or liquid creatine—micronized monohydrate is clinical gold.",
            proRecommendation = "Consistency beats timing; pair with post-workout shake to never forget.",
            defaultDosage = "5 g micronized",
            defaultTiming = SupplementTiming.POST_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Whey Protein Isolate",
            subTag = "Tier S • Must-Have",
            primaryBenefit = "Fast-digesting complete amino profile high in leucine to trigger muscle protein synthesis.",
            howToUseOrSetup = "25–40 g (1–1.5 scoops) post-workout or anytime needed to hit 1.8–2.2 g/kg daily protein.",
            commonMistake = "Buying amino-spiked tubs without third-party Informed Choice / NSF testing.",
            proRecommendation = "Choose Isolate over Concentrate if lactose-sensitive or cutting calories.",
            defaultDosage = "30 g (1 scoop)",
            defaultTiming = SupplementTiming.POST_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Caffeine Anhydrous",
            subTag = "Tier A • Performance",
            primaryBenefit = "Sharpens neural drive, force production, and lowers perceived exertion on heavy sets.",
            howToUseOrSetup = "150–300 mg (3–6 mg/kg max) taken 30–45 minutes pre-workout.",
            commonMistake = "Taking stimulants within 8–10 hours of bedtime, silently destroying deep REM recovery.",
            proRecommendation = "Cycle 4 weeks on, 1 week deload to restore receptor sensitivity.",
            defaultDosage = "200 mg",
            defaultTiming = SupplementTiming.PRE_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "L-Citrulline (Pure)",
            subTag = "Tier A • Performance",
            primaryBenefit = "Boosts nitric oxide blood flow, muscle pump, and nutrient delivery with zero stimulants.",
            howToUseOrSetup = "6–8 g pure L-Citrulline (or 8–10 g Citrulline Malate 2:1) 30–45 mins pre-workout.",
            commonMistake = "Buying under-dosed pre-workouts containing only 2–3 g.",
            proRecommendation = "Best choice for evening/late-night lifters avoiding caffeine.",
            defaultDosage = "6 g pure",
            defaultTiming = SupplementTiming.PRE_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Electrolytes (Sodium + Potassium)",
            subTag = "Tier A • Performance",
            primaryBenefit = "Prevents cramping, strengthens nerve-muscle contractions, and stops mid-session fatigue.",
            howToUseOrSetup = "800–1000 mg Sodium + 200 mg Potassium pre- or intra-workout in 700 ml water.",
            commonMistake = "Chugging plain water during heavy sweat sessions and diluting blood sodium.",
            proRecommendation = "Game-changing on leg days and in warm/humid gyms.",
            defaultDosage = "1000 mg Na / 200 mg K",
            defaultTiming = SupplementTiming.INTRA_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Vitamin D3 + K2",
            subTag = "Tier B • Health & Recovery",
            primaryBenefit = "Supports hormone balance, bone mineral density, and immune resilience.",
            howToUseOrSetup = "2,000–5,000 IU D3 + 100 mcg K2 in the morning WITH a fat-containing meal.",
            commonMistake = "Taking fat-soluble Vitamin D3 on an empty stomach with black coffee (poor absorption).",
            proRecommendation = "Pair with breakfast eggs or Omega-3 Fish Oil.",
            defaultDosage = "5000 IU + 100 mcg",
            defaultTiming = SupplementTiming.MORNING
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Omega-3 Fish Oil",
            subTag = "Tier B • Health & Recovery",
            primaryBenefit = "Reduces systemic joint inflammation from heavy iron and supports cardiovascular health.",
            howToUseOrSetup = "2,000–3,000 mg combined EPA + DHA daily with breakfast or dinner.",
            commonMistake = "Reading total fish oil mg instead of actual EPA + DHA content on the label.",
            proRecommendation = "Store in refrigerator to keep oil fresh and prevent fishy burps.",
            defaultDosage = "2500 mg EPA+DHA",
            defaultTiming = SupplementTiming.MORNING
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Magnesium Glycinate",
            subTag = "Tier B • Health & Recovery",
            primaryBenefit = "Calms the central nervous system and deepens restorative sleep after heavy CNS sessions.",
            howToUseOrSetup = "300–400 mg elemental magnesium 45 minutes before bed.",
            commonMistake = "Buying cheap Magnesium Oxide (poor absorption and GI upset).",
            proRecommendation = "Stack with 200 mg L-Theanine after evening workouts.",
            defaultDosage = "400 mg elemental",
            defaultTiming = SupplementTiming.NIGHT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "Beta-Alanine",
            subTag = "Tier B • Endurance",
            primaryBenefit = "Buffers lactic acid burn during 60–240 second high-rep sets and gymnastics conditioning.",
            howToUseOrSetup = "3.2–6.4 g daily; requires 4 weeks of consistent loading to saturate carnosine.",
            commonMistake = "Taking it only on workout days expecting acute stimulant effects.",
            proRecommendation = "Split into 2 doses if harmless skin tingling (paresthesia) bothers you.",
            defaultDosage = "3.2 g daily",
            defaultTiming = SupplementTiming.PRE_WORKOUT
        ),
        VaultItemEntity(
            category = VaultCategory.SUPPLEMENT,
            name = "BCAAs & OTC Test Boosters",
            subTag = "Tier F • Save Your Money",
            primaryBenefit = "Minimal ROI—redundant when hitting daily whole protein/whey targets.",
            howToUseOrSetup = "Skip these and allocate budget to quality whole food, whey isolate, creatine, and sleep.",
            commonMistake = "Spending $60/month on proprietary 'testosterone booster' blends that fail clinical trials.",
            proRecommendation = "LOCKOUT Evidence Rule: Never buy Tier F hype.",
            defaultDosage = "Skip",
            defaultTiming = SupplementTiming.MORNING
        ),

        // ==================== 4. GYM CLOTHES & FOOTWEAR ====================
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "Flat Zero-Drop Shoes",
            subTag = "Footwear • Deadlifts & Back Days",
            primaryBenefit = "Keeps heel millimeters off floor, shortening pull distance with rock-solid tripod stability.",
            howToUseOrSetup = "Use Converse Chuck Taylors, Vans, VivoBarefoot, Notorious Lift slippers, or bare socks.",
            commonMistake = "Pulling heavy deadlifts in thick, squishy running shoes that rock under load.",
            proRecommendation = "Keep lightweight deadlift slippers packed permanently inside your gym bag."
        ),
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "Weightlifting Shoes (0.75\" Heel)",
            subTag = "Footwear • Squats & Hack Squats",
            primaryBenefit = "Solid TPU/wood raised heel unlocks deep ankle flexion and keeps torso upright for quad drive.",
            howToUseOrSetup = "Strap midfoot tightly before barbell squats, front squats, and hack squats (Romaleos, Adipower, TYR L-1).",
            commonMistake = "Squatting in unstable cushioned sneakers—compressing foam destabilizes knees and ankles.",
            proRecommendation = "Essential upgrade if heels ever rise during deep squats."
        ),
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "Cross-Trainers",
            subTag = "Footwear • Hybrid & Calisthenics",
            primaryBenefit = "Flat, firm lifting heel paired with flexible forefoot for lunges, box jumps, and sleds.",
            howToUseOrSetup = "Ideal all-rounder (Nike Metcon, Reebok Nano, NOBULL) for Push/Pull and hybrid conditioning.",
            commonMistake = "Using them for long outdoor pavement runs where dedicated running shoes belong.",
            proRecommendation = "Best single-shoe choice if you only carry one pair to the gym."
        ),
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "Moisture-Wicking Performance Tees",
            subTag = "Apparel • 85–92% Poly / 8–15% Elastane",
            primaryBenefit = "Pulls sweat away from skin rapidly and stretches cleanly across shoulders without chafing.",
            howToUseOrSetup = "Wear fitted stretch blends on high-sweat sessions; tuck fitted tanks for inverted handstands.",
            commonMistake = "Using heavy 100% cotton during humid cardio—wet cotton traps heat and irritates skin.",
            proRecommendation = "ANTI-ODOR RULE: Never use liquid fabric softener—it coats fibers in wax and traps bacteria. Wash cold inside-out with sports detergent or 1/2 cup white vinegar and air-dry."
        ),
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "Heavyweight 'Pump Cover' Layer",
            subTag = "Apparel • 240–300 GSM Warm-Up",
            primaryBenefit = "Raises core muscle temperature and joint warmth fast during your first 15 minutes of warm-up.",
            howToUseOrSetup = "Wear a boxy cotton hoodie or heavyweight oversized tee through warm-up ramps, then strip down.",
            commonMistake = "Jumping straight into cold heavy pressing in an air-conditioned gym without warming tissue.",
            proRecommendation = "Pairs perfectly with LOCKOUT's 50% / 70% / 85% Warm-Up Ramp."
        ),
        VaultItemEntity(
            category = VaultCategory.APPAREL,
            name = "4-Way Stretch Zippered Shorts & Crew Socks",
            subTag = "Legwear • 5\"–7\" Inseam + Shin Guard",
            primaryBenefit = "Zippered pockets keep phone locked on flat benches; built-in liner stops inner-thigh friction.",
            howToUseOrSetup = "Pair 5\"–7\" stretch shorts with long ribbed crew socks on deadlift days to protect shins from knurling.",
            commonMistake = "Wearing loose pockets that dump phones onto iron plates during bench press.",
            proRecommendation = "For gymnastics/parallettes, choose snug compression shorts that never snag."
        )
    )
}
