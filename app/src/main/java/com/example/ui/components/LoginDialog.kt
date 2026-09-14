package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.AdminConstants

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginFirebase: (email: String, password: String) -> Unit,
    onLoginDirect: (email: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf(AdminConstants.SUPER_ADMIN_EMAIL) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MinecraftPalette.ButtonBorderBlack)
                .padding(2.dp)
                .border(2.dp, MinecraftPalette.ButtonStoneHighlight)
                .background(MinecraftPalette.PanelBackground)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        MinecraftText(
                            text = "LOGIN FIREBASE",
                            fontSize = 14,
                            fontWeight = FontWeight.Bold,
                            color = MinecraftPalette.GoldYellow
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                MinecraftText(
                    text = "Acesso exclusivo aos módulos administrativos, configurações remotas e controle de mods.",
                    fontSize = 11,
                    color = Color.LightGray
                )

                // Error alert
                if (!errorMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1515))
                            .border(1.dp, MinecraftPalette.RedstoneRed)
                            .padding(8.dp)
                    ) {
                        MinecraftText(
                            text = errorMessage,
                            fontSize = 11,
                            color = Color(0xFFFFB4AB)
                        )
                    }
                }

                // Email Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MinecraftPalette.DiamondBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "E-MAIL DO ADMINISTRADOR",
                            fontSize = 11,
                            color = MinecraftPalette.DiamondBlue
                        )
                    }
                    MinecraftInputField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "seuemail@exemplo.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                // Password Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "SENHA FIREBASE",
                            fontSize = 11,
                            color = MinecraftPalette.GoldYellow
                        )
                    }
                    MinecraftInputField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Digite sua senha",
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar visibilidade",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Primary Action: Firebase Auth
                MinecraftButton(
                    text = if (isLoading) "AUTENTICANDO FIREBASE..." else "ENTRAR COM FIREBASE",
                    onClick = {
                        onLoginFirebase(email.trim(), password)
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    isActionGreen = true,
                    icon = Icons.Default.Cloud,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_firebase_login"
                )

                // Quick Owner Access (Jean Pierre)
                MinecraftButton(
                    text = "ACESSO DIRETO SUPER ADMIN (JEAN PIERRE)",
                    onClick = {
                        onLoginDirect(email.trim())
                    },
                    enabled = !isLoading,
                    icon = Icons.Default.VerifiedUser,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_owner_direct_login"
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MinecraftPalette.EmeraldGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

