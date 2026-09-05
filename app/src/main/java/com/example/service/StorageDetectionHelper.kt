package com.example.service

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object StorageDetectionHelper {

    data class StorageInfo(
        val rootDir: File,
        val description: String,
        val deviceIdentifier: String,
        val isInternalFallback: Boolean
    )

    /**
     * Dynamically detects the valid, writable storage root across physical devices,
     * virtual machines, and emulators (such as sdk_gphone64_arm64).
     * Avoids assuming or hardcoding /storage/emulated/0/.
     */
    fun detectStorageRoot(context: Context): StorageInfo {
        val deviceId = "${Build.DEVICE} (${Build.MODEL}, ${Build.PRODUCT})"

        // 1. Check all mounted external files directories
        try {
            val extDirs = context.getExternalFilesDirs(null)
            for (dir in extDirs) {
                if (dir != null && isDirectoryWritable(dir)) {
                    val path = dir.absolutePath
                    val desc = if (path.contains("storage/emulated/0")) {
                        "Armazenamento Compartilhado Padrão ($path)"
                    } else {
                        "Raiz de Armazenamento Dinâmica do Ambiente ($path)"
                    }
                    return StorageInfo(
                        rootDir = dir,
                        description = desc,
                        deviceIdentifier = deviceId,
                        isInternalFallback = false
                    )
                }
            }
        } catch (t: Throwable) {}

        // 2. Try primary external storage if mounted and writable
        try {
            val extRoot = Environment.getExternalStorageDirectory()
            if (extRoot != null && extRoot.exists() && isDirectoryWritable(extRoot)) {
                val appDir = File(extRoot, "Android/data/${context.packageName}/files").apply { mkdirs() }
                if (isDirectoryWritable(appDir)) {
                    return StorageInfo(
                        rootDir = appDir,
                        description = "Raiz Externa (${appDir.absolutePath})",
                        deviceIdentifier = deviceId,
                        isInternalFallback = false
                    )
                }
            }
        } catch (t: Throwable) {}

        // 3. Fallback to app's internal root directory (/data/user/0/... or /data/data/...)
        // Highly reliable in emulators where /storage/emulated/0 is unmounted or permission-restricted
        val internalFiles = context.filesDir
        try {
            if (internalFiles != null && isDirectoryWritable(internalFiles)) {
                return StorageInfo(
                    rootDir = internalFiles,
                    description = "Raiz Interna Nativa (${internalFiles.absolutePath})",
                    deviceIdentifier = deviceId,
                    isInternalFallback = true
                )
            }
        } catch (t: Throwable) {}

        // 4. Final fallback to cache directory
        return StorageInfo(
            rootDir = context.cacheDir,
            description = "Raiz de Cache Local (${context.cacheDir.absolutePath})",
            deviceIdentifier = deviceId,
            isInternalFallback = true
        )
    }

    private fun isDirectoryWritable(dir: File): Boolean {
        if (!dir.exists() && !dir.mkdirs()) {
            return false
        }
        val testProbe = File(dir, ".probe_write_${System.currentTimeMillis()}.tmp")
        return try {
            val created = testProbe.createNewFile()
            if (created) {
                testProbe.delete()
                true
            } else {
                dir.canWrite()
            }
        } catch (e: Exception) {
            false
        }
    }
}
