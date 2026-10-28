/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.di

import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module
import paige.navic.work.DownloadMigrationWorker
import paige.navic.work.DownloadWorker
import paige.navic.work.LibrarySyncWorker
import paige.navic.work.SyncActionWorker

val workerModule = module {
	workerOf(::DownloadWorker)
	workerOf(::DownloadMigrationWorker)
	workerOf(::LibrarySyncWorker)
	workerOf(::SyncActionWorker)
}
