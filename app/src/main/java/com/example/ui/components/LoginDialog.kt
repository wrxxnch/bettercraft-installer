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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.AdminConstants
import com.example.ui.screens.MinecraftInputField

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginGoogle: () -> Unit,
    onLoginEmail: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var emailInput by remember { mutableStateOf("") }

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        MinecraftText(
                            text = "LOGIN ADMINISTRATIVO",
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
                    text = "Acesso exclusivo para administradores. Por segurança, a sessão não fica salva permanentemente.",
                    fontSize = 11,
                    color = Color.LightGray
                )

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

                // Google Sign In Button
                MinecraftButton(
                    text = if (isLoading) "CONECTANDO GOOGLE..." else "ENTRAR COM GOOGLE",
                    onClick = onLoginGoogle,
                    enabled = !isLoading,
                    isActionGreen = true,
                    icon = Icons.Default.Login,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_google_signin"
                )

                // Direct email option
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MinecraftText(
                        text = "OU ENTRE COM E-MAIL DE ADMINISTRADOR:",
                        fontSize = 10,
                        color = Color.Gray
                    )
                    MinecraftInputField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = "seu_email_admin@gmail.com"
                    )
                    MinecraftButton(
                        text = "ENTRAR COM ESTE E-MAIL",
                        onClick = {
                            if (emailInput.isNotBlank()) {
                                onLoginEmail(emailInput.trim())
                            }
                        },
                        enabled = emailInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Owner Quick Access Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E2413))
                        .border(1.dp, MinecraftPalette.GoldYellow)
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MinecraftPalette.GoldYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftText(
                                text = "ACESSO RÁPIDO DO OWNER",
                                fontSize = 11,
                                fontWeight = FontWeight.Bold,
                                color = MinecraftPalette.GoldYellow
                            )
                        }
                        MinecraftButton(
                            text = "ENTRAR COMO OWNER (JEAN)",
                            onClick = { onLoginEmail(AdminConstants.SUPER_ADMIN_EMAIL) },
                            enabled = !isLoading,
                            icon = Icons.Default.Star,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_login_owner"
                        )
                    }
                }

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
