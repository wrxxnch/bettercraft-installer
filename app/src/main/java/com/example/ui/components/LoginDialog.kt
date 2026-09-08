package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginGoogle: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
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
                    text = "Acesso exclusivo para administradores autenticados via Conta Google. Por segurança, a sessão não fica salva permanentemente.",
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
