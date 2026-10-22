/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.work

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import paige.navic.R
import paige.navic.database.dao.DownloadDao
import paige.navic.database.entity.DownloadEntity
import paige.navic.database.entity.DownloadStatus
import paige.navic.domain.download.DownloadCancelledException
import paige.navic.domain.download.DownloadProcessor
import paige.navic.domain.manager.NotificationIds
import paige.navic.domain.manager.NotificationManager
import paige.navic.util.Logger

class DownloadWorker(
	context: Context,
	params: WorkerParameters,
	private val downloadProcessor: DownloadProcessor,
	private val downloadDao: DownloadDao,
	private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {
	private val concurrency = 4
	private val progressMutex = Mutex()

	override suspend fun doWork(): Result {
		return try {
			var downloaded = 0
			var failed = 0

			while (true) {
				val queued = downloadDao.getDownloadsByStatus(DownloadStatus.DOWNLOADING)
				if (queued.isEmpty()) break

				val batch = processBatch(queued)

				downloaded += batch.first
				failed += batch.second
			}

			Logger.i(
				"DownloadWorker",
				"download queue finished: $downloaded downloaded, $failed failed"
			)
			Result.success()
		} catch (e: CancellationException) {
			throw e
		} catch (e: Exception) {
			Logger.e("DownloadWorker", "download queue failed", e)
			if (runAttemptCount < 3) {
				Result.retry()
			} else {
				try {
					downloadDao.getDownloadsByStatus(DownloadStatus.DOWNLOADING)
						.forEach { download ->
							downloadDao.insertDownload(
								DownloadEntity(
									download.songId,
									DownloadStatus.FAILED,
									0f
								)
							)
						}
				} catch (e: CancellationException) {
					throw e
				} catch (e: Exception) {
					Logger.e("DownloadWorker", "could not mark queued downloads failed", e)
				}

				Result.failure()
			}
		}
	}

	private suspend fun processBatch(
		queued: List<DownloadEntity>
	): Pair<Int, Int> {
		val total = queued.size
		var completed = 0
		var failed = 0

		setForeground(createForegroundInfo(0, total))

		val queue = Channel<DownloadEntity>(Channel.UNLIMITED)
		queued.forEach { queue.trySend(it) }
		queue.close()

		coroutineScope {
			val workers = List(concurrency) {
				launch {
					for ((songId) in queue) {
						try {
							downloadProcessor.download(songId)

							progressMutex.withLock {
								completed++
								updateForeground(completed, total)
							}
						} catch (_: DownloadCancelledException) {
							progressMutex.withLock {
								completed++
								updateForeground(completed, total)
							}
						} catch (e: CancellationException) {
							throw e
						} catch (e: Exception) {
							Logger.e(
								"DownloadWorker",
								"failed to download $songId",
								e
							)

							val current = downloadDao.getDownloadById(songId)
							val markedFailed = current?.status == DownloadStatus.DOWNLOADING

							if (markedFailed) {
								downloadDao.insertDownload(
									DownloadEntity(
										songId,
										DownloadStatus.FAILED,
										0f
									)
								)
							}

							progressMutex.withLock {
								completed++
								if (markedFailed) failed++
								updateForeground(completed, total)
							}
						}
					}
				}
			}

			workers.joinAll()
		}

		return completed to failed
	}

	private suspend fun updateForeground(completed: Int, total: Int) {
		if (total <= 0) return

		val progress = completed.toFloat() / total.toFloat()

		setProgress(
			workDataOf(
				WorkKeys.COMPLETED to completed,
				WorkKeys.TOTAL to total,
				WorkKeys.PROGRESS to progress
			)
		)
		setForeground(createForegroundInfo(completed, total))
	}

	private fun createForegroundInfo(completed: Int, total: Int): ForegroundInfo {
		val progress = if (total <= 0) 0f else completed.toFloat() / total.toFloat()
		val message = if (total <= 0) {
			applicationContext.getString(R.string.info_status_downloading)
		} else {
			"$completed / $total"
		}

		val title = applicationContext.getString(R.string.info_status_downloading)
		val notification = notificationManager.createProgressNotification(
			id = NotificationIds.DOWNLOAD_LIBRARY,
			title = "$title (${(progress * 100).toInt()}%)",
			message = message,
			progress = progress,
			indeterminate = total <= 0
		)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ForegroundInfo(
				NotificationIds.DOWNLOAD_LIBRARY,
				notification,
				FOREGROUND_SERVICE_TYPE_DATA_SYNC
			)
		} else {
			ForegroundInfo(NotificationIds.DOWNLOAD_LIBRARY, notification)
		}
	}

	override suspend fun getForegroundInfo(): ForegroundInfo {
		val total = downloadDao.countDownloadsByStatus(DownloadStatus.DOWNLOADING)
		return createForegroundInfo(0, total)
	}
}
