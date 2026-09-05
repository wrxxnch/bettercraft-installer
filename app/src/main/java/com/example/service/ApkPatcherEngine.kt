package com.example.service

import android.content.Context
import com.example.model.LogLevel
import com.example.model.PatchStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext

class ApkPatcherEngine(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun executePatch(
        luantiApkUrl: String,
        bettercraftZipUrl: String,
        targetAssetsPath: String,
        onStep: (PatchStep) -> Unit,
        onLog: (String, LogLevel) -> Unit
    ): File = withContext(Dispatchers.IO) {
        // Detect storage root dynamically for current environment (e.g. sdk_gphone64_arm64)
        val storageInfo = StorageDetectionHelper.detectStorageRoot(context)
        val workspaceDir = File(context.cacheDir, "patcher_workspace").apply { mkdirs() }
        val outputDir = File(storageInfo.rootDir, "output").apply { mkdirs() }

        val luantiApkFile = File(workspaceDir, "luanti_base.apk")
        val modZipFile = File(workspaceDir, "bettercraft_mod.zip")
        val unsignedApkFile = File(workspaceDir, "repackaged_unsigned.apk")
        val signedApkFile = File(outputDir, "BetterCraft-Luanti-v5.17.0.apk")

        try {
            onLog("Iniciando processo de montagem do BetterCraft Luanti...", LogLevel.INFO)
            onLog("Dispositivo/Ambiente: ${storageInfo.deviceIdentifier}", LogLevel.INFO)
            onLog("Raiz de Armazenamento Detectada: ${storageInfo.rootDir.absolutePath}", LogLevel.SUCCESS)
            onLog("Tipo: ${storageInfo.description}", LogLevel.INFO)
            onLog("Engine Base: $luantiApkUrl", LogLevel.INFO)
            onLog("Repositório Mod: $bettercraftZipUrl", LogLevel.INFO)
            onLog("Destino Assets: $targetAssetsPath", LogLevel.INFO)

            // Step 1: Download Luanti APK
            onLog("Passo 1/4: Baixando APK do Luanti do GitHub...", LogLevel.PROGRESS)
            downloadFileWithProgress(
                url = luantiApkUrl,
                destination = luantiApkFile,
                onProgress = { progress, current, total ->
                    onStep(PatchStep.DownloadingApk(progress, current, total))
                },
                onLog = onLog,
                fileLabel = "Luanti APK"
            )
            onLog("Luanti APK baixado com sucesso: ${formatBytes(luantiApkFile.length())}", LogLevel.SUCCESS)

            // Step 2: Download BetterCraft mod repo zip
            onLog("Passo 2/4: Baixando repositório BetterCraft (.zip)...", LogLevel.PROGRESS)
            downloadFileWithProgress(
                url = bettercraftZipUrl,
                destination = modZipFile,
                onProgress = { progress, current, total ->
                    onStep(PatchStep.DownloadingZip(progress, current, total))
                },
                onLog = onLog,
                fileLabel = "BetterCraft Zip"
            )
            onLog("BetterCraft Zip baixado com sucesso: ${formatBytes(modZipFile.length())}", LogLevel.SUCCESS)

            // Step 3: Inject BetterCraft into APK assets
            onLog("Passo 3/4: Descompactando e inserindo arquivos na pasta de assets do APK...", LogLevel.PROGRESS)
            repackageApkWithAssets(
                sourceApk = luantiApkFile,
                modZip = modZipFile,
                targetAssetsFolder = targetAssetsPath,
                outputUnsignedApk = unsignedApkFile,
                onStep = onStep,
                onLog = onLog
            )
            onLog("Repacotamento concluído! APK intermediário: ${formatBytes(unsignedApkFile.length())}", LogLevel.SUCCESS)

            // Step 4: Sign APK
            onLog("Passo 4/4: Assinando o novo APK com APK Signature Scheme v1+v2+v3...", LogLevel.PROGRESS)
            onStep(PatchStep.SigningApk("Iniciando assinatura digital..."))
            ApkSignerHelper.signApk(
                unsignedApk = unsignedApkFile,
                signedApk = signedApkFile,
                onProgress = { detail ->
                    onStep(PatchStep.SigningApk(detail))
                    onLog(detail, LogLevel.INFO)
                }
            )

            // Clean up intermediate files to save disk space
            luantiApkFile.delete()
            modZipFile.delete()
            unsignedApkFile.delete()

            val sha256 = calculateSha256(signedApkFile)
            onLog("APK final assinado com sucesso! Tamanho: ${formatBytes(signedApkFile.length())}", LogLevel.SUCCESS)
            onLog("SHA-256: $sha256", LogLevel.INFO)
            onLog("Local do arquivo: ${signedApkFile.absolutePath}", LogLevel.INFO)

            onStep(PatchStep.Success(signedApkFile, signedApkFile.length(), sha256))
            return@withContext signedApkFile

        } catch (e: Exception) {
            onLog("ERRO durante a montagem: ${e.message}", LogLevel.ERROR)
            onStep(PatchStep.Failed(e.message ?: "Erro desconhecido", e.stackTraceToString()))
            throw e
        }
    }

    private suspend fun downloadFileWithProgress(
        url: String,
        destination: File,
        onProgress: (Float, Long, Long) -> Unit,
        onLog: (String, LogLevel) -> Unit,
        fileLabel: String
    ) {
        if (destination.exists()) {
            destination.delete()
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "BetterCraft-Installer-Android")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Falha ao baixar $fileLabel (HTTP ${response.code}): ${response.message}")
        }

        val body = response.body ?: throw IllegalStateException("Resposta vazia ao baixar $fileLabel")
        val contentLength = body.contentLength()
        onLog("Conexão estabelecida para $fileLabel (${formatBytes(contentLength)})", LogLevel.INFO)

        val inputStream: InputStream = body.byteStream()
        val outputStream = FileOutputStream(destination)

        val buffer = ByteArray(64 * 1024)
        var bytesReadTotal: Long = 0
        var lastLogTime = System.currentTimeMillis()

        try {
            while (coroutineContext.isActive) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break
                outputStream.write(buffer, 0, bytesRead)
                bytesReadTotal += bytesRead

                val progress = if (contentLength > 0) {
                    (bytesReadTotal.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                } else {
                    -1f
                }
                onProgress(progress, bytesReadTotal, contentLength)

                val now = System.currentTimeMillis()
                if (now - lastLogTime > 2500) {
                    val percentStr = if (progress >= 0) "${(progress * 100).toInt()}%" else "${formatBytes(bytesReadTotal)}"
                    onLog("Baixando $fileLabel: $percentStr...", LogLevel.INFO)
                    lastLogTime = now
                }
            }
        } finally {
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            response.close()
        }
    }

    private fun repackageApkWithAssets(
        sourceApk: File,
        modZip: File,
        targetAssetsFolder: String,
        outputUnsignedApk: File,
        onStep: (PatchStep) -> Unit,
        onLog: (String, LogLevel) -> Unit
    ) {
        if (outputUnsignedApk.exists()) {
            outputUnsignedApk.delete()
        }

        val workspaceDir = sourceApk.parentFile ?: context.cacheDir
        val tempOrigAssetsZip = File(workspaceDir, "temp_orig_assets.zip")
        val tempUpdatedAssetsZip = File(workspaceDir, "temp_updated_assets.zip")

        if (tempOrigAssetsZip.exists()) tempOrigAssetsZip.delete()
        if (tempUpdatedAssetsZip.exists()) tempUpdatedAssetsZip.delete()

        val innerGameFolder = resolveInnerGameFolder(targetAssetsFolder)

        val sourceZip = ZipFile(sourceApk)
        val assetsZipEntry = sourceZip.getEntry("assets/assets.zip")

        if (assetsZipEntry != null) {
            onLog("Estrutura oficial Luanti detectada: 'assets/assets.zip' encontrado!", LogLevel.SUCCESS)
            onLog("Injetando subgame BetterCraft dentro de <luanti.zip>/assets/assets.zip/$innerGameFolder", LogLevel.PROGRESS)

            // 1. Extract assets/assets.zip from Luanti APK
            sourceZip.getInputStream(assetsZipEntry).use { input ->
                FileOutputStream(tempOrigAssetsZip).use { output ->
                    input.copyTo(output)
                }
            }
            onLog("assets.zip interno extraído: ${formatBytes(tempOrigAssetsZip.length())}", LogLevel.INFO)

            // 2. Inject BetterCraft into tempOrigAssetsZip -> tempUpdatedAssetsZip
            injectModIntoAssetsZip(
                origAssetsZip = tempOrigAssetsZip,
                modZip = modZip,
                innerGameFolder = innerGameFolder,
                outputAssetsZip = tempUpdatedAssetsZip,
                onStep = onStep,
                onLog = onLog
            )
            onLog("assets.zip interno atualizado: ${formatBytes(tempUpdatedAssetsZip.length())}", LogLevel.SUCCESS)

            // 3. Rebuild APK: copy all entries from source APK except META-INF signatures and assets/assets.zip
            onLog("Montando APK final com o novo assets.zip inserido...", LogLevel.PROGRESS)
            val zos = ZipOutputStream(FileOutputStream(outputUnsignedApk))
            val writtenPaths = HashSet<String>()
            val totalApkEntries = sourceZip.size()
            var processedCount = 0

            val entries = sourceZip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                processedCount++

                // Skip previous signature files in META-INF
                if (isSignatureFile(name)) {
                    continue
                }

                // Skip original assets/assets.zip
                if (name == "assets/assets.zip") {
                    continue
                }

                val newEntry = ZipEntry(name)
                // CRITICAL: Native shared libraries (.so)
                if (name.endsWith(".so")) {
                    // Storing native libraries (.so) as DEFLATED ensures Android Package Manager extracts them
                    // to the app's native library directory on disk upon APK installation.
                    // This guarantees 4KB/16KB memory page alignment on Android 11+ and emulators (sdk_gphone64_arm64),
                    // preventing immediate dlopen / unaligned page crash when Luanti is launched!
                    newEntry.method = ZipEntry.DEFLATED
                } else if (entry.method == ZipEntry.STORED) {
                    newEntry.method = ZipEntry.STORED
                    newEntry.size = entry.size
                    newEntry.compressedSize = entry.compressedSize
                    newEntry.crc = entry.crc
                } else {
                    newEntry.method = ZipEntry.DEFLATED
                }

                zos.putNextEntry(newEntry)
                sourceZip.getInputStream(entry).use { it.copyTo(zos) }
                zos.closeEntry()
                writtenPaths.add(name)

                if (processedCount % 500 == 0) {
                    onStep(PatchStep.ExtractingAndInjecting(name, processedCount, totalApkEntries))
                }
            }

            // Now insert the updated assets/assets.zip into the APK
            // CRITICAL: Luanti C++ engine (porting_android.cpp) opens assets/assets.zip using AAsset_openFileDescriptor!
            // Android NDK strictly requires assets to be uncompressed (STORED) to return a valid file descriptor.
            // If DEFLATED, AAsset_openFileDescriptor returns -1 and Luanti aborts immediately on launch!
            onLog("Inserindo assets/assets.zip atualizado no APK como STORED (acesso NDK direto)...", LogLevel.INFO)
            val assetsCrc = calculateCrc32(tempUpdatedAssetsZip)
            val newAssetsEntry = ZipEntry("assets/assets.zip")
            newAssetsEntry.method = ZipEntry.STORED
            newAssetsEntry.size = tempUpdatedAssetsZip.length()
            newAssetsEntry.compressedSize = tempUpdatedAssetsZip.length()
            newAssetsEntry.crc = assetsCrc
            zos.putNextEntry(newAssetsEntry)
            FileInputStream(tempUpdatedAssetsZip).use { it.copyTo(zos) }
            zos.closeEntry()
            writtenPaths.add("assets/assets.zip")

            zos.finish()
            zos.flush()
            zos.close()

            // Cleanup temp files
            tempOrigAssetsZip.delete()
            tempUpdatedAssetsZip.delete()

        } else {
            // Fallback for APKs that do not contain assets/assets.zip
            onLog("Aviso: 'assets/assets.zip' não encontrado no APK base. Injetando diretamente em assets/$innerGameFolder...", LogLevel.WARNING)
            repackageApkDirectAssets(
                sourceZip = sourceZip,
                modZip = modZip,
                destFolder = "assets/$innerGameFolder",
                outputUnsignedApk = outputUnsignedApk,
                onStep = onStep,
                onLog = onLog
            )
        }

        sourceZip.close()
    }

    private fun injectModIntoAssetsZip(
        origAssetsZip: File,
        modZip: File,
        innerGameFolder: String,
        outputAssetsZip: File,
        onStep: (PatchStep) -> Unit,
        onLog: (String, LogLevel) -> Unit
    ) {
        val origZip = ZipFile(origAssetsZip)
        val zos = ZipOutputStream(FileOutputStream(outputAssetsZip))
        val writtenEntries = HashSet<String>()

        // 1. Copy original assets.zip entries (excluding any existing games in the same target folder)
        val entries = origZip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val name = entry.name
            if (name.startsWith(innerGameFolder)) {
                continue
            }

            val newEntry = ZipEntry(name)
            if (entry.method == ZipEntry.STORED) {
                newEntry.method = ZipEntry.STORED
                newEntry.size = entry.size
                newEntry.compressedSize = entry.compressedSize
                newEntry.crc = entry.crc
            } else {
                newEntry.method = ZipEntry.DEFLATED
            }

            zos.putNextEntry(newEntry)
            origZip.getInputStream(entry).use { it.copyTo(zos) }
            zos.closeEntry()
            writtenEntries.add(name)
        }
        origZip.close()

        // 2. Read mod zip and inject into innerGameFolder (e.g. games/bettercraft/)
        val modZipFile = ZipFile(modZip)
        val commonPrefix = findCommonPrefix(modZipFile)
        val modEntries = modZipFile.entries()
        var modFileCount = 0

        while (modEntries.hasMoreElements()) {
            val entry = modEntries.nextElement()
            if (entry.isDirectory) continue

            val rawName = entry.name
            val relativeName = if (commonPrefix.isNotEmpty() && rawName.startsWith(commonPrefix)) {
                rawName.removePrefix(commonPrefix)
            } else {
                rawName
            }.trimStart('/')

            if (relativeName.isEmpty()) continue

            val destPath = "$innerGameFolder$relativeName"
            if (!writtenEntries.contains(destPath)) {
                val newEntry = ZipEntry(destPath)
                newEntry.method = ZipEntry.DEFLATED
                zos.putNextEntry(newEntry)
                modZipFile.getInputStream(entry).use { it.copyTo(zos) }
                zos.closeEntry()
                writtenEntries.add(destPath)
                modFileCount++

                if (modFileCount % 100 == 0) {
                    onStep(PatchStep.ExtractingAndInjecting(relativeName, modFileCount, modFileCount))
                }
            }
        }
        modZipFile.close()

        // Inject default minetest.conf to guarantee default game and fallback paths
        if (!writtenEntries.contains("minetest.conf")) {
            val confEntry = ZipEntry("minetest.conf")
            confEntry.method = ZipEntry.DEFLATED
            zos.putNextEntry(confEntry)
            val defaultConf = """
                # BetterCraft Luanti Configuration
                default_game = bettercraft
                main_menu_game_mgr = 1
                secure.enable_security = false
            """.trimIndent()
            zos.write(defaultConf.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            writtenEntries.add("minetest.conf")
            onLog("Configuração minetest.conf injetada (default_game = bettercraft)", LogLevel.INFO)
        }

        zos.finish()
        zos.flush()
        zos.close()

        onLog("Injetados $modFileCount arquivos em assets.zip/$innerGameFolder com sucesso!", LogLevel.SUCCESS)
    }

    private fun repackageApkDirectAssets(
        sourceZip: ZipFile,
        modZip: File,
        destFolder: String,
        outputUnsignedApk: File,
        onStep: (PatchStep) -> Unit,
        onLog: (String, LogLevel) -> Unit
    ) {
        val zos = ZipOutputStream(FileOutputStream(outputUnsignedApk))
        val writtenPaths = HashSet<String>()
        val totalApkEntries = sourceZip.size()
        var processedCount = 0

        val entries = sourceZip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val name = entry.name
            processedCount++

            if (isSignatureFile(name)) continue

            val newEntry = ZipEntry(name)
            if (entry.method == ZipEntry.STORED) {
                newEntry.method = ZipEntry.STORED
                newEntry.size = entry.size
                newEntry.compressedSize = entry.compressedSize
                newEntry.crc = entry.crc
            } else {
                newEntry.method = ZipEntry.DEFLATED
            }

            zos.putNextEntry(newEntry)
            sourceZip.getInputStream(entry).use { it.copyTo(zos) }
            zos.closeEntry()
            writtenPaths.add(name)

            if (processedCount % 500 == 0) {
                onStep(PatchStep.ExtractingAndInjecting(name, processedCount, totalApkEntries))
            }
        }

        val modZipFile = ZipFile(modZip)
        val firstPrefix = findCommonPrefix(modZipFile)
        val modEntries = modZipFile.entries()
        var modFileCount = 0

        while (modEntries.hasMoreElements()) {
            val entry = modEntries.nextElement()
            if (entry.isDirectory) continue

            val rawName = entry.name
            val relativeName = if (firstPrefix.isNotEmpty() && rawName.startsWith(firstPrefix)) {
                rawName.removePrefix(firstPrefix)
            } else {
                rawName
            }.trimStart('/')

            if (relativeName.isEmpty()) continue

            val destPath = "$destFolder$relativeName"
            val newEntry = ZipEntry(destPath)
            newEntry.method = ZipEntry.DEFLATED

            zos.putNextEntry(newEntry)
            modZipFile.getInputStream(entry).use { it.copyTo(zos) }
            zos.closeEntry()
            writtenPaths.add(destPath)
            modFileCount++

            if (modFileCount % 100 == 0) {
                onStep(PatchStep.ExtractingAndInjecting(relativeName, modFileCount, modFileCount))
            }
        }
        modZipFile.close()

        zos.finish()
        zos.flush()
        zos.close()
        onLog("Injetados $modFileCount arquivos em $destFolder com sucesso!", LogLevel.SUCCESS)
    }

    private fun resolveInnerGameFolder(targetAssetsFolder: String): String {
        var clean = targetAssetsFolder.trim().trim('/')
        if (clean.startsWith("assets/assets.zip/")) {
            clean = clean.removePrefix("assets/assets.zip/").trim('/')
        } else if (clean.startsWith("assets/")) {
            clean = clean.removePrefix("assets/").trim('/')
        }
        if (clean == "games" || clean.isEmpty()) {
            return "games/bettercraft/"
        }
        if (!clean.startsWith("games/")) {
            clean = "games/$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }

    private fun findCommonPrefix(zip: ZipFile): String {
        val entries = zip.entries()
        var firstRoot = ""
        while (entries.hasMoreElements()) {
            val e = entries.nextElement()
            val slashIdx = e.name.indexOf('/')
            if (slashIdx != -1) {
                val prefix = e.name.substring(0, slashIdx + 1)
                if (firstRoot.isEmpty()) {
                    firstRoot = prefix
                } else if (firstRoot != prefix) {
                    // Different roots
                    return ""
                }
            } else {
                return ""
            }
        }
        return firstRoot
    }

    private fun isSignatureFile(name: String): Boolean {
        if (!name.startsWith("META-INF/")) return false
        val upper = name.uppercase()
        return upper.endsWith(".SF") ||
                upper.endsWith(".RSA") ||
                upper.endsWith(".DSA") ||
                upper.endsWith(".EC") ||
                upper == "META-INF/MANIFEST.MF" ||
                upper.startsWith("META-INF/SIG-")
    }

    private fun calculateCrc32(file: File): Long {
        val crc = java.util.zip.CRC32()
        val buffer = ByteArray(65536)
        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                crc.update(buffer, 0, read)
            }
        }
        return crc.value
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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
}
