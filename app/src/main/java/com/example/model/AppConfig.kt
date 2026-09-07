package com.example.model

data class DownloadLink(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val category: String = "Engine" // "Engine", "Subgame", "Mod"
)

data class AppConfig(
    val luantiApkUrl: String = DEFAULT_LUANTI_APK_URL,
    val bettercraftZipUrl: String = DEFAULT_BETTERCRAFT_ZIP_URL,
    val targetAssetsPath: String = DEFAULT_ASSETS_PATH,
    val webViewUrl: String = DEFAULT_WEBVIEW_URL,
    val outputDirectoryPath: String = DEFAULT_OUTPUT_DIRECTORY_PATH,
    val serverMessage: String = "BetterCraft Voxel Edition v5.17.0 pronta para montagem!",
    val featuredLinks: List<DownloadLink> = defaultFeaturedLinks(),
    val adminEmails: List<String> = defaultAdmins()
) {
    companion object {
        const val DEFAULT_LUANTI_APK_URL =
            "https://github.com/luanti-org/luanti/releases/download/5.17.0/luanti-5.17.0-arm64-v8a.apk"
        const val DEFAULT_BETTERCRAFT_ZIP_URL =
            "https://github.com/wrxxnch/bettercraft/archive/refs/heads/main.zip"
        const val DEFAULT_ASSETS_PATH = "assets/assets.zip/games/bettercraft/"
        const val DEFAULT_WEBVIEW_URL = "https://wrxxnch.github.io/bettercraftsite"
        const val DEFAULT_GITHUB_RELEASE_URL = "https://github.com/wrxxnch/bettercraft/releases/latest"
        const val DEFAULT_OUTPUT_DIRECTORY_PATH =
            "/storage/emulated/0/Android/data/com.aistudio.bettercraft.vzkx/files/output/"

        fun defaultAdmins(): List<String> = listOf(
            AdminConstants.SUPER_ADMIN_EMAIL
        )

        fun defaultFeaturedLinks(): List<DownloadLink> = listOf(
            DownloadLink(
                id = "bettercraft_apk_release",
                title = "BetterCraft APK (GitHub Releases)",
                url = "https://github.com/wrxxnch/bettercraft/releases/latest",
                description = "Baixe o APK oficial montado diretamente da página de Releases do GitHub.",
                category = "Release"
            ),
            DownloadLink(
                id = "luanti_arm64",
                title = "Luanti 5.17.0 (ARM64-v8a)",
                url = "https://github.com/luanti-org/luanti/releases/download/5.17.0/luanti-5.17.0-arm64-v8a.apk",
                description = "Base engine recomendada para a maioria dos celulares modernos de 64 bits.",
                category = "Engine"
            ),
            DownloadLink(
                id = "luanti_armeabi",
                title = "Luanti 5.17.0 (armeabi-v7a)",
                url = "https://github.com/luanti-org/luanti/releases/download/5.17.0/luanti-5.17.0-armeabi-v7a.apk",
                description = "Base engine para dispositivos Android mais antigos de 32 bits.",
                category = "Engine"
            ),
            DownloadLink(
                id = "bettercraft_main",
                title = "BetterCraft Subgame (Branch Main)",
                url = "https://github.com/wrxxnch/bettercraft/archive/refs/heads/main.zip",
                description = "Subgame BetterCraft com texturas Faithful, mods e mecânicas otimizadas.",
                category = "Subgame"
            )
        )
    }
}
