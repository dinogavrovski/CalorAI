package com.calorai.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = OnSurface))
            IconButton(
                onClick = { viewModel.toggleEdit() },
                modifier = Modifier.clip(CircleShape).background(if (uiState.isEditing) GreenContainer else Surface2).size(40.dp)
            ) {
                Icon(
                    if (uiState.isEditing) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = if (uiState.isEditing) Green400 else OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(8.dp).background(
                    Brush.horizontalGradient(listOf(Green400.copy(alpha = 0.7f), Green300.copy(alpha = 0.3f)))
                )
            )
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val initials = uiState.profile?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(GreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, style = MaterialTheme.typography.titleLarge.copy(color = Green400, fontWeight = FontWeight.Bold, fontSize = 28.sp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Green400, modifier = Modifier.size(24.dp))
                } else {
                    Text(uiState.profile?.email?.substringBefore("@") ?: "—", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OnSurface))
                    Text(uiState.profile?.email ?: "—", style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isEditing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Edit Profile", style = MaterialTheme.typography.titleSmall.copy(color = OnSurface, fontWeight = FontWeight.SemiBold))

                    OutlinedTextField(
                        value = uiState.editCalorieGoal,
                        onValueChange = viewModel::updateCalorieGoal,
                        label = { Text("Daily Calorie Goal") },
                        leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = OnSurfaceVariant) },
                        suffix = { Text("kcal", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green400, unfocusedBorderColor = Surface3,
                            focusedLabelColor = Green400, unfocusedLabelColor = OnSurfaceVariant,
                            cursorColor = Green400, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface,
                            focusedContainerColor = Surface2, unfocusedContainerColor = Surface1
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        Text(uiState.errorMessage ?: "", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
                    }

                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Green400, contentColor = Color(0xFF003314))
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF003314), strokeWidth = 2.dp)
                        } else {
                            Text("Save Changes", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface1),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = uiState.profile?.email ?: "—")
                    HorizontalDivider(color = Surface3, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileInfoRow(icon = Icons.Default.LocalFireDepartment, label = "Daily Goal", value = "${uiState.profile?.calorieGoal ?: 2000} kcal")
                }
            }
        }

        AnimatedVisibility(visible = uiState.successMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = GreenContainer)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Green400, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uiState.successMessage ?: "", style = MaterialTheme.typography.bodyMedium.copy(color = Green300))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { viewModel.logout(); onLogout() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Green400, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface, fontWeight = FontWeight.Medium))
        }
    }
}
