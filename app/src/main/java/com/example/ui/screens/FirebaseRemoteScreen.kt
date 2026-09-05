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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.FirebaseSyncStatus
import com.example.model.DownloadLink
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftPanel
import com.example.ui.components.MinecraftText

@Composable
fun FirebaseRemoteScreen(
    uiState: MainUiState,
    viewModel: BetterCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var serverMessage by remember(uiState.config.serverMessage) { mutableStateOf(uiState.config.serverMessage) }
    var linksList by remember(uiState.config.featuredLinks) { mutableStateOf(uiState.config.featuredLinks) }

    var newLinkTitle by remember { mutableStateOf("") }
    var newLinkUrl by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Card
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MinecraftPalette.DiamondBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MinecraftText(
                                text = "CONTROLE REMOTO FIREBASE",
                                fontSize = 13,
                                color = MinecraftPalette.DiamondBlue
                            )
                        }

                        when (val status = uiState.firebaseStatus) {
                            is FirebaseSyncStatus.Connected -> {
                                MinecraftBadge(text = "CONECTADO", color = MinecraftPalette.EmeraldGreen)
                            }
                            is FirebaseSyncStatus.Syncing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MinecraftPalette.GoldYellow,
                                    strokeWidth = 2.dp
                                )
                            }
                            is FirebaseSyncStatus.Offline -> {
                                MinecraftBadge(text = "MODO LOCAL", color = MinecraftPalette.GoldYellow)
                            }
                            is FirebaseSyncStatus.Error -> {
                                MinecraftBadge(text = "ERRO", color = MinecraftPalette.RedstoneRed)
                            }
                            FirebaseSyncStatus.Idle -> {
                                MinecraftBadge(text = "STANDBY", color = Color.Gray)
                            }
                        }
                    }

                    // Status details description
                    val statusText = when (val s = uiState.firebaseStatus) {
                        is FirebaseSyncStatus.Connected -> "${s.message} (Última sync: ${s.lastSyncText})"
                        is FirebaseSyncStatus.Offline -> s.reason
                        is FirebaseSyncStatus.Error -> s.error
                        is FirebaseSyncStatus.Syncing -> "Comunicando com o Cloud Firestore..."
                        FirebaseSyncStatus.Idle -> "Conexão com Firestore pronta para consulta ou publicação."
                    }

                    MinecraftText(
                        text = statusText,
                        fontSize = 11,
                        color = Color.LightGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            text = "SINCRONIZAR",
                            onClick = { viewModel.syncFromFirebase() },
                            icon = Icons.Default.Sync,
                            modifier = Modifier.weight(1f)
                        )

                        MinecraftButton(
                            text = "PUBLICAR NO FIREBASE",
                            onClick = {
                                val updatedConfig = uiState.config.copy(
                                    serverMessage = serverMessage,
                                    featuredLinks = linksList
                                )
                                viewModel.publishToFirebase(updatedConfig)
                                Toast.makeText(context, "Publicando no Firestore...", Toast.LENGTH_SHORT).show()
                            },
                            isActionGreen = true,
                            icon = Icons.Default.CloudUpload,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Global Announcement Message
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "MENSAGEM REMOTA / AVISO DO JOGO",
                            fontSize = 12,
                            color = Color.White
                        )
                    }

                    MinecraftInputField(
                        value = serverMessage,
                        onValueChange = { serverMessage = it },
                        placeholder = "Mensagem ou aviso para os jogadores..."
                    )
                }
            }
        }

        // Featured Installation Links Section
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MinecraftPalette.EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftText(
                                text = "LINKS DE INSTALAÇÃO NO JOGO",
                                fontSize = 12,
                                color = MinecraftPalette.EmeraldGreen
                            )
                        }

                        IconButton(
                            onClick = { showAddDialog = !showAddDialog },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar Link",
                                tint = MinecraftPalette.EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (showAddDialog) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinecraftPalette.PanelInner)
                                .border(1.dp, MinecraftPalette.EmeraldGreen)
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MinecraftText(text = "NOVO LINK DE INSTALAÇÃO", fontSize = 11, color = MinecraftPalette.EmeraldGreen)
                                MinecraftInputField(
                                    value = newLinkTitle,
                                    onValueChange = { newLinkTitle = it },
                                    placeholder = "Título (ex: Luanti v5.17 Dev)"
                                )
                                MinecraftInputField(
                                    value = newLinkUrl,
                                    onValueChange = { newLinkUrl = it },
                                    placeholder = "URL (https://github.com/...)"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    MinecraftButton(
                                        text = "CANCELAR",
                                        onClick = { showAddDialog = false }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MinecraftButton(
                                        text = "INCLUIR",
                                        isActionGreen = true,
                                        onClick = {
                                            if (newLinkTitle.isNotBlank() && newLinkUrl.isNotBlank()) {
                                                val newLink = DownloadLink(
                                                    id = "link_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}",
                                                    title = newLinkTitle.trim(),
                                                    url = newLinkUrl.trim(),
                                                    description = "Link personalizado adicionado pelo administrador."
                                                )
                                                linksList = linksList + newLink
                                                newLinkTitle = ""
                                                newLinkUrl = ""
                                                showAddDialog = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Links list items
        items(linksList.distinctBy { it.id }, key = { it.id }) { link ->
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinecraftBadge(
                                text = link.category.uppercase(),
                                color = if (link.category == "Engine") MinecraftPalette.EmeraldGreen else MinecraftPalette.DiamondBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftText(
                                text = link.title,
                                fontSize = 12,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        MinecraftText(
                            text = link.url,
                            fontSize = 10,
                            color = Color.LightGray
                        )
                        if (link.description.isNotBlank()) {
                            MinecraftText(
                                text = link.description,
                                fontSize = 9,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            linksList = linksList.filter { it.id != link.id }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            MinecraftText(
                text = "Dica: Ao clicar em 'Publicar no Firebase', os links e as variáveis de repositório são gravadas no Firestore na coleção 'bettercraft_config/global_installer'. Qualquer usuário do app receberá as atualizações remotamente.",
                fontSize = 10,
                color = Color.DarkGray
            )
        }
    }
}
