package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel

@Composable
fun LoginScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBootstrapMode by remember { mutableStateOf(false) }
    var bootstrapDisplayName by remember { mutableStateOf("Initial Super Admin") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, GeoCardBorder, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = GeoSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Logo Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GeoPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GeoOnPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROYAL CONSOLE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (isBootstrapMode) "Super Admin Initialization" else "Geometric Balance Console",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                HorizontalDivider(color = GeoOutlineVariant)

                // Error Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { error ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(GeoRoseContainer)
                                .border(1.dp, GeoRose.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = GeoRoseText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = error,
                                    fontSize = 12.sp,
                                    color = GeoRoseText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Input Fields
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Admin Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeoPrimary,
                        unfocusedBorderColor = GeoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GeoPrimary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeoPrimary,
                        unfocusedBorderColor = GeoCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GeoPrimary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                if (isBootstrapMode) {
                    OutlinedTextField(
                        value = bootstrapDisplayName,
                        onValueChange = { bootstrapDisplayName = it },
                        label = { Text("Super Admin Display Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                // Action Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter your email and password."
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        if (isBootstrapMode) {
                            viewModel.bootstrapSuperAdmin(email, password, bootstrapDisplayName) { success, err ->
                                isSubmitting = false
                                if (!success) errorMessage = err
                            }
                        } else {
                            viewModel.signIn(email, password) { success, err ->
                                isSubmitting = false
                                if (!success) errorMessage = err
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isBootstrapMode) "Bootstrap Super Admin" else "Sign In to Console",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mode Toggle & Security Info
                TextButton(
                    onClick = {
                        isBootstrapMode = !isBootstrapMode
                        errorMessage = null
                    }
                ) {
                    Text(
                        text = if (isBootstrapMode) "← Back to Regular Sign In" else "First-time setup? Initialize Super Admin",
                        fontSize = 12.sp,
                        color = GeoPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Security Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GeoSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🔒 Direct Firebase Auth & Firestore role verification active.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

