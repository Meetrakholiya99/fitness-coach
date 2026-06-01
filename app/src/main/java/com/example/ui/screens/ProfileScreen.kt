package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SportyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var age by remember(userProfile.age) { mutableStateOf(userProfile.age.toString()) }
    var weight by remember(userProfile.weight) { mutableStateOf(userProfile.weight.toString()) }
    var height by remember(userProfile.height) { mutableStateOf(userProfile.height.toString()) }
    var gender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
    var fitnessLevel by remember(userProfile.fitnessLevel) { mutableStateOf(userProfile.fitnessLevel) }
    var goal by remember(userProfile.goal) { mutableStateOf(userProfile.goal) }
    var location by remember(userProfile.location) { mutableStateOf(userProfile.location) }
    var equipment by remember(userProfile.equipment) { mutableStateOf(userProfile.equipment) }
    var daysPerWeek by remember(userProfile.daysPerWeek) { mutableStateOf(userProfile.daysPerWeek) }
    var injuries by remember(userProfile.injuries) { mutableStateOf(userProfile.injuries) }

    // Dropdown States
    var showGenderMenu by remember { mutableStateOf(false) }
    var showLevelMenu by remember { mutableStateOf(false) }
    var showGoalMenu by remember { mutableStateOf(false) }
    var showLocMenu by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Real-time parsed state for live preview metrics
    val liveProfile = remember(age, gender, height, weight, fitnessLevel, goal, location, equipment, daysPerWeek, injuries) {
        val parsedAge = age.toIntOrNull() ?: userProfile.age
        val parsedWeight = weight.toDoubleOrNull() ?: userProfile.weight
        val parsedHeight = height.toDoubleOrNull() ?: userProfile.height
        UserProfile(
            age = parsedAge,
            gender = gender,
            height = parsedHeight,
            weight = parsedWeight,
            fitnessLevel = fitnessLevel,
            goal = goal,
            location = location,
            equipment = equipment,
            daysPerWeek = daysPerWeek,
            injuries = injuries
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Dynamic Coach Medical Assessment Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Physiological Report",
                        tint = NeonLime,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Physiological Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricWidget(
                        label = "BMI",
                        value = String.format("%.1f", liveProfile.bmi),
                        color = ElectricBlue,
                        subText = when {
                            liveProfile.bmi < 18.5 -> "Underweight"
                            liveProfile.bmi < 25 -> "Healthy"
                            liveProfile.bmi < 30 -> "Overweight"
                            else -> "Obese"
                        }
                    )
                    MetricWidget(
                        label = "Est. Body Fat",
                        value = String.format("%.1f%%", liveProfile.bodyFatEstimate),
                        color = SportyOrange,
                        subText = "Formula Avg"
                    )
                    MetricWidget(
                        label = "TDEE (Metabolic)",
                        value = "${liveProfile.tdee.toInt()}",
                        color = NeonLime,
                        subText = "kcal / day"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PHYSIOLOGICAL PROFILE",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Age, Height, Weight fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_age"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_height"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_weight"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gender & Fitness Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = showGenderMenu,
                onExpandedChange = { showGenderMenu = !showGenderMenu },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = gender,
                    onValueChange = {},
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderMenu) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .testTag("dropdown_gender"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showGenderMenu,
                    onDismissRequest = { showGenderMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Male") },
                        onClick = { gender = "Male"; showGenderMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Female") },
                        onClick = { gender = "Female"; showGenderMenu = false }
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = showLevelMenu,
                onExpandedChange = { showLevelMenu = !showLevelMenu },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = fitnessLevel,
                    onValueChange = {},
                    label = { Text("Experience") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLevelMenu) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .testTag("dropdown_level"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showLevelMenu,
                    onDismissRequest = { showLevelMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Beginner") },
                        onClick = { fitnessLevel = "Beginner"; showLevelMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Intermediate") },
                        onClick = { fitnessLevel = "Intermediate"; showLevelMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Advanced") },
                        onClick = { fitnessLevel = "Advanced"; showLevelMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TRAINING STRATEGY",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Training Goal Selector
        ExposedDropdownMenuBox(
            expanded = showGoalMenu,
            onExpandedChange = { showGoalMenu = !showGoalMenu },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = goal,
                onValueChange = {},
                label = { Text("Primary Goal") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGoalMenu) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("dropdown_goal"),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = showGoalMenu,
                onDismissRequest = { showGoalMenu = false }
            ) {
                listOf("Weight Loss", "Muscle Gain", "Fat Loss", "Strength", "Endurance").forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { goal = item; showGoalMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Selector
        ExposedDropdownMenuBox(
            expanded = showLocMenu,
            onExpandedChange = { showLocMenu = !showLocMenu },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = location,
                onValueChange = {},
                label = { Text("Workout Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocMenu) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("dropdown_location"),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = showLocMenu,
                onDismissRequest = { showLocMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    onClick = { location = "Home"; showLocMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Gym") },
                    onClick = { location = "Gym"; showLocMenu = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Available Equipment Details
        OutlinedTextField(
            value = equipment,
            onValueChange = { equipment = it },
            label = { Text("Available Equipment (Dumbbells, Mats, Band...)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonLime,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_equipment"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Workout Days Slider
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Days / Week",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$daysPerWeek Days",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = daysPerWeek.toFloat(),
                onValueChange = { daysPerWeek = it.toInt() },
                valueRange = 1f..7f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = NeonLime,
                    activeTrackColor = NeonLime,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("slider_days")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Injuries & Limitations
        OutlinedTextField(
            value = injuries,
            onValueChange = { injuries = it },
            label = { Text("Injuries or Limitations (None, Knee pain, Shoulder pain...)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonLime,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_injuries"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // SAVE COACH PROFILE BUTTON
        Button(
            onClick = {
                val validatedAge = age.toIntOrNull() ?: 28
                val validatedWeight = weight.toDoubleOrNull() ?: 75.0
                val validatedHeight = height.toDoubleOrNull() ?: 175.0
                onSaveProfile(
                    UserProfile(
                        age = validatedAge,
                        gender = gender,
                        height = validatedHeight,
                        weight = validatedWeight,
                        fitnessLevel = fitnessLevel,
                        goal = goal,
                        location = location,
                        equipment = equipment,
                        daysPerWeek = daysPerWeek,
                        injuries = injuries
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_profile_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = Color(0xFF101416)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Profile",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Apply Profile Changes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MetricWidget(
    label: String,
    value: String,
    color: Color,
    subText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}
