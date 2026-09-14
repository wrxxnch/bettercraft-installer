package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppConfig
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftInputField
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftPanel
import com.example.ui.components.MinecraftText

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: BetterCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var luantiUrl by remember(uiState.config.luantiApkUrl) { mutableStateOf(uiState.config.luantiApkUrl) }
    var modZipUrl by remember(uiState.config.bettercraftZipUrl) { mutableStateOf(uiState.config.bettercraftZipUrl) }
    var assetsPath by remember(uiState.config.targetAssetsPath) { mutableStateOf(uiState.config.targetAssetsPath) }
    var webViewUrl by remember(uiState.config.webViewUrl) { mutableStateOf(uiState.config.webViewUrl) }
    var outputDirectoryPath by remember(uiState.config.outputDirectoryPath) { mutableStateOf(uiState.config.outputDirectoryPath) }
    var googleWebClientId by remember(uiState.config.googleWebClientId) { mutableStateOf(uiState.config.googleWebClientId) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MinecraftPalette.GoldYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MinecraftText(
                        text = "CONFIGURAÇÃO DOS REPOSITÓRIOS",
                        fontSize = 14,
                        color = MinecraftPalette.GoldYellow
                    )
                }
            }
        }

        // Quick Presets
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MinecraftPalette.DiamondBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "PRESETS RÁPIDOS",
                            fontSize = 12,
                            color = MinecraftPalette.DiamondBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MinecraftButton(
                            text = "ARM64 (PADRÃO)",
                            onClick = {
                                luantiUrl = "https://github.com/luanti-org/luanti/releases/download/5.17.0/luanti-5.17.0-arm64-v8a.apk"
                                modZipUrl = "https://github.com/wrxxnch/bettercraft/archive/refs/heads/main.zip"
                                assetsPath = "assets/games/bettercraft/"
                                Toast.makeText(context, "Preset ARM64 selecionado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MinecraftButton(
                            text = "ARMv7 (32-BIT)",
                            onClick = {
                                luantiUrl = "https://github.com/luanti-org/luanti/releases/download/5.17.0/luanti-5.17.0-armeabi-v7a.apk"
                                Toast.makeText(context, "Preset ARMv7 selecionado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MinecraftButton(
                        text = "ABRIR GITHUB RELEASES (BAIXAR APK)",
                        onClick = {
                            val releaseUrl = "https://github.com/wrxxnch/bettercraft/releases/latest"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        icon = Icons.Default.OpenInBrowser,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // URL Input 1: Luanti APK
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MinecraftBadge(text = "ENGINE BASE", color = MinecraftPalette.EmeraldGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "URL DO APK DO LUANTI (GITHUB)",
                            fontSize = 12,
                            color = Color.White
                        )
                    }

                    MinecraftInputField(
                        value = luantiUrl,
                        onValueChange = { luantiUrl = it },
                        placeholder = "https://github.com/.../luanti-xxx.apk"
                    )

                    MinecraftText(
                        text = "Padrão: Luanti 5.17.0 ARM64 do repositório oficial luanti-org/luanti",
                        fontSize = 10,
                        color = Color.Gray
                    )
                }
            }
        }

        // URL Input 2: BetterCraft Zip
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MinecraftBadge(text = "SUBGAME / MOD", color = MinecraftPalette.DiamondBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "URL DO REPOSITÓRIO GITHUB (.ZIP)",
                            fontSize = 12,
                            color = Color.White
                        )
                    }

                    MinecraftInputField(
                        value = modZipUrl,
                        onValueChange = { modZipUrl = it },
                        placeholder = "https://github.com/.../archive/.../main.zip"
                    )

                    MinecraftText(
                        text = "Padrão: wrxxnch/bettercraft (.zip da branch main)",
                        fontSize = 10,
                        color = Color.Gray
                    )
                }
            }
        }

        // URL Input 3: Website URL
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MinecraftPalette.EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "URL DO SITE WEBVIEW (EXCLUSIVO ADMIN)",
                            fontSize = 12,
                            color = Color.White
                        )
                    }

                    MinecraftInputField(
                        value = webViewUrl,
                        onValueChange = { webViewUrl = it },
                        placeholder = "https://wrxxnch.github.io/bettercraftsite"
                    )

                    MinecraftText(
                        text = "Padrão: https://wrxxnch.github.io/bettercraftsite (Acesso protegido por login)",
                        fontSize = 10,
                        color = Color.Gray
                    )
                }
            }
        }

        // Path Input: Output / Download Folder
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MinecraftPalette.DiamondBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "DIRETÓRIO DE DOWNLOAD / SAÍDA (PADRÃO)",
                            fontSize = 12,
                            color = MinecraftPalette.DiamondBlue
                        )
                    }

                    MinecraftInputField(
                        value = outputDirectoryPath,
                        onValueChange = { outputDirectoryPath = it },
                        placeholder = "/storage/emulated/0/Android/data/com.aistudio.bettercraft.vzkx/files/output/"
                    )

                    MinecraftText(
                        text = "Padrão: /storage/emulated/0/Android/data/com.aistudio.bettercraft.vzkx/files/output/",
                        fontSize = 10,
                        color = Color.LightGray
                    )

                    MinecraftButton(
                        text = "USAR DIRETÓRIO PADRÃO DO APP",
                        onClick = {
                            outputDirectoryPath = AppConfig.DEFAULT_OUTPUT_DIRECTORY_PATH
                            Toast.makeText(context, "Caminho padrão restaurado!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Path Input: Target Assets Folder
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "PASTA DE DESTINO DENTRO DO ZIP (ASSETS)",
                            fontSize = 12,
                            color = Color.White
                        )
                    }

                    MinecraftInputField(
                        value = assetsPath,
                        onValueChange = { assetsPath = it },
                        placeholder = "assets/games/bettercraft/"
                    )

                    MinecraftText(
                        text = "O Luanti procura jogos em 'assets/games/<nome_do_jogo>/'",
                        fontSize = 10,
                        color = Color.Gray
                    )
                }
            }
        }

        // Google Web Client ID
        item {
            MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MinecraftPalette.EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MinecraftText(
                            text = "GOOGLE WEB CLIENT ID (FIREBASE AUTH)",
                            fontSize = 12,
                            color = MinecraftPalette.EmeraldGreen
                        )
                    }

                    MinecraftInputField(
                        value = googleWebClientId,
                        onValueChange = { googleWebClientId = it },
                        placeholder = "ID do cliente Web do Google"
                    )

                    MinecraftText(
                        text = "Utilizado para login administrativo via Google Credential Manager.",
                        fontSize = 10,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Save & Reset Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MinecraftButton(
                    text = "SALVAR CONFIGURAÇÕES LOCAIS",
                    onClick = {
                        val newConfig = uiState.config.copy(
                            luantiApkUrl = luantiUrl.trim(),
                            bettercraftZipUrl = modZipUrl.trim(),
                            targetAssetsPath = assetsPath.trim(),
                            webViewUrl = webViewUrl.trim(),
                            outputDirectoryPath = outputDirectoryPath.trim(),
                            googleWebClientId = googleWebClientId.trim()
                        )
                        viewModel.saveConfig(newConfig)
                        Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                    },
                    isActionGreen = true,
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_save_config"
                )

                MinecraftButton(
                    text = "RESTAURAR PADRÕES ORIGINAIS",
                    onClick = {
                        viewModel.resetDefaults()
                        luantiUrl = AppConfig.DEFAULT_LUANTI_APK_URL
                        modZipUrl = AppConfig.DEFAULT_BETTERCRAFT_ZIP_URL
                        assetsPath = AppConfig.DEFAULT_ASSETS_PATH
                        webViewUrl = AppConfig.DEFAULT_WEBVIEW_URL
                        outputDirectoryPath = AppConfig.DEFAULT_OUTPUT_DIRECTORY_PATH
                        Toast.makeText(context, "Valores padrão restaurados!", Toast.LENGTH_SHORT).show()
                    },
                    icon = Icons.Default.RestartAlt,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
