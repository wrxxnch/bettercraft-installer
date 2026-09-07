package com.example.model

import java.io.File
import java.util.concurrent.atomic.AtomicLong

private val logIdGenerator = AtomicLong(1L)

enum class LogLevel {
    INFO, SUCCESS, WARNING, ERROR, PROGRESS
}

data class ConsoleLog(
    val id: Long = logIdGenerator.incrementAndGet(),
    val message: String,
    val level: LogLevel = LogLevel.INFO,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
)

sealed interface PatchStep {
    object Idle : PatchStep
    data class DownloadingApk(val progress: Float, val currentBytes: Long, val totalBytes: Long) : PatchStep
    data class DownloadingZip(val progress: Float, val currentBytes: Long, val totalBytes: Long) : PatchStep
    data class ExtractingAndInjecting(val currentFile: String, val processedCount: Int, val totalFiles: Int) : PatchStep
    data class SigningApk(val stepDetail: String) : PatchStep
    data class Success(
        val signedApkFile: File,
        val sizeBytes: Long,
        val sha256Hex: String,
        val outputDirectory: File? = null,
        val gameZipFile: File? = null,
        val gamesFolder: File? = null,
        val luantiCopied: Boolean = false
    ) : PatchStep
    data class Failed(val errorMessage: String, val details: String? = null) : PatchStep
}
