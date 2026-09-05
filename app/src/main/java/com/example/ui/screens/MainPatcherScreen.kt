package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.LogLevel
import com.example.model.PatchStep
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftPanel
import com.example.ui.components.MinecraftText
import com.example.ui.components.MinecraftXpBar

@Composable
fun MainPatcherScreen(
    uiState: MainUiState,
    viewModel: BetterCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-scroll logs to bottom when new logs arrive
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            try {
                listState.animateScrollToItem(uiState.logs.size - 1)
            } catch (t: Throwable) {
                // Ignore scroll cancellation or layout interruptions
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(2.dp, MinecraftPalette.ButtonBorderBlack)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Faithful Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    MinecraftBadge(
                        text = "FAITHFUL 32x32 EDITION",
                        color = MinecraftPalette.DiamondBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MinecraftText(
                        text = "BETTERCRAFT BUILDER",
                        fontSize = 18,
                        fontWeight = FontWeight.ExtraBold,
                        color = MinecraftPalette.GoldYellow
                    )
                    MinecraftText(
                        text = "Luanti APK Patcher, Mod Injector & Signer",
                        fontSize = 12,
                        color = MinecraftPalette.IronGray
                    )
                }
            }
        }

        // Target Sources Card
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "FONTES DE MONTAGEM",
                            fontSize = 13,
                            color = MinecraftPalette.GoldYellow
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MinecraftPalette.PanelInner)
                            .border(1.dp, Color(0xFF33353A))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MinecraftBadge(text = "APK", color = MinecraftPalette.EmeraldGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinecraftText(
                                    text = uiState.config.luantiApkUrl.substringAfterLast('/'),
                                    fontSize = 11,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MinecraftBadge(text = "ZIP", color = MinecraftPalette.DiamondBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinecraftText(
                                    text = uiState.config.bettercraftZipUrl.substringAfterLast('/'),
                                    fontSize = 11,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MinecraftBadge(text = "DESTINO", color = MinecraftPalette.GoldYellow)
                                Spacer(modifier = Modifier.width(6.dp))
                                MinecraftText(
                                    text = uiState.config.targetAssetsPath,
                                    fontSize = 11,
                                    color = Color.LightGray
                                )
                            }
                            if (uiState.detectedEnvironment.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MinecraftBadge(text = "AMBIENTE", color = MinecraftPalette.DiamondBlue)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MinecraftText(
                                        text = uiState.detectedEnvironment,
                                        fontSize = 10,
                                        color = MinecraftPalette.GoldYellow
                                    )
                                }
                            }
                            if (uiState.detectedStoragePath.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MinecraftBadge(text = "RAIZ", color = MinecraftPalette.EmeraldGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MinecraftText(
                                        text = uiState.detectedStoragePath,
                                        fontSize = 10,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons / Active Status Card
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (val step = uiState.patchStep) {
                        is PatchStep.Idle -> {
                            MinecraftButton(
                                text = "BAIXAR, INJETAR & ASSINAR APK",
                                onClick = { viewModel.startPatchProcess() },
                                isActionGreen = true,
                                icon = Icons.Default.CloudDownload,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_start_patch"
                            )
                        }

                        is PatchStep.DownloadingApk -> {
                            MinecraftXpBar(
                                progress = step.progress,
                                label = "Baixando Luanti Engine APK (${formatBytes(step.currentBytes)} / ${formatBytes(step.totalBytes)})"
                            )
                            MinecraftButton(
                                text = "CANCELAR MONTAGEM",
                                onClick = { viewModel.cancelPatch() },
                                icon = Icons.Default.Cancel,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is PatchStep.DownloadingZip -> {
                            MinecraftXpBar(
                                progress = step.progress,
                                label = "Baixando BetterCraft Zip (${formatBytes(step.currentBytes)} / ${formatBytes(step.totalBytes)})",
                                activeColor = MinecraftPalette.DiamondBlue
                            )
                            MinecraftButton(
                                text = "CANCELAR MONTAGEM",
                                onClick = { viewModel.cancelPatch() },
                                icon = Icons.Default.Cancel,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is PatchStep.ExtractingAndInjecting -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MinecraftPalette.GoldYellow,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MinecraftText(
                                        text = "Injetando em assets: ${step.processedCount} arquivos...",
                                        fontSize = 12,
                                        color = MinecraftPalette.GoldYellow
                                    )
                                }
                                MinecraftText(
                                    text = step.currentFile,
                                    fontSize = 10,
                                    color = Color.LightGray
                                )
                            }
                        }

                        is PatchStep.SigningApk -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MinecraftPalette.EmeraldGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                MinecraftText(
                                    text = step.stepDetail,
                                    fontSize = 12,
                                    color = MinecraftPalette.EmeraldGreen
                                )
                            }
                        }

                        is PatchStep.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MinecraftPalette.EmeraldGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MinecraftText(
                                        text = "APK PRONTO E ASSINADO!",
                                        fontSize = 14,
                                        color = MinecraftPalette.EmeraldGreen
                                    )
                                }
                                MinecraftText(
                                    text = "Arquivo: ${step.signedApkFile.name} (${formatBytes(step.sizeBytes)})",
                                    fontSize = 11,
                                    color = Color.White
                                )

                                MinecraftButton(
                                    text = "INSTALAR APK NO DISPOSITIVO",
                                    onClick = { viewModel.installApk(context, step.signedApkFile) },
                                    isActionGreen = true,
                                    icon = Icons.Default.InstallMobile,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_install_apk"
                                )

                                MinecraftButton(
                                    text = "ABRIR LUANTI / BETTERCRAFT",
                                    onClick = { viewModel.launchLuantiApp(context) },
                                    icon = Icons.Default.PlayArrow,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_launch_luanti"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MinecraftButton(
                                        text = "COMPARTILHAR",
                                        onClick = { viewModel.shareApk(context, step.signedApkFile) },
                                        icon = Icons.Default.Share,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MinecraftButton(
                                        text = "NOVA MONTAGEM",
                                        onClick = { viewModel.startPatchProcess() },
                                        icon = Icons.Default.Build,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        is PatchStep.Failed -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MinecraftPalette.RedstoneRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MinecraftText(
                                        text = "FALHA NA MONTAGEM",
                                        fontSize = 13,
                                        color = MinecraftPalette.RedstoneRed
                                    )
                                }
                                MinecraftText(
                                    text = step.errorMessage,
                                    fontSize = 11,
                                    color = Color(0xFFFFB4AB)
                                )
                                MinecraftButton(
                                    text = "TENTAR NOVAMENTE",
                                    onClick = { viewModel.startPatchProcess() },
                                    isActionGreen = true,
                                    icon = Icons.Default.Build,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Minecraft Terminal Console Logs
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinecraftBadge(text = "CONSOLE", color = MinecraftPalette.EmeraldGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftText(
                                text = "LOGS EM TEMPO REAL",
                                fontSize = 12,
                                color = MinecraftPalette.IronGray
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpar logs",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF0C0D0E))
                            .border(1.5.dp, Color(0xFF222428))
                            .padding(6.dp)
                    ) {
                        if (uiState.logs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                MinecraftText(
                                    text = "Nenhum log registrado.",
                                    fontSize = 11,
                                    color = Color.DarkGray
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(uiState.logs, key = { it.id }) { log ->
                                    val logColor = when (log.level) {
                                        LogLevel.SUCCESS -> MinecraftPalette.EmeraldGreen
                                        LogLevel.WARNING -> MinecraftPalette.GoldYellow
                                        LogLevel.ERROR -> MinecraftPalette.RedstoneRed
                                        LogLevel.PROGRESS -> MinecraftPalette.DiamondBlue
                                        LogLevel.INFO -> Color(0xFFC0C4CC)
                                    }
                                    Row {
                                        androidx.compose.material3.Text(
                                            text = "[${log.timestamp}] ",
                                            color = Color(0xFF6B7280),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        androidx.compose.material3.Text(
                                            text = log.message,
                                            color = logColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.2f GB".format(gb)
        mb >= 1.0 -> "%.2f MB".format(mb)
        kb >= 1.0 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}
