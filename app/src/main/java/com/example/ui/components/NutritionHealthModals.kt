package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLevel
import com.example.data.AppPrefsEntity
import com.example.data.DailyNutritionEntity
import com.example.data.DateUtils
import com.example.data.NutritionGoal
import com.example.domain.LockoutDomainEngine
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.SupplementDisplayItem

/** Clinical Mifflin-St Jeor Calorie, BMR, TDEE & Macro Calculator Dialog */
@Composable
fun CalorieCalculatorDialog(
    prefs: AppPrefsEntity,
    nutrition: DailyNutritionEntity,
    onApplyPlan: (
        weightKg: Float,
        heightCm: Int,
        ageYears: Int,
        isMale: Boolean,
        activityLevel: ActivityLevel,
        goal: NutritionGoal
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var isMale by remember { mutableStateOf(prefs.isMale) }
    var ageText by remember { mutableStateOf(prefs.athleteAge.toString()) }
    var heightText by remember { mutableStateOf(prefs.athleteHeightCm.toString()) }
    var weightText by remember { mutableStateOf(nutrition.bodyWeightKg.toInt().toString()) }
    var activity by remember { mutableStateOf(prefs.activityLevel) }
    var goal by remember { mutableStateOf(nutrition.goal) }

    val parsedAge = ageText.toIntOrNull()?.coerceIn(14, 90) ?: prefs.athleteAge
    val parsedHeight = heightText.toIntOrNull()?.coerceIn(130, 230) ?: prefs.athleteHeightCm
    val parsedWeight = weightText.toFloatOrNull()?.coerceIn(35f, 220f) ?: nutrition.bodyWeightKg

    val result = remember(parsedWeight, parsedHeight, parsedAge, isMale, activity, goal) {
        LockoutDomainEngine.calculateCaloriePlan(
            weightKg = parsedWeight,
            heightCm = parsedHeight,
            ageYears = parsedAge,
            isMale = isMale,
            activityLevel = activity,
            goal = goal
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = palette.volt)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("MIFFLIN-ST JEOR ENGINE", style = MaterialTheme.typography.labelSmall, color = palette.volt)
                    Text("Calorie & TDEE Calculator", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gender toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isMale,
                        onClick = { isMale = true },
                        label = { Text("Male") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.volt,
                            selectedLabelColor = palette.onVolt
                        )
                    )
                    FilterChip(
                        selected = !isMale,
                        onClick = { isMale = false },
                        label = { Text("Female") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.volt,
                            selectedLabelColor = palette.onVolt
                        )
                    )
                }

                // Biometric inputs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Age (yrs)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_age_input")
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_height_input")
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calc_weight_input")
                    )
                }

                Text("ACTIVITY LEVEL", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivityLevel.entries.forEach { lvl ->
                        FilterChip(
                            selected = activity == lvl,
                            onClick = { activity = lvl },
                            label = { Text(lvl.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.cyan.copy(alpha = 0.2f),
                                selectedLabelColor = palette.cyan
                            )
                        )
                    }
                }
                Text(activity.detail, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)

                Text("NUTRITION GOAL", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NutritionGoal.entries.forEach { g ->
                        FilterChip(
                            selected = goal == g,
                            onClick = { goal = g },
                            label = { Text(g.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.volt.copy(alpha = 0.2f),
                                selectedLabelColor = palette.volt
                            )
                        )
                    }
                }

                // Live Clinical Results Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.surface2)
                        .border(1.dp, palette.volt.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DAILY CALORIE TARGET", style = MaterialTheme.typography.labelSmall, color = palette.volt)
                            Text(
                                text = "${result.targetCalories} kcal",
                                style = TabularNumberStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                                color = palette.volt
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BMR ${result.bmrKcal} kcal", style = TabularNumberStyle.copy(fontSize = 12.sp), color = palette.textPrimary)
                            Text("TDEE ${result.tdeeKcal} kcal", style = TabularNumberStyle.copy(fontSize = 12.sp), color = palette.cyan)
                            Text("BMI ${result.bmi} • ${result.bmiCategory}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = palette.textSecondary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalcMiniBadge("PROTEIN", "${result.proteinGrams}g", palette.emerald, Modifier.weight(1f))
                        CalcMiniBadge("CARBS", "${result.carbsGrams}g", palette.cyan, Modifier.weight(1f))
                        CalcMiniBadge("FATS", "${result.fatsGrams}g", palette.amber, Modifier.weight(1f))
                    }

                    Text(
                        text = "Training Day: ${result.trainingDayCalories} kcal  •  Rest Day: ${result.restDayCalories} kcal  •  Water: ${result.waterTargetMl} ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyPlan(parsedWeight, parsedHeight, parsedAge, isMale, activity, goal)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("apply_calorie_plan_btn")
            ) {
                Text("APPLY TO PLAN", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = palette.textSecondary)
            }
        }
    )
}

@Composable
private fun CalcMiniBadge(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val palette = LockoutColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.canvas)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
        Text(value, style = TabularNumberStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = color)
    }
}

