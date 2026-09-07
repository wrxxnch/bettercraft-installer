package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import java.io.File

class BetterCraftApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initWebViewEnvironment()
    }

    private fun initWebViewEnvironment() {
        try {
            // Fix 1: Multi-process directory suffix if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName()
                if (processName != packageName) {
                    try {
                        WebView.setDataDirectorySuffix(processName)
                    } catch (t: Throwable) {
                        // ignore if already set
                    }
                }
            }

            // Fix 2: Pre-create all WebView cache directories so simple_index_file.cc and simple_file_enumerator.cc don't fail
            // Target path from log: /data/user/0/com.aistudio.bettercraft.vzkx/cache/WebView/Default/HTTP Cache/Code Cache/js
            val cacheBase = cacheDir
            val webViewCacheDir = File(cacheBase, "WebView")
            val defaultCacheDir = File(webViewCacheDir, "Default")
            val httpCacheDir = File(defaultCacheDir, "HTTP Cache")
            val codeCacheDir = File(httpCacheDir, "Code Cache")
            val jsCodeCacheDir = File(codeCacheDir, "js")
            val wasmCodeCacheDir = File(codeCacheDir, "wasm")

            jsCodeCacheDir.mkdirs()
            wasmCodeCacheDir.mkdirs()

            // Also ensure app_webview in dataDir exists
            val appDataDir = applicationInfo.dataDir
            val webViewDataDir = File(appDataDir, "app_webview")
            val defaultDataDir = File(webViewDataDir, "Default")
            val dataHttpCache = File(defaultDataDir, "HTTP Cache")
            dataHttpCache.mkdirs()

            // Also ensure standard code_cache directory exists
            File(cacheBase, "code_cache").mkdirs()
        } catch (t: Throwable) {
            // Prevent crash on app startup
        }
    }
}
