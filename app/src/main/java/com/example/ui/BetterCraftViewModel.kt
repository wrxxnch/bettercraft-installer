package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthService
import com.example.data.FirebaseRemoteManager
import com.example.data.FirebaseSyncStatus
import com.example.model.AdminConstants
import com.example.model.AppConfig
import com.example.model.ConsoleLog
import com.example.model.LogLevel
import com.example.model.PatchStep
import com.example.model.UserProfile
import com.example.service.ApkPatcherEngine
import com.example.service.StorageDetectionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class MainUiState(
    val config: AppConfig = AppConfig(),
    val patchStep: PatchStep = PatchStep.Idle,
    val logs: List<ConsoleLog> = emptyList(),
    val firebaseStatus: FirebaseSyncStatus = FirebaseSyncStatus.Idle,
    val selectedTab: Int = 0,
    val isPatching: Boolean = false,
    val lastGeneratedApk: File? = null,
    val currentUser: UserProfile? = null,
    val isAuthLoading: Boolean = false,
    val authError: String? = null,
    val showLoginDialog: Boolean = false,
    val detectedStoragePath: String = "",
    val detectedEnvironment: String = ""
) {
    val isAdmin: Boolean
        get() = currentUser?.isAdmin == true || currentUser?.isSuperAdmin == true

    val isSuperAdmin: Boolean
        get() = currentUser?.isSuperAdmin == true
}

class BetterCraftViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteManager = FirebaseRemoteManager(application)
    private val patcherEngine = ApkPatcherEngine(application)
    private val authService = AuthService(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var patchingJob: Job? = null

    init {
        // Load local configuration on startup
        val initialConfig = remoteManager.loadLocalConfig()
        val storageInfo = StorageDetectionHelper.detectStorageRoot(application)
        _uiState.update {
            it.copy(
                config = initialConfig,
                detectedStoragePath = storageInfo.rootDir.absolutePath,
                detectedEnvironment = storageInfo.deviceIdentifier
            )
        }
        addLog("BetterCraft Luanti Installer inicializado com sucesso.", LogLevel.INFO)
        addLog("Ambiente Detectado: ${storageInfo.deviceIdentifier}", LogLevel.INFO)
        addLog("Raiz de Armazenamento Ativa: ${storageInfo.rootDir.absolutePath} (${storageInfo.description})", LogLevel.SUCCESS)
        addLog("Pronto para baixar, injetar mods e assinar APK.", LogLevel.INFO)

        // Ensure "não deixe a conta logada": Always clear any leftover auth session on startup
        viewModelScope.launch {
            authService.signOut()
            _uiState.update { it.copy(currentUser = null) }
        }

        // Try syncing from Firebase in background
        syncFromFirebase(silent = true)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun showLoginDialog(show: Boolean = true) {
        _uiState.update { it.copy(showLoginDialog = show, authError = null) }
    }

    fun loginWithGoogle(webClientId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val result = authService.signInWithGoogleCredential(webClientId, _uiState.value.config.adminEmails)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isAuthLoading = false,
                        showLoginDialog = false
                    )
                }
                if (user.isAdmin) {
                    addLog("Google Login: ${user.email} [ADMINISTRADOR AUTORIZADO]", LogLevel.SUCCESS)
                } else {
                    addLog("Google Login: ${user.email} [USUÁRIO COMUM]", LogLevel.INFO)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authError = "Google Play Services ou Credential falhou: ${error.message}. Você pode usar o botão 'Entrar como Owner' ou e-mail abaixo."
                    )
                }
                addLog("Aviso Login Google: ${error.message}", LogLevel.WARNING)
            }
        }
    }

    fun loginWithEmail(email: String) {
        val user = authService.signInWithDirectEmail(email, _uiState.value.config.adminEmails)
        _uiState.update {
            it.copy(
                currentUser = user,
                showLoginDialog = false,
                authError = null
            )
        }
        if (user.isAdmin) {
            addLog("Login realizado com sucesso: ${user.email} [ADMINISTRADOR AUTORIZADO]", LogLevel.SUCCESS)
        } else {
            addLog("Login: ${user.email} [USUÁRIO SEM PRIVILÉGIOS DE ADMIN]", LogLevel.INFO)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
            _uiState.update {
                it.copy(
                    currentUser = null,
                    selectedTab = if (it.selectedTab > 1) 0 else it.selectedTab
                )
            }
            addLog("Sessão finalizada. Modo visitante ativo.", LogLevel.INFO)
        }
    }

    fun addAdmin(email: String) {
        val clean = email.trim().lowercase()
        if (clean.isBlank() || !clean.contains("@")) return

        val currentAdmins = _uiState.value.config.adminEmails.toMutableList()
        if (!currentAdmins.any { it.equals(clean, ignoreCase = true) }) {
            currentAdmins.add(clean)
            val updatedConfig = _uiState.value.config.copy(adminEmails = currentAdmins)
            saveConfig(updatedConfig)

            // If current logged-in user matches this email, grant admin in state
            val cur = _uiState.value.currentUser
            if (cur != null && cur.email.equals(clean, ignoreCase = true)) {
                _uiState.update { it.copy(currentUser = cur.copy(isAdmin = true)) }
            }
            addLog("Novo administrador registrado: $clean", LogLevel.SUCCESS)
        }
    }

    fun removeAdmin(email: String) {
        val clean = email.trim().lowercase()
        if (clean.equals(AdminConstants.SUPER_ADMIN_EMAIL, ignoreCase = true)) {
            addLog("O superadmin proprietário não pode ser removido.", LogLevel.WARNING)
            return
        }

        val currentAdmins = _uiState.value.config.adminEmails.filterNot { it.equals(clean, ignoreCase = true) }
        val updatedConfig = _uiState.value.config.copy(adminEmails = currentAdmins)
        saveConfig(updatedConfig)

        val cur = _uiState.value.currentUser
        if (cur != null && cur.email.equals(clean, ignoreCase = true)) {
            _uiState.update { it.copy(currentUser = cur.copy(isAdmin = false)) }
        }
        addLog("Administrador removido: $clean", LogLevel.INFO)
    }

    fun updateWebsiteUrl(newUrl: String) {
        val cleanUrl = newUrl.trim()
        val updatedConfig = _uiState.value.config.copy(webViewUrl = cleanUrl)
        saveConfig(updatedConfig)
        addLog("Link do site atualizado para: $cleanUrl", LogLevel.SUCCESS)
    }

    fun addLog(message: String, level: LogLevel = LogLevel.INFO) {
        _uiState.update { state ->
            val updatedLogs = (state.logs + ConsoleLog(message = message, level = level)).takeLast(250)
            state.copy(logs = updatedLogs)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun startPatchProcess() {
        if (_uiState.value.isPatching) return

        patchingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPatching = true,
                    patchStep = PatchStep.DownloadingApk(0f, 0, 0)
                )
            }

            val currentConfig = _uiState.value.config
            try {
                val signedApk = patcherEngine.executePatch(
                    luantiApkUrl = currentConfig.luantiApkUrl,
                    bettercraftZipUrl = currentConfig.bettercraftZipUrl,
                    targetAssetsPath = currentConfig.targetAssetsPath,
                    onStep = { step ->
                        _uiState.update { it.copy(patchStep = step) }
                    },
                    onLog = { msg, lvl ->
                        addLog(msg, lvl)
                    }
                )

                _uiState.update {
                    it.copy(
                        isPatching = false,
                        lastGeneratedApk = signedApk
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPatching = false,
                        patchStep = PatchStep.Failed(e.message ?: "Erro desconhecido", e.stackTraceToString())
                    )
                }
            }
        }
    }

    fun cancelPatch() {
        patchingJob?.cancel()
        _uiState.update {
            it.copy(
                isPatching = false,
                patchStep = PatchStep.Idle
            )
        }
        addLog("Processo de montagem cancelado pelo usuário.", LogLevel.WARNING)
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "Arquivo APK não encontrado!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            addLog("Instalador do sistema disparado para: ${apkFile.name}", LogLevel.SUCCESS)
        } catch (e: Exception) {
            addLog("Erro ao abrir instalador de pacotes: ${e.message}", LogLevel.ERROR)
            Toast.makeText(context, "Erro ao instalar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "Arquivo APK não encontrado!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BetterCraft Luanti APK")
                putExtra(Intent.EXTRA_TEXT, "APK do BetterCraft Luanti montado e assinado!")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(Intent.createChooser(shareIntent, "Salvar ou Enviar APK"))
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchLuantiApp(context: Context) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("net.minetest.minetest")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                addLog("Iniciando Luanti / BetterCraft...", LogLevel.SUCCESS)
            } else {
                Toast.makeText(context, "Luanti ainda não está instalado no dispositivo! Instale o APK primeiro.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            addLog("Erro ao abrir Luanti: ${e.message}", LogLevel.ERROR)
            Toast.makeText(context, "Erro ao abrir Luanti: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveConfig(newConfig: AppConfig) {
        remoteManager.saveLocalConfig(newConfig)
        _uiState.update { it.copy(config = newConfig) }
        addLog("Configurações locais salvas com sucesso!", LogLevel.SUCCESS)
    }

    fun resetDefaults() {
        val defaultConfig = remoteManager.resetToDefaults()
        _uiState.update { it.copy(config = defaultConfig) }
        addLog("Configurações restauradas para os padrões originais.", LogLevel.INFO)
    }

    fun syncFromFirebase(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(firebaseStatus = FirebaseSyncStatus.Syncing) }
                addLog("Tentando sincronizar variáveis remotas do Firebase...", LogLevel.INFO)
            }

            val (remoteConfig, status) = remoteManager.fetchRemoteConfig()
            _uiState.update {
                it.copy(
                    config = remoteConfig,
                    firebaseStatus = status
                )
            }

            // Check if current user is now in admin list
            val cur = _uiState.value.currentUser
            if (cur != null) {
                val isAdmin = AdminConstants.isAdmin(cur.email, remoteConfig.adminEmails)
                _uiState.update { it.copy(currentUser = cur.copy(isAdmin = isAdmin)) }
            }

            when (status) {
                is FirebaseSyncStatus.Connected -> {
                    addLog("Firebase conectado! Variáveis sincronizadas remotamente.", LogLevel.SUCCESS)
                }
                is FirebaseSyncStatus.Offline -> {
                    if (!silent) addLog("Firebase Offline: ${status.reason}", LogLevel.WARNING)
                }
                is FirebaseSyncStatus.Error -> {
                    if (!silent) addLog("Erro Firebase: ${status.error}", LogLevel.ERROR)
                }
                else -> {}
            }
        }
    }

    fun publishToFirebase(configToPublish: AppConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(firebaseStatus = FirebaseSyncStatus.Syncing) }
            addLog("Publicando variáveis no Firestore...", LogLevel.INFO)

            val status = remoteManager.publishRemoteConfig(configToPublish)
            _uiState.update {
                it.copy(
                    config = configToPublish,
                    firebaseStatus = status
                )
            }

            when (status) {
                is FirebaseSyncStatus.Connected -> {
                    addLog("Variáveis salvas no Firebase Firestore!", LogLevel.SUCCESS)
                }
                is FirebaseSyncStatus.Error -> {
                    addLog("Erro ao publicar no Firebase: ${status.error}", LogLevel.ERROR)
                }
                is FirebaseSyncStatus.Offline -> {
                    addLog("Firebase indisponível: ${status.reason}", LogLevel.WARNING)
                }
                else -> {}
            }
        }
    }
}