/** Custom Meal & Calorie Logger Dialog */
@Composable
fun LogCustomMealDialog(
    onLogMeal: (name: String, protein: Int, carbs: Int, fats: Int, calories: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var mealName by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("35") }
    var carbsText by remember { mutableStateOf("50") }
    var fatsText by remember { mutableStateOf("12") }
    var caloriesOverride by remember { mutableStateOf("") }

    val p = proteinText.toIntOrNull()?.coerceIn(0, 300) ?: 0
    val c = carbsText.toIntOrNull()?.coerceIn(0, 600) ?: 0
    val f = fatsText.toIntOrNull()?.coerceIn(0, 200) ?: 0
    val autoKcal = (p * 4) + (c * 4) + (f * 9)
    val finalKcal = caloriesOverride.toIntOrNull()?.takeIf { it > 0 } ?: autoKcal

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = palette.volt)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Meal & Macros", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name (e.g. Grilled Salmon & Rice)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_meal_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Protein (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Carbs (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatsText,
                        onValueChange = { fatsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Fats (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = caloriesOverride,
                    onValueChange = { caloriesOverride = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Calories (Auto: $autoKcal kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Total logged: $finalKcal kcal ($p P / $c C / $f F)",
                    style = TabularNumberStyle.copy(fontSize = 13.sp),
                    color = palette.volt
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val label = mealName.trim().ifEmpty { "Custom Meal" }
                    onLogMeal(label, p, c, f, finalKcal)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("confirm_custom_meal_btn")
            ) {
                Text("LOG MEAL", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textSecondary)
            }
        }
    )
}

/** Supplement Daily Dose Alarm Scheduler Dialog */
@Composable
fun SupplementAlarmDialog(
    item: SupplementDisplayItem,
    onSaveAlarm: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onTestAlarm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var enabled by remember { mutableStateOf(item.entity.reminderEnabled) }
    var hour by remember { mutableIntStateOf(item.entity.reminderHour) }
    var minute by remember { mutableIntStateOf(item.entity.reminderMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = palette.amber)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("SUPPLEMENT DOSE ALARM", style = MaterialTheme.typography.labelSmall, color = palette.amber)
                    Text(item.entity.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface2)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Dose Reminder", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text(
                            text = "${item.totalDaysTaken} days tracked • ${item.streakDays}d streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.cyan
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onVolt,
                            checkedTrackColor = palette.volt
                        ),
                        modifier = Modifier.testTag("alarm_enable_switch")
                    )
                }

                // Digital Time Stepper
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.canvas)
                        .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = DateUtils.formatTime12h(hour, minute),
                        style = TabularNumberStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
                        color = if (enabled) palette.amber else palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HOUR", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { hour = (hour + 23) % 24 }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease hour", tint = palette.textPrimary)
                                }
                                IconButton(onClick = { hour = (hour + 1) % 24 }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase hour", tint = palette.volt)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MINUTE", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { minute = (minute + 55) % 60 }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease 5 minutes", tint = palette.textPrimary)
                                }
                                IconButton(onClick = { minute = (minute + 5) % 60 }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase 5 minutes", tint = palette.volt)
                                }
                            }
                        }
                    }
                }

                // Quick Window Presets
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "7:30 AM" to (7 to 30),
                        "12:30 PM" to (12 to 30),
                        "5:30 PM" to (17 to 30),
                        "7:15 PM" to (19 to 15),
                        "10:00 PM" to (22 to 0)
                    ).forEach { (label, hm) ->
                        Surface(
                            onClick = {
                                hour = hm.first
                                minute = hm.second
                                enabled = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Immediate Test Alarm Chime & Notification
                OutlinedButton(
                    onClick = onTestAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("test_supplement_alarm_btn"),
                    border = BorderStroke(1.dp, palette.amber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = palette.amber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TEST ALARM SOUND & NOTIFICATION", color = palette.amber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAlarm(enabled, hour, minute)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("save_supplement_alarm_btn")
            ) {
                Text("SAVE ALARM", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textSecondary)
            }
        }
    )
}

/** Add Custom Healthy Body Habit Dialog */
@Composable
fun AddHabitDialog(
    onAddHabit: (title: String, category: String, targetLabel: String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Recovery") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.emerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Healthy Habit", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name (e.g. 20m Sauna / Stretching)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_habit_title_input")
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Recovery", "Cardio", "Nutrition", "Hydration", "Joints", "Mindset").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.emerald.copy(alpha = 0.2f),
                                selectedLabelColor = palette.emerald
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Note (e.g. Daily before bed)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddHabit(title, category, target)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("confirm_add_habit_btn")
            ) {
                Text("ADD HABIT", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textSecondary)
            }
        }
    )
}
