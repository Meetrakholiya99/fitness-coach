package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PersonalRecordEntity
import com.example.data.local.ProgressLogEntity
import com.example.data.local.StreakStatsEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SportyOrange

@Composable
fun ProgressScreen(
    logs: List<ProgressLogEntity>,
    prs: List<PersonalRecordEntity>,
    streakStats: StreakStatsEntity?,
    isReviewing: Boolean,
    coachReview: String?,
    onRequestReview: () -> Unit,
    onAddLog: (Double, Double, Double, String) -> Unit,
    onDeleteLog: (Int) -> Unit,
    onAddPR: (String, Double, Int) -> Unit,
    onDeletePR: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Log Form Input States
    var logWeight by remember { mutableStateOf("") }
    var logBodyFat by remember { mutableStateOf("") }
    var logBurnedCalories by remember { mutableStateOf("") }
    var logNote by remember { mutableStateOf("") }
    var showLogDialog by remember { mutableStateOf(false) }

    // PR Form Input States
    var prName by remember { mutableStateOf("") }
    var prWeight by remember { mutableStateOf("") }
    var prReps by remember { mutableStateOf("") }
    var showPrDialog by remember { mutableStateOf(false) }

    // Smart Wearable Simulator state
    var isSyncingWearable by remember { mutableStateOf(false) }
    var wearableLastSync by remember { mutableStateOf("Ready") }
    var syncedHeartRate by remember { mutableStateOf(72) }
    var syncedSteps by remember { mutableStateOf(5820) }
    var syncedSleep by remember { mutableStateOf("7h 15m") }
    var syncedCalories by remember { mutableStateOf(320) }

    // Computed level-up metrics based on real database contents
    val totalCaloriesBurned = logs.sumOf { it.caloriesBurned }
    val xpFromLogs = logs.size * 75
    val xpFromPrs = prs.size * 150
    val xpFromStreak = (streakStats?.currentStreak ?: 0) * 120
    val totalXp = xpFromLogs + xpFromPrs + xpFromStreak
    val xpThreshold = 500
    val currentLevel = (totalXp / xpThreshold) + 1
    val currentXpProgress = totalXp % xpThreshold
    val xpProgressFraction = currentXpProgress.toFloat() / xpThreshold.toFloat()

    // Badges based on real database records
    val badgeStreakUnlocked = (streakStats?.currentStreak ?: 0) > 0
    val badgeIronUnlocked = prs.isNotEmpty()
    val badgeHabitUnlocked = logs.size >= 2
    val badgeBurnerUnlocked = totalCaloriesBurned > 500.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Streak Card Dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WORKOUT STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${streakStats?.currentStreak ?: 0} Days Active",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if ((streakStats?.currentStreak ?: 0) > 0) "Last active on: ${streakStats?.lastActiveDate}" else "Log a workout to start your streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SportyOrange.copy(alpha = 0.12f), CircleShape)
                        .border(1.5.dp, SportyOrange.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak flame icon",
                        tint = SportyOrange,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ADVANCED GAMIFICATION: LEVEL UP & ACHIEVEMENT HUB
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Title and Level Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FITNESS LEVEL & RANKS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Level $currentLevel Warrior",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(NeonLime.copy(alpha = 0.12f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalXp XP TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress to next level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$currentXpProgress / $xpThreshold XP",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = xpProgressFraction.coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = NeonLime,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                // Unlocked achievements summary
                Text(
                    text = "UNLOCKED SPECIALTY BADGES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgeItem(
                        title = "Flame On",
                        description = "Streaked active",
                        icon = Icons.Default.LocalFireDepartment,
                        unlocked = badgeStreakUnlocked,
                        activeColor = SportyOrange,
                        modifier = Modifier.weight(1f)
                    )
                    BadgeItem(
                        title = "Iron Disciple",
                        description = "Milestone PR",
                        icon = Icons.Default.EmojiEvents,
                        unlocked = badgeIronUnlocked,
                        activeColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    BadgeItem(
                        title = "Habits Formed",
                        description = "Multiple logs",
                        icon = Icons.Default.CheckCircle,
                        unlocked = badgeHabitUnlocked,
                        activeColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    BadgeItem(
                        title = "Burn Master",
                        description = ">500 kcal split",
                        icon = Icons.Default.FlashOn,
                        unlocked = badgeBurnerUnlocked,
                        activeColor = SportyOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SMART WEARABLE INTEGRATION HUB
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SMART WEARABLE SYNC CENTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WearOS / Garmin / Fitbit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                if (wearableLastSync == "Synchronized") NeonLime.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = wearableLastSync,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (wearableLastSync == "Synchronized") NeonLime else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tracked stats from wearables
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WearableStatItem(
                        name = "Heart Rate",
                        value = "$syncedHeartRate BPM",
                        icon = Icons.Default.Favorite,
                        tint = SportyOrange,
                        modifier = Modifier.weight(1f)
                    )
                    WearableStatItem(
                        name = "Steps",
                        value = "%,d".format(syncedSteps),
                        icon = Icons.Default.DirectionsRun,
                        tint = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    WearableStatItem(
                        name = "Sleep",
                        value = syncedSleep,
                        icon = Icons.Default.Bedtime,
                        tint = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    WearableStatItem(
                        name = "Kcal Log",
                        value = "$syncedCalories kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        tint = SportyOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isSyncingWearable = true
                            wearableLastSync = "Syncing..."
                            kotlinx.coroutines.delay(1200)
                            // Simulate updated wearable logs
                            syncedHeartRate = (68..80).random()
                            syncedSteps += (1200..2500).random()
                            syncedCalories += (80..180).random()
                            syncedSleep = listOf("7h 15m", "7h 48m", "8h 02m", "6h 55m").random()
                            wearableLastSync = "Synchronized"
                            isSyncingWearable = false
                        }
                    },
                    enabled = !isSyncingWearable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSyncingWearable) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Bluetooth Sync...")
                    } else {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Real-Time Bluetooth Sync", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Coach AI Review Button & Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Personal Coach Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request your coach to review your active weight metrics, BMI records, calories, and PR history to provide guidance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                if (coachReview != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = coachReview,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRequestReview,
                    enabled = !isReviewing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_review_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = Color(0xFF101416)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isReviewing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF101416))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing History...")
                    } else {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compile Dynamic Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WEIGHT TRACKER SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEIGHT & BMI HISTORY",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            IconButton(
                onClick = { showLogDialog = true },
                modifier = Modifier.testTag("add_log_pbutton")
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Log progress metrics", tint = ElectricBlue)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history logged yet. Use + to record progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            logs.take(15).forEach { log ->
                HistoryRowItem(log = log, onDelete = { onDeleteLog(log.id) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PERSONAL RECORDS SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PERSONAL STRENGTH RECORDS (PR)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            IconButton(
                onClick = { showPrDialog = true },
                modifier = Modifier.testTag("add_pr_button")
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add strength PR", tint = NeonLime)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (prs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No lifting PRs recorded. Tap + to record milestones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            prs.forEach { pr ->
                PrRowItem(pr = pr, onDelete = { onDeletePR(pr.id) })
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Progress Metrics Log Dialog
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Log Progress Metrics", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = logWeight,
                        onValueChange = { logWeight = it },
                        label = { Text("Body weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                        modifier = Modifier.testTag("input_log_weight")
                    )
                    OutlinedTextField(
                        value = logBodyFat,
                        onValueChange = { logBodyFat = it },
                        label = { Text("Estimated Body Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                        modifier = Modifier.testTag("input_log_bf")
                    )
                    OutlinedTextField(
                        value = logBurnedCalories,
                        onValueChange = { logBurnedCalories = it },
                        label = { Text("Calories Burned (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                        modifier = Modifier.testTag("input_log_cal")
                    )
                    OutlinedTextField(
                        value = logNote,
                        onValueChange = { logNote = it },
                        label = { Text("Coaching notes (e.g. Felt powerful)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                        modifier = Modifier.testTag("input_log_note")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = logWeight.toDoubleOrNull() ?: 0.0
                        val bf = logBodyFat.toDoubleOrNull() ?: 0.0
                        val cal = logBurnedCalories.toDoubleOrNull() ?: 0.0
                        if (wt > 0) {
                            onAddLog(wt, bf, cal, logNote)
                            // reset
                            logWeight = ""
                            logBodyFat = ""
                            logBurnedCalories = ""
                            logNote = ""
                            showLogDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Apply Log", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PR Log Dialog
    if (showPrDialog) {
        AlertDialog(
            onDismissRequest = { showPrDialog = false },
            title = { Text("Record Personal Milestone", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = prName,
                        onValueChange = { prName = it },
                        label = { Text("Exercise name (e.g. Bench Press)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonLime),
                        modifier = Modifier.testTag("input_pr_name")
                    )
                    OutlinedTextField(
                        value = prWeight,
                        onValueChange = { prWeight = it },
                        label = { Text("Weight load (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonLime),
                        modifier = Modifier.testTag("input_pr_weight")
                    )
                    OutlinedTextField(
                        value = prReps,
                        onValueChange = { prReps = it },
                        label = { Text("Repetitions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonLime),
                        modifier = Modifier.testTag("input_pr_reps")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = prWeight.toDoubleOrNull() ?: 0.0
                        val rp = prReps.toIntOrNull() ?: 0
                        if (prName.isNotBlank() && wt >= 0 && rp >= 0) {
                            onAddPR(prName, wt, rp)
                            // reset
                            prName = ""
                            prWeight = ""
                            prReps = ""
                            showPrDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color(0xFF101416))
                ) {
                    Text("Record PR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryRowItem(
    log: ProgressLogEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (log.weight > 0) {
                        Text(
                            text = "${log.weight} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    if (log.bmi > 0) {
                        Text(
                            text = "BMI: ${String.format("%.1f", log.bmi)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricBlue
                        )
                    }
                    if (log.bodyFat > 0) {
                        Text(
                            text = "Fat: ${log.bodyFat}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SportyOrange
                        )
                    }
                }
                if (log.caloriesBurned > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔥 Burned ${log.caloriesBurned.toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (log.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 14.sp
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete log history",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PrRowItem(
    pr: PersonalRecordEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NeonLime.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pr.exerciseName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Achieved: ${pr.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pr.weight} kg x ${pr.reps}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete personal record",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    unlocked: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (unlocked) activeColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) activeColor.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (unlocked) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (unlocked) activeColor else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) Color.White else Color.White.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontSize = 10.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = if (unlocked) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WearableStatItem(
    name: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.08f), CircleShape)
                .border(BorderStroke(1.dp, tint.copy(alpha = 0.2f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 9.sp
        )
    }
}
