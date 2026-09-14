package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.service.StorageDetectionHelper
import com.example.ui.BetterCraftViewModel
import com.example.ui.MainUiState
import com.example.ui.components.MinecraftBadge
import com.example.ui.components.MinecraftButton
import com.example.ui.components.MinecraftInputField
import com.example.ui.components.MinecraftPalette
import com.example.ui.components.MinecraftPanel
import com.example.ui.components.MinecraftText
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    uiState: MainUiState,
    viewModel: BetterCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUrl = uiState.config.webViewUrl

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    val hasRenderCrashed = uiState.isWebViewCompatibilityMode
    var retryKey by remember { mutableStateOf(0) }

    var isEditingUrl by remember { mutableStateOf(false) }
    var editedUrlText by remember(currentUrl) { mutableStateOf(currentUrl) }

    // Pre-create WebView cache directories to prevent simple_index_file.cc and simple_file_enumerator.cc errors
    LaunchedEffect(Unit) {
        try {
            val cacheBase = context.cacheDir
            val webViewCache = File(cacheBase, "WebView")
            val defaultCache = File(webViewCache, "Default")
            val httpCache = File(defaultCache, "HTTP Cache")
            val codeCache = File(httpCache, "Code Cache")
            File(codeCache, "js").mkdirs()
            File(codeCache, "wasm").mkdirs()

            val appDataDir = context.applicationInfo.dataDir
            val webviewDir = File(appDataDir, "app_webview")
            val defaultDir = File(webviewDir, "Default")
            val httpCacheDir = File(defaultDir, "HTTP Cache")
            val codeCacheDir = File(httpCacheDir, "Code Cache")
            File(codeCacheDir, "js").mkdirs()
            File(codeCacheDir, "wasm").mkdirs()
        } catch (t: Throwable) {
            // Ignore directory creation failure
        }
    }

    // Website view accessible to all clients and visitors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinecraftPalette.BackgroundDark)
    ) {
        // Minecraft Web Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinecraftPalette.PanelBackground)
                .border(1.dp, MinecraftPalette.ButtonBorderBlack)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MinecraftPalette.DiamondBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (uiState.isAdmin) {
                        MinecraftBadge(text = "WEB ADMIN", color = MinecraftPalette.EmeraldGreen)
                    } else {
                        MinecraftBadge(text = "BETTERCRAFT WEB", color = MinecraftPalette.DiamondBlue)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    MinecraftText(
                        text = currentUrl.removePrefix("https://").removePrefix("http://"),
                        fontSize = 11,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Edit URL button (only for administrators)
                    if (uiState.isAdmin) {
                        IconButton(
                            onClick = { isEditingUrl = !isEditingUrl },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Alterar Link do Site",
                                tint = MinecraftPalette.GoldYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Navigation controls
                    IconButton(
                        onClick = {
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            }
                        },
                        enabled = canGoBack && !hasRenderCrashed,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = if (canGoBack && !hasRenderCrashed) Color.White else Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (webViewInstance?.canGoForward() == true) {
                                webViewInstance?.goForward()
                            }
                        },
                        enabled = canGoForward && !hasRenderCrashed,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Avançar",
                            tint = if (canGoForward && !hasRenderCrashed) Color.White else Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (hasRenderCrashed) {
                                retryKey++
                                viewModel.setWebViewCompatibilityMode(false)
                                isLoading = true
                            } else {
                                try {
                                    webViewInstance?.reload()
                                } catch (t: Throwable) {}
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recarregar",
                            tint = MinecraftPalette.GoldYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Botão destacado no topo para abrir o site em outro navegador
                MinecraftButton(
                    text = "ABRIR EM OUTRO NAVEGADOR",
                    onClick = {
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                            context.startActivity(browserIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao abrir navegador: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    isActionGreen = true,
                    icon = Icons.Default.OpenInBrowser,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    testTag = "btn_top_open_other_browser"
                )

                // Inline Edit URL bar (for administrators)
                if (isEditingUrl && uiState.isAdmin) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MinecraftPalette.PanelInner)
                            .border(1.dp, MinecraftPalette.GoldYellow)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MinecraftInputField(
                                value = editedUrlText,
                                onValueChange = { editedUrlText = it },
                                placeholder = "https://wrxxnch.github.io/bettercraftsite",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftButton(
                                text = "SALVAR",
                                onClick = {
                                    if (editedUrlText.isNotBlank()) {
                                        var finalUrl = editedUrlText.trim()
                                        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                                            finalUrl = "https://$finalUrl"
                                        }
                                        viewModel.updateWebsiteUrl(finalUrl)
                                        isEditingUrl = false
                                        if (!StorageDetectionHelper.isVirtualOrEmulatorEnvironment()) {
                                            viewModel.setWebViewCompatibilityMode(false)
                                            webViewInstance?.loadUrl(finalUrl)
                                        }
                                        Toast.makeText(context, "Link do site atualizado!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isActionGreen = true,
                                icon = Icons.Default.Save,
                                modifier = Modifier.height(38.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isLoading && !hasRenderCrashed) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MinecraftPalette.EmeraldGreen,
                trackColor = Color(0xFF222428)
            )
        }

        // Main content area: Fallback compatibility card OR WebView
        if (hasRenderCrashed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                MinecraftPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MinecraftPalette.PanelInner)
                                .border(2.dp, MinecraftPalette.GoldYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MinecraftPalette.GoldYellow,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        MinecraftBadge(text = "MODO DE COMPATIBILIDADE", color = MinecraftPalette.GoldYellow)

                        MinecraftText(
                            text = "NAVEGADOR DO DISPOSITIVO DISPONÍVEL",
                            fontSize = 13,
                            fontWeight = FontWeight.Bold,
                            color = MinecraftPalette.GoldYellow
                        )

                        MinecraftText(
                            text = "O ambiente gráfico deste dispositivo não possui suporte a aceleração 3D por hardware para WebViews embutidas. Abra o site diretamente no seu navegador:",
                            fontSize = 11,
                            color = Color.LightGray
                        )

                        MinecraftText(
                            text = currentUrl,
                            fontSize = 12,
                            fontWeight = FontWeight.Bold,
                            color = MinecraftPalette.DiamondBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        MinecraftButton(
                            text = "ABRIR NO NAVEGADOR DO DISPOSITIVO",
                            onClick = {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao abrir navegador: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            isActionGreen = true,
                            icon = Icons.Default.OpenInBrowser,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_open_external_browser"
                        )

                        MinecraftButton(
                            text = "TENTAR NAVEGADOR INTERNO NOVAMENTE",
                            onClick = {
                                retryKey++
                                viewModel.setWebViewCompatibilityMode(false)
                                isLoading = true
                            },
                            icon = Icons.Default.Refresh,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_retry_internal_webview"
                        )
                    }
                }
            }
        } else {
            // WebView Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .border(2.dp, MinecraftPalette.ButtonBorderBlack)
            ) {
                key(retryKey) {
                    var isRenderDead = false
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                try {
                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        mediaPlaybackRequiresUserGesture = true
                                        allowFileAccess = false
                                        allowContentAccess = false
                                        databaseEnabled = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            safeBrowsingEnabled = false
                                        }
                                        val curUa = userAgentString ?: ""
                                        if (!curUa.contains("BetterCraftApp")) {
                                            userAgentString = "$curUa BetterCraftApp/1.0"
                                        }
                                    }
                                } catch (t: Throwable) {}

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                    }

                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        isRenderDead = true
                                        viewModel.setWebViewCompatibilityMode(true)
                                        isLoading = false
                                        webViewInstance = null
                                        try {
                                            view?.destroy()
                                        } catch (t: Throwable) {}
                                        return true
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        loadProgress = newProgress / 100f
                                        if (newProgress >= 100) {
                                            isLoading = false
                                        }
                                    }
                                }

                                try {
                                    loadUrl(currentUrl)
                                } catch (t: Throwable) {
                                    viewModel.setWebViewCompatibilityMode(true)
                                }
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            if (!isRenderDead && view.url != currentUrl && !isLoading) {
                                try {
                                    view.loadUrl(currentUrl)
                                } catch (t: Throwable) {
                                    viewModel.setWebViewCompatibilityMode(true)
                                }
                            }
                        },
                        onRelease = { view ->
                            if (!isRenderDead) {
                                try {
                                    view.stopLoading()
                                    view.destroy()
                                } catch (t: Throwable) {}
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
