package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayWorkout
import com.example.data.model.Exercise
import com.example.data.model.GeneratedPlan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SportyOrange

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkoutScreen(
    latestPlan: GeneratedPlan?,
    isGenerating: Boolean,
    generationError: String?,
    onGenerate: () -> Unit,
    onCompleteWorkout: (Double) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = NeonLime,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("generation_loader"),
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Formulating Plan...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your AI Coach is designing a customized, science-backed progression split & nutrition guide.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else if (latestPlan == null) {
            // Empty State Dashboard
            EmptyWorkoutState(
                generationError = generationError,
                onGenerate = onGenerate,
                onClearError = onClearError
            )
        } else {
            // Render Generated Plan Dashboard
            PlanDashboard(
                plan = latestPlan,
                onCompleteWorkout = onCompleteWorkout
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun EmptyWorkoutState(
    generationError: String?,
    onGenerate: () -> Unit,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = NeonLime,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Coach Workout Generator",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Get scientifically accurate, custom-tailored weight routines and targeted diet blueprints crafted directly from your customized body metrics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (generationError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coach Advisory Error",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onClearError) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Text(
                            text = generationError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_plan_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color(0xFF101416)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Coach Split",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlanDashboard(
    plan: GeneratedPlan,
    onCompleteWorkout: (Double) -> Unit
) {
    // Current calorie target and macro targets
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TARGET FUEL PLAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${plan.daily_calories} Target Calories",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonLime
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SportyOrange.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Calorie target",
                        tint = SportyOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Macro sliders representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroCircleItem(name = "Protein", amount = plan.protein, color = NeonLime)
                MacroCircleItem(name = "Carbs", amount = plan.carbs, color = ElectricBlue)
                MacroCircleItem(name = "Fat", amount = plan.fat, color = SportyOrange)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Workout Day Selector & Routines
    var selectedDayIndex by remember { mutableStateOf(0) }
    val days = plan.workout_plan
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TRAINING DAYS SPLIT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "${days.size} Sessions",
            style = MaterialTheme.typography.bodySmall,
            color = ElectricBlue,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (days.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days.size) { index ->
                val day = days[index]
                val isSelected = index == selectedDayIndex
                
                Card(
                    onClick = { selectedDayIndex = index },
                    modifier = Modifier
                        .width(120.dp)
                        .testTag("day_tab_$index"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NeonLime else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF101416) else Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.focus,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color(0xFF333333).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Day Plan
        val currentDayPlan = days[selectedDayIndex]
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S TARGET SPLIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDayPlan.focus,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonLime
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(ElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentDayPlan.day,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Exercises List
                if (currentDayPlan.exercises.isEmpty()) {
                    Text(
                        text = "Rest Day! Proper recovery is where the muscles grow. Stretch, hydrate, and maintain meal goals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    currentDayPlan.exercises.forEachIndexed { i, ex ->
                        ExerciseRowItem(index = i + 1, exercise = ex)
                    }
                    
                    val totalCalories = currentDayPlan.exercises.sumOf { it.calories }

                    Spacer(modifier = Modifier.height(16.dp))

                    // COMPLETE WORKOUT BUTTON
                    Button(
                        onClick = { onCompleteWorkout(totalCalories.toDouble()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color(0xFF101416)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("complete_workout_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Session Accomplished (~${totalCalories} kcal)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // AI Labs
    Text(
        text = "AI TRAINING INTELLIGENCE LABS",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(12.dp))

    AICameraTrackerSimulator()

    Spacer(modifier = Modifier.height(24.dp))

    // Warm-up & Cool-down routine
    Text(
        text = "ROUTINES & ADVANCED METHODOLOGY",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(12.dp))

    ExpandableCoachCard(title = "Warm-up Routine", content = plan.warm_up, icon = Icons.Default.DirectionsRun, tint = ElectricBlue)
    ExpandableCoachCard(title = "Cool-down Routine", content = plan.cool_down, icon = Icons.Default.Spa, tint = SportyOrange)
    ExpandableCoachCard(title = "Monthly Progression Plan", content = plan.monthly_progression, icon = Icons.Default.CalendarMonth, tint = NeonLime)
    ExpandableCoachCard(title = "Progressive Overload Blueprint", content = plan.progressive_overload, icon = Icons.Default.TrendingUp, tint = ElectricBlue)
    ExpandableCoachCard(title = "Recovery & Injury Cautions", content = plan.recovery_advice, icon = Icons.Default.Healing, tint = SportyOrange)

    Spacer(modifier = Modifier.height(24.dp))

    // Nutrition section
    Text(
        text = "MEAL BLUEPRINTS & HYDRATION",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(12.dp))

    plan.nutrition?.let { diet ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Hydration Target",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .background(ElectricBlue.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = diet.water_target,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBlue
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                DietMealItem(mealName = "Breakfast Option", details = diet.breakfast, icon = Icons.Default.Coffee, tint = SportyOrange)
                DietMealItem(mealName = "Lunch Option", details = diet.lunch, icon = Icons.Default.LunchDining, tint = NeonLime)
                DietMealItem(mealName = "Dinner Option", details = diet.dinner, icon = Icons.Default.RestaurantMenu, tint = ElectricBlue)
                DietMealItem(mealName = "Snack Options", details = diet.snacks, icon = Icons.Default.Fastfood, tint = SportyOrange)
            }
        }
    }
}

@Composable
fun ExerciseRowItem(index: Int, exercise: Exercise) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.0f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = exercise.target,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${exercise.sets}x${exercise.reps}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LabelMetric(label = "Rest", value = exercise.rest)
                    LabelMetric(label = "Level", value = exercise.difficulty)
                    LabelMetric(label = "Burn", value = "${exercise.calories} kcal")
                }

                Spacer(modifier = Modifier.height(12.dp))

                CoachPoints(title = "Correct Form Instructions", points = exercise.instructions, bulletColor = NeonLime)
                Spacer(modifier = Modifier.height(8.dp))
                CoachPoints(title = "Common Mistakes to Avoid", points = exercise.mistakes, bulletColor = SportyOrange)
                Spacer(modifier = Modifier.height(8.dp))
                CoachPoints(title = "Key Workout Benefits", points = exercise.benefits, bulletColor = ElectricBlue)
            }
        }
    }
}

@Composable
fun LabelMetric(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun CoachPoints(title: String, points: List<String>, bulletColor: Color) {
    if (points.isNotEmpty()) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            points.forEach { pt ->
                Row(
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "• ", style = MaterialTheme.typography.bodySmall, color = bulletColor, fontWeight = FontWeight.Black)
                    Text(text = pt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun ExpandableCoachCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun DietMealItem(
    mealName: String,
    details: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = mealName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun MacroCircleItem(name: String, amount: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color.copy(alpha = 0.08f), CircleShape)
                .border(BorderStroke(2.dp, color.copy(alpha = 0.3f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AICameraTrackerSimulator() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pose Tracker, 1 = Body Scan Analyzer
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) NeonLime else Color.Transparent,
                        contentColor = if (selectedTab == 0) Color(0xFF101416) else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Posture & Form", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) ElectricBlue else Color.Transparent,
                        contentColor = if (selectedTab == 1) Color(0xFF101416) else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Accessibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Body Scan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                PoseFormCorrectionSimulator()
            } else {
                BodySymmetryScannerSimulator()
            }
        }
    }
}

@Composable
fun PoseFormCorrectionSimulator() {
    var isTrackerActive by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf("Deep Squat") }
    var language by remember { mutableStateOf("English") } // English, Indian English, Gujarati, Hindi
    var playbackState by remember { mutableStateOf("Play") } // Play, Pause
    var poseStateFraction by remember { mutableStateOf(0f) } // animated state fraction
    
    // Auto-cycling joint coordinates to simulate active live pose-tracking squats
    LaunchedEffect(isTrackerActive, playbackState) {
        if (isTrackerActive && playbackState == "Play") {
            var increasing = true
            while (true) {
                if (increasing) {
                    poseStateFraction += 0.05f
                    if (poseStateFraction >= 1f) increasing = false
                } else {
                    poseStateFraction -= 0.05f
                    if (poseStateFraction <= 0f) increasing = true
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    // Dynamic calculations representing key joint angle trackers
    val kneeFlexion = (165 - (poseStateFraction * 85)).toInt() // Range 165deg to 80deg
    val spinalTilt = (180 - (poseStateFraction * 14)).toInt() // Range 180deg to 166deg
    val formScore = if (kneeFlexion < 95) 98 else if (kneeFlexion < 120) 91 else 84

    // Language-specific voice feedback alerts
    val vocalAlert = when (language) {
        "Gujarati" -> {
            if (kneeFlexion > 130) "નીચે નમો! (Lower your hips!)"
            else if (kneeFlexion < 95) "સ્મૂથ ગતિ, છાતી સીધી રાખો! Scan complete."
            else "આગળ વધો, શ્વાસ બહાર કાઢો!"
        }
        "Hindi" -> {
            if (kneeFlexion > 130) "और नीचे जाएं! (Lower down!)"
            else if (kneeFlexion < 95) "बढ़िया पोस्चर, पीठ सीधी! Scan complete."
            else "जारी रखें, सांस बाहर निकालें!"
        }
        else -> { // English
            if (kneeFlexion > 130) "Lower down! Reach parallel hip crease."
            else if (kneeFlexion < 95) "Perfect depth squat! Spine is neutral."
            else "Push through the heels. Exhale up!"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Voice guidance language:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("English", "Hindi", "Gujarati").forEach { lang ->
                        val isSelected = language == lang
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NeonLime.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (isSelected) NeonLime else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clickable { language = lang }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = lang, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) NeonLime else Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Select Routine:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Squat", "Deadlift", "Curl").forEach { r ->
                        val isSelected = selectedExercise.contains(r)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clickable { selectedExercise = if (r == "Squat") "Deep Squat" else if (r == "Deadlift") "Bar Deadlift" else "Bicep Curl" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = r, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        if (!isTrackerActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = NeonLime, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Activate Frontal Camera Live Tracking", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The AI voice coach auto-corrects joint flexion degrees, spinal posture, and rep ranges in real-time.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isTrackerActive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF101416)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Engage Real-Time Scan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // ENGAGED POSE TRACKING SCREEN SIMULATION (With beautiful skeleton animation)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF0C0E0F), RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(2.dp, Brush.linearGradient(listOf(NeonLime, ElectricBlue))),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    // Draw Simulated Skeleton using Box elements
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Title / Live Watermark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE AI: $selectedExercise",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                               )
                            }
                            Text(
                                text = "FPS: 60 / Latency: 9ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }

                        // THE SKELETON WIREFRAME SIMULATOR (Drawing line segments on canvas manually or using styled containers)
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val headX = w / 2f
                            val headY = h * 0.18f

                            val collarX = w / 2f
                            val collarY = h * 0.32f

                            val pelvisX = w / 2f
                            val pelvisY = h * 0.58f

                            // Left Arm
                            val lShoulderX = w * 0.4f
                            val lShoulderY = h * 0.34f
                            val lElbowX = w * 0.32f
                            val lElbowY = h * (0.45f + (poseStateFraction * 0.08f))
                            val lWristX = w * 0.34f
                            val lWristY = h * (0.58f - (poseStateFraction * 0.1f))

                            // Right Arm
                            val rShoulderX = w * 0.6f
                            val rShoulderY = h * 0.34f
                            val rElbowX = w * 0.68f
                            val rElbowY = h * (0.45f + (poseStateFraction * 0.08f))
                            val rWristX = w * 0.66f
                            val rWristY = h * (0.58f - (poseStateFraction * 0.1f))

                            // Left Leg (flexing based on poseStateFraction)
                            val lHipX = w * 0.44f
                            val lHipY = h * 0.58f
                            val lKneeX = w * (0.36f - (poseStateFraction * 0.08f))
                            val lKneeY = h * (0.72f + (poseStateFraction * 0.1f))
                            val lAnkleX = w * 0.42f
                            val lAnkleY = h * 0.9f

                            // Right Leg
                            val rHipX = w * 0.56f
                            val rHipY = h * 0.58f
                            val rKneeX = w * (0.64f + (poseStateFraction * 0.08f))
                            val rKneeY = h * (0.72f + (poseStateFraction * 0.1f))
                            val rAnkleX = w * 0.58f
                            val rAnkleY = h * 0.9f

                            // Draw Bone Connections
                            // Spine
                            drawLine(color = Color.White, start = Offset(collarX, collarY), end = Offset(pelvisX, pelvisY), strokeWidth = 3f)
                            // Shoulder Line
                            drawLine(color = Color.White, start = Offset(lShoulderX, lShoulderY), end = Offset(rShoulderX, rShoulderY), strokeWidth = 3f)
                            // Hip Line
                            drawLine(color = Color.White, start = Offset(lHipX, lHipY), end = Offset(rHipX, rHipY), strokeWidth = 3f)

                            // Left Arm
                            drawLine(color = ElectricBlue, start = Offset(lShoulderX, lShoulderY), end = Offset(lElbowX, lElbowY), strokeWidth = 4f)
                            drawLine(color = ElectricBlue, start = Offset(lElbowX, lElbowY), end = Offset(lWristX, lWristY), strokeWidth = 4f)

                            // Right Arm
                            drawLine(color = ElectricBlue, start = Offset(rShoulderX, rShoulderY), end = Offset(rElbowX, rElbowY), strokeWidth = 4f)
                            drawLine(color = ElectricBlue, start = Offset(rElbowX, rElbowY), end = Offset(rWristX, rWristY), strokeWidth = 4f)

                            // Left Leg
                            drawLine(color = NeonLime, start = Offset(lHipX, lHipY), end = Offset(lKneeX, lKneeY), strokeWidth = 4f)
                            drawLine(color = NeonLime, start = Offset(lKneeX, lKneeY), end = Offset(lAnkleX, lAnkleY), strokeWidth = 4f)

                            // Right Leg
                            drawLine(color = NeonLime, start = Offset(rHipX, rHipY), end = Offset(rKneeX, rKneeY), strokeWidth = 4f)
                            drawLine(color = NeonLime, start = Offset(rKneeX, rKneeY), end = Offset(rAnkleX, rAnkleY), strokeWidth = 4f)

                            // Draw Joint Circles
                            drawCircle(color = Color.White, radius = 6f, center = Offset(headX, headY))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(collarX, collarY))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(pelvisX, pelvisY))
                            
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(lShoulderX, lShoulderY))
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(lElbowX, lElbowY))
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(lWristX, lWristY))
                            
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(rShoulderX, rShoulderY))
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(rElbowX, rElbowY))
                            drawCircle(color = ElectricBlue, radius = 4f, center = Offset(rWristX, rWristY))

                            drawCircle(color = NeonLime, radius = 4f, center = Offset(lHipX, lHipY))
                            drawCircle(color = NeonLime, radius = 5f, center = Offset(lKneeX, lKneeY)) // highlighted knee
                            drawCircle(color = NeonLime, radius = 4f, center = Offset(lAnkleX, lAnkleY))

                            drawCircle(color = NeonLime, radius = 4f, center = Offset(rHipX, rHipY))
                            drawCircle(color = NeonLime, radius = 5f, center = Offset(rKneeX, rKneeY)) // highlighted knee
                            drawCircle(color = NeonLime, radius = 4f, center = Offset(rAnkleX, rAnkleY))
                        }

                        // Angle Telemetry Indicators overlays on screen
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                Text(text = "R Joint Flex: ${kneeFlexion}°", style = MaterialTheme.typography.labelSmall, color = NeonLime, fontWeight = FontWeight.Bold)
                                Text(text = "Spinal Tilt: ${spinalTilt}°", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Text(text = "Score: ${formScore}% Match", style = MaterialTheme.typography.labelSmall, color = if (formScore > 90) NeonLime else SportyOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Voice Wave Speaker feedback bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "LIVE VOICE AI TRAINER FEEDBACK", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                            Text(text = vocalAlert, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }

                // Control panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { playbackState = if (playbackState == "Play") "Pause" else "Play" },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState == "Play") Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (playbackState == "Play") "Pause Feed" else "Resume", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { isTrackerActive = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SportyOrange, contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Deactivate Scanners", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun BodySymmetryScannerSimulator() {
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var scanYCoordinateOffset by remember { mutableStateOf(0f) }

    // Dynamic horizontal scan lines loading
    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanYCoordinateOffset = 0f
            while (scanYCoordinateOffset < 1f) {
                scanYCoordinateOffset += 0.05f
                kotlinx.coroutines.delay(80)
            }
            scanCompleted = true
            isScanning = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!isScanning && !scanCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Symmetry & Muscular Development Scan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimate muscular proportion balances, left-right skeletal symmetry, and exact body fat density using camera scan calibration.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isScanning = true
                            scanCompleted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color(0xFF101416)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Calibrate Body Scanner", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else if (isScanning) {
            // SCANNING VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF0C0E0F), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.5.dp, ElectricBlue), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ElectricBlue.copy(alpha = 0.4f), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Aligning body grids: ${ (scanYCoordinateOffset * 100).toInt() }%", style = MaterialTheme.typography.bodySmall, color = ElectricBlue)
                }

                // Gimmick Horizontal Scanning green/blue bar sweeping
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = (220 * scanYCoordinateOffset).dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, ElectricBlue, ElectricBlue, Color.Transparent)))
                )
            }
        } else {
            // COMPLETED RESULTS REPORT
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI BODY COMPOSITION REPORT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        Box(
                            modifier = Modifier
                                .background(ElectricBlue.copy(alpha = 0.12f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("✓ Scan Complete", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SymmetryReportBox(label = "Estimated Body Fat", value = "15.4%", status = "Optimal Lean", color = NeonLime)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SymmetryReportBox(label = "L/R Muscle Balance", value = "98.2%", status = "Excellent", color = NeonLime)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SymmetryReportBox(label = "Development Target", value = "94/100", status = "High Tone", color = ElectricBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Report Summary: Symmetry balance is extremely strong. Left quadricep muscle mass matches Right quadricep density to 98.2%. Calorie guidelines have successfully calibrated to achieve targeted body-fat reduction.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 14.sp
                    )

                    Button(
                        onClick = { scanCompleted = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Perform New Scan Calibration", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SymmetryReportBox(label: String, value: String, status: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = status, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = color.copy(alpha = 0.8f))
        }
    }
}
