package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.AdminConstants
import com.example.model.AppConfig
import com.example.model.DownloadLink
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

sealed interface FirebaseSyncStatus {
    object Idle : FirebaseSyncStatus
    object Syncing : FirebaseSyncStatus
    data class Connected(val lastSyncText: String, val message: String) : FirebaseSyncStatus
    data class Offline(val reason: String) : FirebaseSyncStatus
    data class Error(val error: String) : FirebaseSyncStatus
}

class FirebaseRemoteManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("bettercraft_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "FirebaseRemoteManager"
        private const val COLLECTION_CONFIG = "bettercraft_config"
        private const val DOC_REMOTE = "global_installer"

        private const val KEY_LUANTI_APK = "pref_luanti_apk"
        private const val KEY_BETTERCRAFT_ZIP = "pref_bettercraft_zip"
        private const val KEY_ASSETS_PATH = "pref_assets_path"
        private const val KEY_WEBVIEW_URL = "pref_webview_url"
        private const val KEY_OUTPUT_DIR = "pref_output_dir"
        private const val KEY_SERVER_MESSAGE = "pref_server_message"
        private const val KEY_ADMIN_EMAILS = "pref_admin_emails"
    }

    fun loadLocalConfig(): AppConfig {
        val adminsRaw = prefs.getString(KEY_ADMIN_EMAILS, null)
        val adminList = if (!adminsRaw.isNullOrBlank()) {
            val list = adminsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(AdminConstants.SUPER_ADMIN_EMAIL)) {
                list.add(0, AdminConstants.SUPER_ADMIN_EMAIL)
            }
            list
        } else {
            AppConfig.defaultAdmins()
        }

        return AppConfig(
            luantiApkUrl = prefs.getString(KEY_LUANTI_APK, AppConfig.DEFAULT_LUANTI_APK_URL) ?: AppConfig.DEFAULT_LUANTI_APK_URL,
            bettercraftZipUrl = prefs.getString(KEY_BETTERCRAFT_ZIP, AppConfig.DEFAULT_BETTERCRAFT_ZIP_URL) ?: AppConfig.DEFAULT_BETTERCRAFT_ZIP_URL,
            targetAssetsPath = prefs.getString(KEY_ASSETS_PATH, AppConfig.DEFAULT_ASSETS_PATH) ?: AppConfig.DEFAULT_ASSETS_PATH,
            webViewUrl = prefs.getString(KEY_WEBVIEW_URL, AppConfig.DEFAULT_WEBVIEW_URL) ?: AppConfig.DEFAULT_WEBVIEW_URL,
            outputDirectoryPath = prefs.getString(KEY_OUTPUT_DIR, AppConfig.DEFAULT_OUTPUT_DIRECTORY_PATH) ?: AppConfig.DEFAULT_OUTPUT_DIRECTORY_PATH,
            serverMessage = prefs.getString(KEY_SERVER_MESSAGE, "BetterCraft Luanti Engine v5.17 pronta para montagem!") ?: "Pronto",
            featuredLinks = AppConfig.defaultFeaturedLinks(),
            adminEmails = adminList
        )
    }

    fun saveLocalConfig(config: AppConfig) {
        prefs.edit()
            .putString(KEY_LUANTI_APK, config.luantiApkUrl)
            .putString(KEY_BETTERCRAFT_ZIP, config.bettercraftZipUrl)
            .putString(KEY_ASSETS_PATH, config.targetAssetsPath)
            .putString(KEY_WEBVIEW_URL, config.webViewUrl)
            .putString(KEY_OUTPUT_DIR, config.outputDirectoryPath)
            .putString(KEY_SERVER_MESSAGE, config.serverMessage)
            .putString(KEY_ADMIN_EMAILS, config.adminEmails.joinToString(","))
            .apply()
    }

    fun resetToDefaults(): AppConfig {
        prefs.edit().clear().apply()
        return AppConfig()
    }

    suspend fun fetchRemoteConfig(): Pair<AppConfig, FirebaseSyncStatus> {
        return try {
            val app = try {
                FirebaseApp.getInstance()
            } catch (e: Exception) {
                null
            }

            if (app == null) {
                return Pair(
                    loadLocalConfig(),
                    FirebaseSyncStatus.Offline("FirebaseApp não inicializado. Usando configurações locais padrão.")
                )
            }

            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection(COLLECTION_CONFIG).document(DOC_REMOTE).get().await()

            if (snapshot.exists()) {
                val luantiApk = snapshot.getString("luanti_apk_url") ?: AppConfig.DEFAULT_LUANTI_APK_URL
                val modZip = snapshot.getString("bettercraft_zip_url") ?: AppConfig.DEFAULT_BETTERCRAFT_ZIP_URL
                val assetsPath = snapshot.getString("target_assets_path") ?: AppConfig.DEFAULT_ASSETS_PATH
                val webView = snapshot.getString("webview_url") ?: AppConfig.DEFAULT_WEBVIEW_URL
                val outputDir = snapshot.getString("output_directory_path") ?: AppConfig.DEFAULT_OUTPUT_DIRECTORY_PATH
                val message = snapshot.getString("server_message") ?: "Configuração remota ativa"

                // Extract custom links list if present
                @Suppress("UNCHECKED_CAST")
                val rawLinks = snapshot.get("featured_links") as? List<Map<String, Any>>
                val links = if (!rawLinks.isNullOrEmpty()) {
                    rawLinks.mapNotNull { map ->
                        val id = map["id"] as? String ?: return@mapNotNull null
                        val title = map["title"] as? String ?: id
                        val url = map["url"] as? String ?: return@mapNotNull null
                        val desc = map["description"] as? String ?: ""
                        val cat = map["category"] as? String ?: "Engine"
                        DownloadLink(id, title, url, desc, cat)
                    }
                } else {
                    AppConfig.defaultFeaturedLinks()
                }

                val remoteAdmins = (snapshot.get("admin_emails") as? List<*>)?.mapNotNull { it as? String }?.toMutableList()
                    ?: AppConfig.defaultAdmins().toMutableList()
                if (!remoteAdmins.contains(AdminConstants.SUPER_ADMIN_EMAIL)) {
                    remoteAdmins.add(0, AdminConstants.SUPER_ADMIN_EMAIL)
                }

                val remoteConfig = AppConfig(
                    luantiApkUrl = luantiApk,
                    bettercraftZipUrl = modZip,
                    targetAssetsPath = assetsPath,
                    webViewUrl = webView,
                    outputDirectoryPath = outputDir,
                    serverMessage = message,
                    featuredLinks = links,
                    adminEmails = remoteAdmins
                )

                // Save locally as cache
                saveLocalConfig(remoteConfig)

                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                Pair(remoteConfig, FirebaseSyncStatus.Connected(timeStr, "Sincronizado com Firestore com sucesso!"))
            } else {
                Pair(
                    loadLocalConfig(),
                    FirebaseSyncStatus.Offline("Documento remoto '$DOC_REMOTE' ainda não existe no Firestore. Você pode publicá-lo agora!")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao conectar no Firestore: ${e.message}")
            Pair(
                loadLocalConfig(),
                FirebaseSyncStatus.Error("Erro Firestore: ${e.message ?: "Conexão falhou"}")
            )
        }
    }

    suspend fun publishRemoteConfig(config: AppConfig): FirebaseSyncStatus {
        return try {
            val app = try {
                FirebaseApp.getInstance()
            } catch (e: Exception) {
                null
            }

            if (app == null) {
                return FirebaseSyncStatus.Offline("Firebase não disponível para publicação.")
            }

            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "luanti_apk_url" to config.luantiApkUrl,
                "bettercraft_zip_url" to config.bettercraftZipUrl,
                "target_assets_path" to config.targetAssetsPath,
                "webview_url" to config.webViewUrl,
                "output_directory_path" to config.outputDirectoryPath,
                "server_message" to config.serverMessage,
                "admin_emails" to config.adminEmails,
                "updated_at" to com.google.firebase.Timestamp.now(),
                "featured_links" to config.featuredLinks.map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "url" to it.url,
                        "description" to it.description,
                        "category" to it.category
                    )
                }
            )

            db.collection(COLLECTION_CONFIG).document(DOC_REMOTE)
                .set(data, SetOptions.merge())
                .await()

            saveLocalConfig(config)
            val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            FirebaseSyncStatus.Connected(timeStr, "Configurações publicadas remotamente no Firebase!")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao publicar no Firestore", e)
            FirebaseSyncStatus.Error("Falha ao salvar no Firebase: ${e.message}")
        }
    }
}
