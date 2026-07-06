package com.calorai.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorai.app.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calorieBias by viewModel.calorieBias.collectAsStateWithLifecycle()
    val biometricForm by viewModel.biometricForm.collectAsStateWithLifecycle()

    var showGoalsDialog by remember { mutableStateOf(false) }
    var showManualGoalDialog by remember { mutableStateOf(false) }
    var showBiasDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var manualGoalValue by remember { mutableStateOf("") }
    var nameValue by remember { mutableStateOf("") }

    val displayName = uiState.profile?.displayName?.takeIf { it.isNotBlank() }
    val shownName = displayName ?: uiState.profile?.email?.substringBefore("@")

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = OnSurface
            ),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Avatar header ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val initials = (displayName ?: uiState.profile?.email)?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(OrangeContainer),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 26.sp
                        )
                    )
                }
            }
            Column {
                Text(
                    text = shownName ?: "—",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.profile?.email ?: "—",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── My Account ────────────────────────────────────────────────────────
        SectionLabel("My Account")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Outlined.Person,
                label = "Name",
                value = displayName ?: "Set name",
                valueColor = if (displayName != null) OnSurfaceVariant else OnSurfaceDim,
                onClick = {
                    nameValue = displayName ?: ""
                    showNameDialog = true
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Email,
                label = "Email Address",
                value = uiState.profile?.email,
                onClick = null
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Lock,
                label = "Change Password",
                value = "Coming soon",
                showChevron = false,
                valueColor = OnSurfaceDim,
                onClick = null
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── My Goals ──────────────────────────────────────────────────────────
        SectionLabel("My Goals")
        SettingsGroup {
            // Calorie goal row — shows calculated value, tapping opens biometrics dialog
            val profile = uiState.profile
            val hasAllBiometrics = listOf(
                profile?.heightCm, profile?.age, profile?.sex,
                profile?.currentWeightKg, profile?.activityLevel
            ).all { it != null }

            SettingsRow(
                icon = Icons.Outlined.LocalFireDepartment,
                iconTint = OrangeAccent,
                label = "Daily Calorie Goal",
                value = "${profile?.calorieGoal ?: 2000} kcal" + if (hasAllBiometrics) " · Auto" else "",
                onClick = {
                    manualGoalValue = (profile?.calorieGoal ?: 2000).toString()
                    showManualGoalDialog = true
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.MonitorWeight,
                label = "Body & Goal Details",
                value = if (hasAllBiometrics) "Set" else "Not set",
                valueColor = if (hasAllBiometrics) Color(0xFF66BB6A) else OnSurfaceVariant,
                onClick = { showGoalsDialog = true }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Tune,
                label = "Estimate Bias",
                value = when {
                    calorieBias < -0.05f -> "Under ${(calorieBias * 100).toInt()}%"
                    calorieBias > 0.05f -> "Over +${(calorieBias * 100).toInt()}%"
                    else -> "Balanced"
                },
                onClick = { showBiasDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Subscription ──────────────────────────────────────────────────────
        SectionLabel("Subscription")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Outlined.Star,
                iconTint = Color(0xFFFFC107),
                label = "CalorAI Premium",
                value = "Free Plan",
                showChevron = false,
                valueColor = OnSurfaceDim,
                onClick = null
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.CardGiftcard,
                label = "Restore Purchase",
                value = "Coming soon",
                showChevron = false,
                valueColor = OnSurfaceDim,
                onClick = null
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── App ───────────────────────────────────────────────────────────────
        SectionLabel("App")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                label = "Notifications",
                value = "Coming soon",
                showChevron = false,
                valueColor = OnSurfaceDim,
                onClick = null
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Info,
                label = "About CalorAI",
                value = "v1.0.0",
                showChevron = false,
                valueColor = OnSurfaceDim,
                onClick = null
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Sign Out ──────────────────────────────────────────────────────────
        SettingsGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.logout(); onLogout() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Success toast
        AnimatedVisibility(visible = uiState.successMessage != null, enter = fadeIn(), exit = fadeOut()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeContainer)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uiState.successMessage ?: "", style = MaterialTheme.typography.bodyMedium.copy(color = OrangeAccent))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Body & Goals Dialog ───────────────────────────────────────────────────
    if (showGoalsDialog) {
        GoalsDialog(
            form = biometricForm,
            isSaving = uiState.isSaving,
            onFormChange = viewModel::updateForm,
            onSave = { viewModel.saveBiometrics(); showGoalsDialog = false },
            onDismiss = { showGoalsDialog = false }
        )
    }

    // ── Name Dialog ───────────────────────────────────────────────────────────
    if (showNameDialog) {
        NameDialog(
            value = nameValue,
            isSaving = uiState.isSaving,
            onValueChange = { nameValue = it },
            onConfirm = {
                viewModel.saveDisplayName(nameValue)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    // ── Manual Goal Dialog ────────────────────────────────────────────────────
    if (showManualGoalDialog) {
        ManualGoalDialog(
            value = manualGoalValue,
            onValueChange = { manualGoalValue = it },
            onConfirm = {
                manualGoalValue.toIntOrNull()?.let { viewModel.saveManualCalorieGoal(it) }
                showManualGoalDialog = false
            },
            onDismiss = { showManualGoalDialog = false }
        )
    }

    // ── Bias Dialog ───────────────────────────────────────────────────────────
    if (showBiasDialog) {
        BiasDialog(
            bias = calorieBias,
            onBiasChange = viewModel::setCalorieBias,
            onDismiss = { showBiasDialog = false }
        )
    }
}

// ── Settings components ───────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp, fontSize = 11.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        elevation = CardDefaults.cardElevation(0.dp),
        content = { Column(content = content) }
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = Surface2, modifier = Modifier.padding(start = 52.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: (() -> Unit)?,
    iconTint: Color = OnSurfaceVariant,
    showChevron: Boolean = true,
    valueColor: Color = OnSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(text = value, style = MaterialTheme.typography.bodySmall.copy(color = valueColor))
        }
        if (onClick != null && showChevron) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceDim, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Body & Goals Dialog ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalsDialog(
    form: BiometricForm,
    isSaving: Boolean,
    onFormChange: (BiometricForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val calculatedGoal = form.calculatedGoal

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    "Body & Goal Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
                Text(
                    "Your daily calorie goal is calculated automatically.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sex selector
                FormLabel("Biological Sex")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("male" to "Male", "female" to "Female", "other" to "Other").forEach { (key, label) ->
                        val selected = form.sex == key
                        FilterChip(
                            selected = selected,
                            onClick = { onFormChange(form.copy(sex = key)) },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeContainer,
                                selectedLabelColor = OrangeAccent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Height + Age row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Height (cm)")
                        FormField(
                            value = form.heightCm,
                            placeholder = "e.g. 175",
                            onValueChange = { onFormChange(form.copy(heightCm = it)) },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Age")
                        FormField(
                            value = form.age,
                            placeholder = "e.g. 24",
                            onValueChange = { onFormChange(form.copy(age = it)) },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current + Goal weight row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Current Weight (kg)")
                        FormField(
                            value = form.currentWeightKg,
                            placeholder = "e.g. 80",
                            onValueChange = { onFormChange(form.copy(currentWeightKg = it)) },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Goal Weight (kg)")
                        FormField(
                            value = form.goalWeightKg,
                            placeholder = "e.g. 72",
                            onValueChange = { onFormChange(form.copy(goalWeightKg = it)) },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weekly goal slider
                val weeklySteps = listOf(-1.0f, -0.75f, -0.5f, -0.25f, 0f, 0.25f, 0.5f, 0.75f, 1.0f)
                val weeklyValue = form.weeklyGoalKg.toFloatOrNull() ?: 0f
                val weeklyIndex = weeklySteps.indexOfFirst { kotlin.math.abs(it - weeklyValue) < 0.01f }.takeIf { it >= 0 } ?: 4
                val weeklyDisplayText = when {
                    weeklyValue == 0f -> "Maintain"
                    weeklyValue > 0f -> "+${weeklyValue} kg / week"
                    else -> "${weeklyValue} kg / week"
                }
                val weeklySubLabel = when {
                    weeklyValue == 0f -> "Keep current weight"
                    weeklyValue > 0f -> "Weight gain"
                    else -> "Weight loss"
                }

                FormLabel("Weekly Goal")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = weeklyDisplayText,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = weeklySubLabel,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = weeklyIndex.toFloat(),
                    onValueChange = { v ->
                        val idx = v.toInt().coerceIn(0, weeklySteps.lastIndex)
                        onFormChange(form.copy(weeklyGoalKg = weeklySteps[idx].toString()))
                    },
                    valueRange = 0f..(weeklySteps.lastIndex.toFloat()),
                    steps = weeklySteps.lastIndex - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangeAccent,
                        activeTrackColor = OrangeAccent,
                        inactiveTrackColor = Surface3
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent)
                                .border(3.dp, OnSurface.copy(alpha = 0.08f), CircleShape)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lose", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
                    Text("Maintain", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
                    Text("Gain", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity level — 3 toggle buttons
                FormLabel("Activity Level")
                Spacer(modifier = Modifier.height(4.dp))
                val activityOptions = listOf(
                    "light" to "Lightly\nActive",
                    "moderate" to "Moderately\nActive",
                    "very_active" to "Very\nActive"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activityOptions.forEach { (key, label) ->
                        val selected = form.activityLevel == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) OrangeContainer else Surface2)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) OrangeAccent else Surface3,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onFormChange(form.copy(activityLevel = key)) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected) OrangeAccent else OnSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Calculated goal preview
                if (calculatedGoal != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeContainer)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Calculated daily goal",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent.copy(alpha = 0.7f))
                                )
                                Text(
                                    "$calculatedGoal kcal / day",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OrangeAccent, fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Icon(Icons.Outlined.LocalFireDepartment, null, tint = OrangeAccent, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Surface3)
                    ) {
                        Text("Cancel", color = OnSurfaceVariant)
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Name Dialog ─────────────────────────────────────────────────────────────

@Composable
private fun NameDialog(
    value: String,
    isSaving: Boolean,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Your Name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "This is how we'll greet you on the home screen.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.length <= 30) onValueChange(it) },
                    singleLine = true,
                    placeholder = { Text("e.g. Dino", color = OnSurfaceDim) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (value.isNotBlank()) onConfirm() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Surface3)
                    ) { Text("Cancel", color = OnSurfaceVariant) }
                    Button(
                        onClick = onConfirm, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        enabled = value.isNotBlank() && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Manual Goal Dialog ────────────────────────────────────────────────────────

@Composable
private fun ManualGoalDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Daily Calorie Goal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Override the calculated goal with a manual value.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) onValueChange(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                    suffix = { Text("kcal", color = OnSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Surface3)
                    ) { Text("Cancel", color = OnSurfaceVariant) }
                    Button(
                        onClick = onConfirm, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) { Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// ── Bias Dialog ───────────────────────────────────────────────────────────────

@Composable
private fun BiasDialog(bias: Float, onBiasChange: (Float) -> Unit, onDismiss: () -> Unit) {
    val steps = listOf(-0.20f, -0.10f, 0f, 0.10f, 0.20f)
    val labels = listOf("Under more", "Under", "Balanced", "Over", "Over more")
    val stepIndex = steps.indexOfFirst { kotlin.math.abs(it - bias) < 0.05f }.takeIf { it >= 0 } ?: 2
    val biasColor = when { bias < -0.05f -> Color(0xFF66BB6A); bias > 0.05f -> Color(0xFFFF7043); else -> OrangeAccent }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Calorie Estimate Bias", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Adjust estimates up or down. Useful when cutting or bulking.", style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant))
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(labels[stepIndex], style = MaterialTheme.typography.bodyMedium.copy(color = biasColor, fontWeight = FontWeight.SemiBold))
                    Text(if (bias == 0f) "±0%" else "${if (bias > 0) "+" else ""}${(bias * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(color = biasColor, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = stepIndex.toFloat(),
                    onValueChange = { v -> onBiasChange(steps[v.toInt().coerceIn(0, steps.lastIndex)]) },
                    valueRange = 0f..4f, steps = 3,
                    colors = SliderDefaults.colors(thumbColor = biasColor, activeTrackColor = biasColor, inactiveTrackColor = Surface3),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Under more", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
                    Text("Over more", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceDim, fontSize = 10.sp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant, fontSize = 11.sp),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun FormField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OnSurfaceDim, fontSize = 14.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = outlinedFieldColors()
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OrangeAccent,
    unfocusedBorderColor = Surface3,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    cursorColor = OrangeAccent,
    focusedContainerColor = Surface2,
    unfocusedContainerColor = Surface2
)
