/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.manager

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.appmattus.crypto.Algorithm
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import paige.navic.domain.model.settings.DownloadStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class StorageManager(
	private val context: Context,
	private val preferenceManager: PreferenceManager
) {
	private data class HashResult(
		val parentDirectory: Pair<String, String>,
		val fileName: String
	)

	private val dispatcher = Dispatchers.IO

	private fun internalDownloadDir(): File = File(context.filesDir, "downloads")

	private fun externalDownloadDirs(): List<File> =
		context.getExternalFilesDirs(null)
			.filterNotNull()
			.filter { dir ->
				runCatching {
					val volume = context
						.getSystemService(android.os.storage.StorageManager::class.java)
						?.getStorageVolume(dir)

					volume?.isRemovable == true &&
						Environment.getExternalStorageState(dir) == Environment.MEDIA_MOUNTED
				}.getOrDefault(false)
			}
			.map { File(it, "downloads") }

	fun externalDownloadDir(): File? = externalDownloadDirs().firstOrNull()

	fun isExternalStorageAvailable(): Boolean = externalDownloadDir() != null

	fun getDownloadDirFor(storage: DownloadStorage): File? = when (storage) {
		DownloadStorage.Internal -> internalDownloadDir().apply { mkdirs() }
		DownloadStorage.External -> externalDownloadDir()?.apply { mkdirs() }
	}

	fun currentDownloadDir(): File =
		getDownloadDirFor(preferenceManager.downloadStorage)
			?: internalDownloadDir().apply { mkdirs() }

	fun allDownloadDirs(): List<File> =
		(listOf(internalDownloadDir()) + externalDownloadDirs())
			.distinctBy { it.absolutePath }

	private fun generatePathHash(albumId: String, songId: String): HashResult {
		val template = "$albumId-$songId".takeIf { it.length > 4 }
			?: UUID.randomUUID().toString()
		val hash = template.hashString()

		return HashResult(
			parentDirectory = Pair(
				hash.substring(0, 2),
				hash.substring(2, 4)
			),
			fileName = hash.substring(4)
		)
	}

	fun getDownloadPath(
		songId: String,
		albumId: String? = null,
		extension: String = "bin"
	): String {
		val downloadsDir = currentDownloadDir()
		return generatePathHash(albumId ?: songId, songId)
			.let { hashResult ->
				val dir = File(
					File(downloadsDir, hashResult.parentDirectory.first),
					hashResult.parentDirectory.second
				).apply { mkdirs() }

				File(dir, "${hashResult.fileName}.$extension").absolutePath
			}
	}

	fun getTempDownloadPath(path: String): String = "$path.part"

	fun commitFile(tempPath: String, finalPath: String): Boolean {
		val tempFile = File(tempPath)
		val finalFile = File(finalPath)
		if (!tempFile.exists()) return false

		if (finalFile.exists()) finalFile.delete()

		return tempFile.renameTo(finalFile)
	}

	fun moveFile(sourcePath: String, targetPath: String): Boolean {
		val source = File(sourcePath)
		if (!source.exists()) return false

		val target = File(targetPath)
		target.parentFile?.mkdirs()

		if (target.exists()) target.delete()

		if (source.renameTo(target)) return true

		val temp = File("$targetPath.part")
		return try {
			source.inputStream().use { input ->
				temp.outputStream().use { output -> input.copyTo(output) }
			}

			if (target.exists()) target.delete()

			if (temp.renameTo(target)) {
				source.delete()
				true
			} else {
				temp.delete()
				false
			}
		} catch (_: Exception) {
			temp.delete()
			false
		}
	}

	fun deleteFile(path: String): Boolean {
		return File(path).delete()
	}

	fun deleteTempFile(path: String): Boolean {
		return deleteFile(getTempDownloadPath(path))
	}

	fun deleteSongFiles(songId: String) {
		allDownloadDirs().forEach { dir ->
			if (!dir.exists()) return@forEach

			dir.walkTopDown()
				.filter { it.isFile && it.name.startsWith("$songId.") }
				.toList()
				.forEach { it.delete() }
		}
	}

	fun clearTempDownloads() {
		allDownloadDirs().forEach { dir ->
			if (!dir.exists()) return@forEach

			dir.walkTopDown()
				.filter { it.isFile && it.name.endsWith(".part") }
				.toList()
				.forEach { it.delete() }
		}
	}

	fun getFileSize(path: String): Long {
		return try {
			File(path).length()
		} catch (_: Exception) {
			0L
		}
	}

	suspend fun saveFile(path: String, channel: ByteReadChannel) {
		withContext(dispatcher) {
			FileOutputStream(path).use { outputStream ->
				channel.copyTo(outputStream)
			}
		}
	}

	fun clearDownloads() {
		allDownloadDirs().forEach { dir ->
			if (dir.exists()) {
				dir.listFiles()?.forEach { it.deleteRecursively() }
			}
		}
	}

	fun getCurrentFreeSpace(): Long {
		val fileStats = StatFs(currentDownloadDir().absolutePath)
		return fileStats.freeBytes
	}

	private fun String.hashString(): String {
		val digest = Algorithm.XXHash32().createDigest()
		return digest.digest(this.toByteArray(Charsets.UTF_8))
			.joinToString("") { byte -> "%02x".format(byte) }
	}
}
