package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AdminConstants
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftPanel
import com.example.ui.components.MinecraftText

@Composable
fun AdminUsersScreen(
    uiState: MainUiState,
    viewModel: BetterCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newAdminEmail by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MinecraftPalette.GoldYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        MinecraftText(
                            text = "GERENCIAMENTO DE ADMINISTRADORES",
                            fontSize = 13,
                            fontWeight = FontWeight.Bold,
                            color = MinecraftPalette.GoldYellow
                        )
                        MinecraftText(
                            text = "Controle quem tem acesso ao site, configurações e Firebase",
                            fontSize = 10,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Current Session Status
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (uiState.isSuperAdmin) MinecraftPalette.GoldYellow else MinecraftPalette.EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        MinecraftText(
                            text = "Sua Sessão Atual:",
                            fontSize = 10,
                            color = Color.Gray
                        )
                        MinecraftText(
                            text = uiState.currentUser?.email ?: "Não autenticado",
                            fontSize = 12,
                            color = Color.White
                        )
                    }
                    MinecraftBadge(
                        text = if (uiState.isSuperAdmin) "SUPERADMIN" else "ADMIN",
                        color = if (uiState.isSuperAdmin) MinecraftPalette.GoldYellow else MinecraftPalette.EmeraldGreen
                    )
                }
            }
        }

        // Add Admin Section
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MinecraftPalette.EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "CONCEDER PERMISSÃO DE ADMIN",
                            fontSize = 12,
                            color = MinecraftPalette.EmeraldGreen
                        )
                    }

                    MinecraftInputField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it },
                        placeholder = "email_do_novo_admin@gmail.com"
                    )

                    MinecraftButton(
                        text = "ADICIONAR COMO ADMINISTRADOR",
                        onClick = {
                            val email = newAdminEmail.trim()
                            if (email.contains("@") && email.contains(".")) {
                                viewModel.addAdmin(email)
                                newAdminEmail = ""
                                Toast.makeText(context, "Administrador adicionado!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Insira um e-mail válido!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isActionGreen = true,
                        icon = Icons.Default.PersonAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Admins List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MinecraftText(
                    text = "ADMINISTRADORES CADASTRADOS (${uiState.config.adminEmails.size})",
                    fontSize = 11,
                    color = MinecraftPalette.IronGray
                )

                MinecraftButton(
                    text = "SYNC FIREBASE",
                    onClick = {
                        viewModel.publishToFirebase(uiState.config)
                        Toast.makeText(context, "Sincronizando admins no Firebase...", Toast.LENGTH_SHORT).show()
                    },
                    icon = Icons.Default.Sync,
                    modifier = Modifier.height(34.dp)
                )
            }
        }

        // Admin Items
        items(uiState.config.adminEmails.distinct(), key = { it }) { email ->
            val isOwner = email.equals(AdminConstants.SUPER_ADMIN_EMAIL, ignoreCase = true)
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOwner) Icons.Default.Star else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isOwner) MinecraftPalette.GoldYellow else MinecraftPalette.DiamondBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinecraftText(
                                text = email,
                                fontSize = 12,
                                color = Color.White
                            )
                            if (isOwner) {
                                Spacer(modifier = Modifier.width(6.dp))
                                MinecraftBadge(text = "PROPRIETÁRIO", color = MinecraftPalette.GoldYellow)
                            }
                        }
                        MinecraftText(
                            text = if (isOwner) "Acesso irrestrito a todos os módulos" else "Acesso a site, configurações e controle remoto",
                            fontSize = 9,
                            color = Color.LightGray
                        )
                    }

                    if (!isOwner) {
                        IconButton(
                            onClick = {
                                viewModel.removeAdmin(email)
                                Toast.makeText(context, "Admin removido!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover admin",
                                tint = MinecraftPalette.RedstoneRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            MinecraftText(
                text = "Nota: Por motivos de segurança, a conta não permanece logada ao fechar o app. Administradores realizam login a cada sessão através do Google.",
                fontSize = 10,
                color = Color.DarkGray
            )
        }
    }
}
